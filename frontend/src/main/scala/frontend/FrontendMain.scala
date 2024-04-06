package frontend

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

@main def main() = {
  val myComponent = div("Hello World")

  // render the component into the <div id="app"></div> in index.html
  Outwatch.renderReplace[SyncIO]("#app", myComponent).unsafeRunSync()
}
