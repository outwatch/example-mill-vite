package frontend

import outwatch._
import outwatch.dsl._
import cats.effect.IO
import sloth.{Client, Request, RequestTransport}
import org.scalajs.dom
import cats.effect.IOApp

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

object Main extends IOApp.Simple {
  def run = {
    val myComponent = div(
      "Hello World",
      RpcClient.requestRpc.fun(5),
    )

    // render the component into the <div id="app"></div> in index.html
    Outwatch.renderReplace[IO]("#app", myComponent)
  }
}

object RpcClient {
  import chameleon.ext.jsoniter.given
  import rpc.JsonCodecs.given
  val requestRpc = Client[String, IO](RequestRpcTransport).wire[rpc.RpcApi]
}

private object RequestRpcTransport extends RequestTransport[String, IO] {
  override def apply(request: Request[String]): IO[String] = {
    import org.scalajs.dom.window.location
    val url         = s"${location.origin}/${request.path.mkString("/")}"
    val requestArgs = new dom.RequestInit { method = dom.HttpMethod.POST; body = request.payload }
    IO.fromThenable(IO(dom.fetch(url, requestArgs).`then`[String](_.text())))
  }
}
