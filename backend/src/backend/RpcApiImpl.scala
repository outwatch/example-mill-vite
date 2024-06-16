package backend

import cats.effect.IO
import org.http4s.Request
import org.http4s.headers.Authorization
import org.http4s.AuthScheme
import org.http4s.Credentials
import cats.implicits.*
import org.sqlite.SQLiteDataSource
import com.augustnagro.magnum
import com.augustnagro.magnum.*
import io.github.arainko.ducktape.*
import org.http4s.ember.client.EmberClientBuilder
import cats.effect.unsafe.implicits.global // TODO
import scala.util.control.NonFatal
import scala.concurrent.duration.*

// import authn.backend.TokenVerifier
// import authn.backend.AuthnClient
// import authn.backend.AuthnClientConfig
// import authn.backend.AccountImport

class RpcApiImpl(request: Request[IO]) extends rpc.RpcApi {

  val headers: Option[Authorization] = request.headers.get[Authorization]
  val deviceToken: Option[String]    = headers.collect { case Authorization(Credentials.Token(AuthScheme.Bearer, token)) => token }
  println(request.headers)

  // Authn integration
  // val headers: Option[Authorization] = request.headers.get[Authorization]
  // val token: Option[String]          = headers.collect { case Authorization(Credentials.Token(AuthScheme.Bearer, token)) => token }
  // val httpClient = EmberClientBuilder.default[IO].withTimeout(44.seconds).build.allocated.map(_._1).unsafeRunSync() // TODO not forever
  // val authnClient = AuthnClient[IO](
  //   AuthnClientConfig(
  //     issuer = "http://localhost:3000",
  //     audiences = Set("localhost"),
  //     username = "admin",
  //     password = "adminpw",
  //     adminURL = Some("http://localhost:3001"),
  //   ),
  //   httpClient = httpClient,
  // )
  // val verifier = TokenVerifier[IO]("http://localhost:3000", Set("localhost"))
  // def userAccountId: IO[Option[String]] = token.traverse(token => verifier.verify(token).map(_.accountId))
  // def withUser[T](code: String => IO[T]): IO[T] = userAccountId.flatMap {
  //   case Some(accountId) => code(accountId)
  //   case None            => IO.raiseError(Exception("403 Unauthorized"))
  // }
  //
  println(deviceToken)

  def withDevice[T](code: String => IO[T]): IO[T] = deviceToken match {
    case Some(deviceToken) => code(deviceToken)
    case None              => IO.raiseError(Exception("403 Unauthorized"))
  }

  val ds = SQLiteDataSource().tap(_.setUrl("jdbc:sqlite:data.db?foreign_keys=ON"))

  def send(messageId: Int, deviceId: String): IO[Unit] = withDevice(accountId =>
    IO {
      magnum.connect(ds) {
        db.InboxRepo.insert(db.Inbox.Creator(messageId, deviceId))
      }
    }
  )

  def create(content: String): IO[Unit] = withDevice(deviceId =>
    IO {
      magnum.transact(ds) {
        val message = db.MessageRepo.insertReturning(db.Message.Creator(content))
        db.InboxRepo.insert(db.Inbox.Creator(messageId = message.messageId, deviceId = deviceId))
      }
    }
  )

  def getInbox(): IO[Vector[rpc.Message]] = withDevice(deviceId =>
    IO {
      magnum.connect(ds) {
        val dbMessages =
          sql"select message_id, content from inbox join message using(message_id) where device_id = ${deviceId}".query[db.Message].run()
        dbMessages.map(_.to[rpc.Message])
      }
    }
  )

  def trust(contactDeviceId: String): IO[Unit] = withDevice(deviceId =>
    IO {
      magnum.connect(ds) {
        db.ContactsRepo.insert(db.Contacts.Creator(deviceId = deviceId, contactDeviceId = contactDeviceId))
      }
    }
  )
}
