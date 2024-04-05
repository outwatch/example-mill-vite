package frontend

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO
import authn.frontend._
import cats.effect.IO
import authn.frontend.authnJS.keratinAuthn.distTypesMod.Credentials
import authn.frontend.authnJS.keratinAuthn.anon.Token
import cats.effect.IOApp
import cps.*
import cps.monads.catsEffect.given

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

object Main extends IOApp.Simple {
  def run = async[IO] {
    val client = AuthnClient[IO](
      AuthnClientConfig(
        hostUrl = "http://localhost:3000",
        sessionStorage = SessionStorage.LocalStorage("session")
      )
    )
    await(client.restoreSession.voidError)

    val myComponent = div(
      button(
        "login",
        onClick.doEffect(
          client.login(Credentials(username = "ich", password = "dumididme"))
        )
      ),
      button(
        "signup",
        onClick.doEffect(
          client.signup(Credentials(username = "ich", password = "dumididme"))
        )
      ),
      div(
        "session: ",
        await(client.session.map(_.toString))
      ),
      "Hello World"
    )

    // render the component into the <div id="app"></div> in index.html
    await(Outwatch.renderReplace[IO]("#app", myComponent))
  }
}
