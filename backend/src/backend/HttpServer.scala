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
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.staticcontent.{fileService, FileService, MemoryCache}
import sloth.ext.http4s.server.HttpRpcRoutes

import scala.concurrent.duration.DurationInt

object HttpServer {
  def start(config: AppConfig): IO[Unit] = asyncScope[IO] {
    val routes =
      ServerRoutes.rpcRoutes(config) <+>
        ServerRoutes.fileRoutes(config)

    def errorHandler(t: Throwable, msg: => String): IO[Unit] =
      IO.println(msg) >> IO.println(t) >> IO(t.printStackTrace())

    val loggedRoutes = Logger.httpApp(logHeaders = false, logBody = false)(
      ErrorAction.log(
        routes.orNotFound,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler,
      )
    )

    val _ = await(
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpApp(loggedRoutes)
        .withShutdownTimeout(1.seconds)
        .build
    )

    await(IO.never)
  }
}

private object ServerRoutes {
  def rpcRoutes(config: AppConfig): HttpRoutes[IO] = {
    import chameleon.ext.upickle.*
    HttpRpcRoutes.withRequest[String, IO] { (request: Request[IO]) =>
      sloth.Router[String, IO].route[rpc.RpcApi](RpcApiImpl(config.dataSource, request))
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
}
