import mill._, scalalib._, scalajslib._
import $ivy.`io.github.nafg.millbundler::jsdeps::0.2.0`,
  io.github.nafg.millbundler.jsdeps._

import mill.scalajslib._
import mill.scalajslib.api._

object app extends RootModule {
  object frontend extends ScalaJSModule {
    def scalaVersion = "3.4.1"
    def scalaJSVersion = "1.16.0"

    def moduleKind = ModuleKind.ESModule
    def moduleSplitStyle = ModuleSplitStyle.SmallModulesFor(List("frontend"))

    def ivyDeps = Agg(
      ivy"io.github.outwatch::outwatch::1.0.0"
    )
  }
}
