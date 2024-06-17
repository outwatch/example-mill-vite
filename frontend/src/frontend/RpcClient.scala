package frontend

import cats.effect.IO
import sloth.ext.http4s.client.*
import org.http4s.dom.*
import org.http4s.Header
import org.http4s.Headers
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme
import org.scalajs.dom.window.localStorage

// import authn.frontend.AuthnClient
// import authn.frontend.AuthnClientConfig

object RpcClient {
  import chameleon.ext.upickle.given // TODO: Option as null

  // authn:
  // val headers: IO[Headers] = lift {
  //   val client     = AuthnClient[IO](AuthnClientConfig("http://localhost:3000"))
  //   val authHeader = unlift(client.session).map(token => Authorization(Credentials.Token(AuthScheme.Bearer, token)))
  //   Headers(authHeader.toSeq)
  // }

  val getDeviceSecret = IO(Option(localStorage.getItem("deviceSecret")))

  val headers: IO[Headers] = lift {
    Headers(unlift(getDeviceSecret).map(secret => Authorization(Credentials.Token(AuthScheme.Bearer, secret))).toList)
  }

  private val httpConfig    = headers.map(headers => HttpRequestConfig(headers = headers))
  private val fetchClient   = FetchClientBuilder[IO].create
  private val requestClient = sloth.Client[String, IO](HttpRpcTransport(fetchClient, httpConfig))

  val call: rpc.RpcApi = requestClient.wire[rpc.RpcApi]
}
