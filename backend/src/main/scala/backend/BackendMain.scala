package backend

import cats.effect.IO
import cats.effect.IOApp
import cps.*
import cps.monads.catsEffect.given
import cps.monads.catsEffect.*
import cats.effect.Resource
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.duration.DurationInt
import cats.data.{Kleisli, OptionT}
import cats.effect.{IO, Resource}
import cats.implicits.given
import com.github.plokhotnyuk.jsoniter_scala.core.writeToString
import fs2.Stream
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.server.staticcontent.{fileService, FileService, MemoryCache}
import org.http4s.*
import http4sJsoniter.ArrayEntityCodec.*
import org.http4s.headers.`Content-Type`

import doobie.util.transactor.Transactor
import doobie.ConnectionIO
import doobie.implicits._
import io.getquill._
import dbtypes.schema.*
import dbtypes.schema.SchemaExtensions.*
import cats.implicits.*

object BackendMain extends IOApp.Simple {
  def run = async[IO] {
    val appConfig = AppConfig.fromEnv()

    await(Woo.runQueryBench)

    // await(HttpServer.start(appConfig))
  }
}

object Woo {
  val ctx = doobie.DoobieContext.SQLite(Literal)
  import ctx._
  val xa = Transactor.fromDriverManager[IO]("org.sqlite.JDBC", "jdbc:sqlite:data.db?foreign_keys=ON", None)

  def woo = async[IO] {

    val queryRun: ConnectionIO[Unit] = run(FooDao.query).map(println(_))
    await(queryRun.transact(xa))
  }

  def runQueryBench = async[IO] {
    println("starting")
    val start = System.nanoTime()
    val n     = 2000
    for (i <- 1 to n) {
      // await(run(FooDao.query).transact(xa))
      // //
      await(run(MyidsDao.query.insertValue(Myids(lift(i)))).transact(xa))
    }
    val end = System.nanoTime()
    println("finished: " + ((end - start) / n.toDouble / 1000000) + "ms per query")
  }
}

object RpcApiImpl extends rpc.RpcApi {
  def fun(x: Int) = IO.pure(x + 1)
}

object HttpServer {
  def start(config: AppConfig): IO[Unit] = asyncScope[IO] {
    val routes = ServerRoutes.fileRoutes(config) <+>
//        infoRoutes(state) <+>
      ServerRoutes.slothRpcRoutes()

    val _ = await(
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(Logger.httpApp(true, true)(routes.orNotFound))
        .withShutdownTimeout(1.seconds)
        .build
    )

    await(IO.never)
  }
}

case class AppConfig(
  // authnUrl: String,
  frontendDistributionPath: String
)

object AppConfig {
  def fromEnv(): AppConfig = AppConfig(
    // authnUrl = sys.env("AUTHN_URL"),
    frontendDistributionPath = sys.env("FRONTEND_DISTRIBUTION_PATH")
  )
}

object ServerRoutes {
  private val dsl = Http4sDsl[IO]
  import dsl.*

  def slothRpcRoutes(): HttpRoutes[IO] = {
    import chameleon.{Deserializer, Serializer}
    import chameleon.ext.jsoniter.given
    import sloth.{Router, ServerFailure}
    import com.github.plokhotnyuk.jsoniter_scala.core.given
    import rpc.JsonCodecs.given

    val slothRouter = Router[String, IO].route[rpc.RpcApi](RpcApiImpl)

    // implement generic http route to call the sloth router
    HttpRoutes.of[IO] { case request @ _ -> Root / apiName / methodName =>
      request.as[String].flatMap { payload =>
        val requestPath = List(apiName, methodName)
        slothRouter(sloth.Request(requestPath, payload)) match {
          case Left(error)     => InternalServerError(error.toString) // TODO: print errror instead of returning
          case Right(response) => Ok(response)
        }
      }
    }
  }

  def fileRoutes(config: AppConfig): HttpRoutes[IO] = {
    fileService[IO](
      FileService.Config(
        systemPath = config.frontendDistributionPath,
        cacheStrategy = MemoryCache[IO](),
      )
    )
  }

  // private def infoRoutes(state: ServerState): HttpRoutes[IO] = {
  //   def appConfig = AppConfig(
  //     authnUrl = state.config.authnIssuerUrl
  //   )
  //
  //   HttpRoutes.of[IO] {
  //     case GET -> Root / "info" / "version"         => Ok("TODO")
  //     case GET -> Root / "info" / "app_config.json" => Ok(appConfig)
  //     case GET -> Root / "info" / "app_config.js" =>
  //       val code =
  //         s"window.${AppConfig.domWindowProperty} = ${writeToString(appConfig)};"
  //       Ok(code, `Content-Type`(MediaType.application.`javascript`))
  //   }
  // }

}
