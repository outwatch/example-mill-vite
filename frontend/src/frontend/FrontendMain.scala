package frontend

import cats.effect.IO
import cats.effect.IOApp
import outwatch.*
import outwatch.dsl.*

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

object Main extends IOApp.Simple {
  def run = {
    val myComponent = div(
      "Hello World, rpc: ",
      RpcClient.call.fun(5),
    )

    // render the component into the <div id="app"></div> in index.html
    Outwatch.renderReplace[IO]("#app", myComponent)
  }
}
