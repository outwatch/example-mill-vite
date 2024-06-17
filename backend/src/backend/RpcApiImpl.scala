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
import rpc.{generateSecureKey, PublicDeviceProfile}
import scala.util.Try
import scala.util.Success
import scala.util.Failure
// import org.http4s.ember.client.EmberClientBuilder
// import cats.effect.unsafe.implicits.global // TODO
// import scala.util.control.NonFatal
// import scala.concurrent.duration.*

// import authn.backend.TokenVerifier
// import authn.backend.AuthnClient
// import authn.backend.AuthnClientConfig
// import authn.backend.AccountImport

class RpcApiImpl(request: Request[IO]) extends rpc.RpcApi {

  val headers: Option[Authorization] = request.headers.get[Authorization]
  val deviceSecret: Option[String]   = headers.collect { case Authorization(Credentials.Token(AuthScheme.Bearer, secret)) => secret }
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

  val ds = SQLiteDataSource().tap(_.setUrl("jdbc:sqlite:data.db")).tap(_.setEnforceForeignKeys(true)).tap(_.setLoadExtension(true))

  def withDevice[T](code: db.DeviceProfile => IO[T]): IO[T] = deviceId match {
    case Some(deviceId) =>
      magnum.connect(ds) {
        db.DeviceProfileRepo.findById(deviceAddress)
      } match {
        case Some(profile) => code(profile)
        case None          => IO.raiseError(Exception("403 Unauthorized"))
      }

    case None => IO.raiseError(Exception("403 Unauthorized"))
  }

  def registerDevice(deviceSecret: String): IO[Unit] = IO {
    magnum.connect(ds) {
      ???
      sql"insert into ${db.DeviceProfile.Table}(${db.DeviceProfile.Table.all}) values (${db.DeviceProfile.Creator(
          deviceId = deviceId,
          publicDeviceId = "p-" + generateSecureKey(10),
        )}) on conflict(${db.DeviceProfile.Table.deviceId}) do nothing".update
        .run()
    }
  }

  def send(messageId: Int, publicDeviceId: String): IO[Boolean] = withDevice(accountId =>
    IO {
      magnum.transact(ds) {
        lift[Option] {
          val targetDeviceProfile = unlift(db.DeviceProfileRepo.findByIndexOnPublicDeviceId(publicDeviceId))
          val message             = unlift(db.MessageRepo.findById(messageId))
          db.MessageRepo.update(message.copy(onDevice = Some(targetDeviceProfile.deviceId)))
          db.MessageHistoryRepo.insert(
            db.MessageHistory.Creator(messageId = message.messageId, onDevice = Some(targetDeviceProfile.deviceId), atPlace = None)
          )
          true
        }.getOrElse(false)
      }
    }
  )

  def create(content: String): IO[Unit] = withDevice(deviceProfile =>
    IO {
      magnum.transact(ds) {
        val message = db.MessageRepo.insertReturning(db.Message.Creator(content, onDevice = Some(deviceProfile.deviceId), atPlace = None))
      }
    }
  )

  def getOnDeviceMessages: IO[Vector[rpc.Message]] = withDevice(deviceProfile =>
    IO {
      magnum.connect(ds) {
        val dbMessages = db.MessageRepo.findByIndexOnOnDevice(Some(deviceProfile.deviceId))
        dbMessages.map(_.to[rpc.Message])
      }
    }
  )

  def getPublicDeviceId: IO[String] = withDevice(deviceProfile => IO.pure(deviceProfile.publicDeviceId))

  def trust(contactPublicDeviceId: String): IO[Boolean] = withDevice(deviceProfile =>
    IO {
      magnum.transact(ds) {
        db.DeviceProfileRepo.findByIndexOnPublicDeviceId(contactPublicDeviceId) match {
          case Some(contactDeviceProfile) =>
            db.TrustRepo.insert(db.Trust.Creator(deviceId = deviceProfile.deviceId, contactDeviceId = contactDeviceProfile.deviceId))
            true
          case None =>
            false
        }
      }
    }
  )

  override def getContacts: IO[Vector[PublicDeviceProfile]] = withDevice { deviceProfile =>
    IO {
      magnum.connect(ds) {
        val publicDeviceIds =
          sql"""select device_profile.public_device_id from trust inner join device_profile on trust.contact_device_id = device_profile.device_id"""
            .query[String]
            .run()
        publicDeviceIds.map(PublicDeviceProfile(_))
      }
    }

  }
}
