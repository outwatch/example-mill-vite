package frontend

import outwatch._
import outwatch.dsl._
import cats.effect.SyncIO

object Main {
  def main(args: Array[String]): Unit = {

    val myComponent = div("Hello World")

    Outwatch.renderReplace[SyncIO]("#app", myComponent).unsafeRunSync()
  }
}
