package frontend

import cats.effect.IO
import org.scalajs.dom
import sloth.ext.http4s.client.*
import org.http4s.dom.*

object RpcClient {
  import chameleon.ext.upickle.given // TODO: Option as null

  private val httpConfig    = IO.pure(HttpRequestConfig())
  private val fetchClient   = FetchClientBuilder[IO].create
  private val requestClient = sloth.Client[String, IO](HttpRpcTransport(fetchClient, httpConfig))

  val call: rpc.RpcApi = requestClient.wire[rpc.RpcApi]
}
