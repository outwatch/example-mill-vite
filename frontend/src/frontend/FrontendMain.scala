package frontend

import cats.effect.IO
import cats.effect.IOApp
import outwatch.*
import outwatch.dsl.*
import colibri.*
import colibri.reactive.*
import authn.frontend.*
import authn.frontend.authnJS.keratinAuthn.distTypesMod.Credentials

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

object Main extends IOApp.Simple {
  def run = {
    val count = Var(0)
    val myComponent = {
      div(
        "Hello World, rpc: ",
        count,
        Rx { RpcClient.call.fun(count()) },
        button("click", onClick.foreach(count.update(_ + 1))),
        authControl,
      )
    }

    // render the component into the <div id="app"></div> in index.html
    Outwatch.renderReplace[IO]("#app", myComponent)
  }
}

def authControl = {
  val authn = AuthnClient[IO](
    AuthnClientConfig(
      hostUrl = "http://localhost:3000",
      sessionStorage = SessionStorage.LocalStorage("session"),
    )
  )
  div(
    button(
      "Register",
      onClick.doEffect {
        authn.signup(Credentials(username = "est", password = "wolfgang254!!??"))
      },
    ),
    button(
      "Login",
      onClick.doEffect {
        authn.login(Credentials(username = "est", password = "wolfgang254!!??"))
      },
    ),
    b(authn.session),
    button(
      "Logout",
      onClick.doEffect {
        authn.logout
      },
    ),
  )
}
