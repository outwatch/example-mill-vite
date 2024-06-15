package backend

import cats.effect.IO
import authn.backend.TokenVerifier
import org.http4s.Request
import org.http4s.headers.Authorization
import org.http4s.AuthScheme
import org.http4s.Credentials
import cats.implicits.*
import org.sqlite.SQLiteDataSource
import com.augustnagro.magnum
import com.augustnagro.magnum.*
import io.github.arainko.ducktape.*
import authn.backend.AuthnClient
import authn.backend.AuthnClientConfig
import org.http4s.ember.client.EmberClientBuilder
import cats.effect.unsafe.implicits.global // TODO
import authn.backend.AccountImport
import scala.util.control.NonFatal

class RpcApiImpl(request: Request[IO]) extends rpc.RpcApi {

  val headers: Option[Authorization] = request.headers.get[Authorization]
  val token: Option[String]          = headers.collect { case Authorization(Credentials.Token(AuthScheme.Bearer, token)) => token }

  val httpClient = EmberClientBuilder.default[IO].build.allocated.map(_._1).unsafeRunSync() // TODO not forever

  val authnClient = AuthnClient[IO](
    AuthnClientConfig(
      issuer = "http://localhost:3000",
      audiences = Set("localhost"),
      username = "admin",
      password = "adminpw",
      adminURL = Some("http://localhost:3001"),
    ),
    httpClient = httpClient,
  )
  val verifier = TokenVerifier[IO]("http://localhost:3000", Set("localhost"))

  def userAccountId: IO[Option[String]] = token.traverse(token => verifier.verify(token).map(_.accountId))

  def withUser[T](code: String => IO[T]): IO[T] = userAccountId.flatMap {
    case Some(accountId) => code(accountId)
    case None            => IO.raiseError(Exception("403 Unauthorized"))
  }

  val ds = SQLiteDataSource().tap(_.setUrl("jdbc:sqlite:data.db?foreign_keys=ON"))

  def register(username: String, password: String): IO[Unit] = lift {
    val accountImport = unlift(authnClient.importAccount(AccountImport(username, password)))
    unlift(
      IO {
        magnum.connect(ds) {
          db.UserProfileRepo.insert(db.UserProfile.Creator(userId = accountImport.id.toString, userName = username))
        }
      }.onError(_ =>
        IO {
          // if database fails, remove the just created account
          authnClient.archiveAccount(accountImport.id.toString)
        }
      )
    )
  }

  def send(messageId: Int, userId: String): IO[Unit] = withUser(accountId =>
    IO {
      magnum.connect(ds) {
        db.InboxRepo.insert(db.Inbox.Creator(messageId, userId))
      }
    }
  )

  def create(content: String): IO[Unit] = withUser(userId =>
    IO {
      magnum.transact(ds) {
        val message = db.MessageRepo.insertReturning(db.Message.Creator(content))
        db.InboxRepo.insert(db.Inbox.Creator(messageId = message.messageId, userId = userId))
      }
    }
  )

  def getInbox(): IO[Vector[rpc.Message]] = withUser(userId =>
    IO {
      magnum.connect(ds) {
        val dbMessages =
          sql"select message_id, content from inbox join message using(message_id) where user_id = ${userId}".query[db.Message].run()
        dbMessages.map(_.to[rpc.Message])
      }
    }
  )

  def addContact(contactUserId: String): IO[Unit] = withUser(userId =>
    IO {
      magnum.connect(ds) {
        db.ContactsRepo.insert(db.Contacts.Creator(userId = userId, contactUserId = contactUserId))
      }
    }
  )
}
