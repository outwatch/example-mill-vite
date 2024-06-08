package frontend

import cats.effect.IO
import org.scalajs.dom
import outwatch.dsl.s
import sloth.{Client, Request, RequestTransport}

object RpcClient {
  import chameleon.ext.jsoniter.given
  import rpc.JsonCodecs.given
  val call = Client[String, IO](RequestRpcTransport).wire[rpc.RpcApi]
}

private object RequestRpcTransport extends RequestTransport[String, IO] {
  override def apply(request: Request[String]): IO[String] = {
    import org.scalajs.dom.window.location
    val url         = s"${location.origin}/${request.path.mkString("/")}"
    val requestArgs = new dom.RequestInit { method = dom.HttpMethod.POST; body = request.payload }
    IO.fromThenable(IO(dom.fetch(url, requestArgs).`then`[String](_.text())))
  }
}
