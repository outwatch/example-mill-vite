package backend

import cats.data.{Kleisli, OptionT}
import cats.effect.{IO, Resource}
import cats.implicits.given
import com.comcast.ip4s.*
import cps.*
import cps.monads.catsEffect.{*, given}
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.staticcontent.{fileService, FileService, MemoryCache}
import sloth.ext.http4s.server.HttpRpcRoutes

import scala.concurrent.duration.DurationInt

object HttpServer {
  def start(config: AppConfig): IO[Unit] = asyncScope[IO] {
    val routes = ServerRoutes.fileRoutes(config) <+>
//        infoRoutes(state) <+>
      ServerRoutes.rpcRoutes()

    val _ = await(
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpApp(Logger.httpApp(true, true)(routes.orNotFound))
        .withShutdownTimeout(1.seconds)
        .build
    )

    await(IO.never)
  }
}

object ServerRoutes {
  private val dsl = Http4sDsl[IO]
  def rpcRoutes(): HttpRoutes[IO] = {
    import chameleon.ext.upickle.*
    val slothRouter = sloth.Router[String, IO].route[rpc.RpcApi](RpcApiImpl)

    HttpRpcRoutes.apply[String, IO](slothRouter)
  }

  // def slothRpcRoutes(): HttpRoutes[IO] = {
  //   import sloth.{Router, ServerFailure}
  //
  //
  //   // implement generic http route to call the sloth router
  //   HttpRoutes.of[IO] { case request @ _ -> Root / apiName / methodName =>
  //     request.as[String].flatMap { payload =>
  //       val requestPath = List(apiName, methodName)
  //       slothRouter(sloth.Request(requestPath, payload)) match {
  //         case Left(error)     => InternalServerError(error.toString) // TODO: print errror instead of returning
  //         case Right(response) => Ok(response)
  //       }
  //     }
  //   }
  // }

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
