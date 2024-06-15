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
        authControl,
        createMessage,
        inbox,
      )
    }

    // render the component into the <div id="app"></div> in index.html
    Outwatch.renderReplace[IO]("#app", myComponent)
  }
}

def createMessage = {
  val messageString = Var("")
  div(
    input(tpe := "text", placeholder := "type message", value <-- messageString, onInput.value --> messageString),
    button("create", onClick.foreachEffect(_ => RpcClient.call.create(messageString.now()).void)),
  )
}

def inbox = {
  div(
    lift {
      unlift(RpcClient.call.getInbox()).map(message => div(message.content))
    }
  )
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
        RpcClient.call.register(username = "u2", password = "wolfgang254!!??")
      },
    ),
    button(
      "Login",
      onClick.doEffect {
        authn.login(Credentials(username = "u2", password = "wolfgang254!!??"))
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
