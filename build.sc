import mill._, scalalib._, scalajslib._

import $repo.`https://oss.sonatype.org/content/repositories/snapshots`
import $repo.`https://oss.sonatype.org/content/repositories/public`

import $ivy.`com.github.cornerman::mill-db-codegen:0.4.1+2-918d6203-SNAPSHOT`, dbcodegen.plugin._
import $ivy.`com.github.cornerman::mill-web-components-codegen:0.0.0+14-85443756-SNAPSHOT`, webcodegen.plugin._

import mill.scalajslib._
import mill.scalajslib.api._

trait AppScalaModule extends ScalaModule {
  def scalaVersion = "3.4.1"
  val versions = new {
    val authn    = "0.1.2+8-c4db15bd-SNAPSHOT"
    val colibri  = "0.8.4"
    val outwatch = "1.0.0"
    val sloth    = "0.7.4"
  }
  def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect::3.5.4",
    ivy"com.github.rssh::dotty-cps-async::0.9.21",
    ivy"com.github.rssh::cps-async-connect-cats-effect::0.9.21",
  )
  def scalacOptions = T {
    super.scalacOptions() ++ Seq(
      "-Wunused:imports",
      // default imports in every scala file. we use the scala defaults + chaining + cps for direct syntax with lift/unlift/!
      // https://docs.scala-lang.org/overviews/compiler-options/
      "-Yimports:java.lang,scala,scala.Predef,scala.util.chaining,cps.syntax.monadless,cps.monads.catsEffect",
    )
  }
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule {
  def scalaJSVersion = "1.16.0"
}

object frontend extends AppScalaJSModule with WebCodegenModule {
  def moduleKind       = ModuleKind.ESModule
  def moduleSplitStyle = ModuleSplitStyle.SmallModulesFor(List("frontend"))

  def moduleDeps = Seq(rpc.js)
  def ivyDeps = Agg(
    ivy"io.github.outwatch::outwatch::${versions.outwatch}",
    ivy"com.github.cornerman::colibri::${versions.colibri}",
    ivy"com.github.cornerman::colibri-reactive::${versions.colibri}",
    ivy"com.github.cornerman::colibri-fs2::${versions.colibri}",
    ivy"org.http4s::http4s-dom::0.2.11",
    ivy"com.github.cornerman::sloth-http4s-client::${versions.sloth}",
    ivy"com.github.cornerman::keratin-authn-frontend::${versions.authn}",
    ivy"org.scala-js:scalajs-java-securerandom_sjs1_2.13:1.0.0",
  )

  def scalacOptions = T {
    // vite serves source maps from the out-folder. Fix the relative path to the source files:
    super.scalacOptions() ++ Seq(s"-scalajs-mapSourceURI:${T.workspace.toIO.toURI}->../../../.")
  }

  override def webcodegenCustomElements = Seq(
    webcodegen
      .CustomElements("shoelace", (os.pwd / "node_modules" / "@shoelace-style" / "shoelace" / "dist" / "custom-elements.json").toIO),
    webcodegen.CustomElements("emojipicker", (os.pwd / "node_modules" / "emoji-picker-element" / "custom-elements.json").toIO),
  )
  override def webcodegenTemplates = Seq(
    webcodegen.Template.Outwatch
  )
}

object backend extends AppScalaModule with DbCodegenModule {
  def dbTemplateFile = T.source(os.pwd / "schema.scala.ssp")
  def dbSchemaFile   = T.source(os.pwd / "schema.sql")

  def dbcodegenTemplateFiles = T { Seq(dbTemplateFile()) }
  def dbcodegenJdbcUrl       = "jdbc:sqlite:file::memory:?cache=shared"
  def dbcodegenSetupTask = T.task { (db: Db) =>
    db.executeSqlFile(dbSchemaFile())
  }

  def moduleDeps = Seq(rpc.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.xerial:sqlite-jdbc::3.46.0.0",
    ivy"com.augustnagro::magnum::1.2.0", // db access
    ivy"com.github.cornerman::sloth-http4s-server::${versions.sloth}",
    ivy"org.http4s::http4s-ember-server::0.23.24",
    ivy"org.http4s::http4s-ember-client::0.23.24",
    ivy"org.http4s::http4s-dsl::0.23.24",
    ivy"com.outr::scribe-slf4j2::3.13.0",  // logging
    ivy"org.flywaydb:flyway-core::10.6.0", // migrations
    ivy"com.github.cornerman::keratin-authn-backend::${versions.authn}",
    ivy"io.github.arainko::ducktape::0.2.1",
  )
}

object rpc extends Module {
  trait SharedModule extends AppScalaModule with PlatformScalaModule {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.github.cornerman::sloth::${versions.sloth}", // rpc
      ivy"com.lihaoyi::upickle::3.3.1",                    // json and msgpack
    )
  }
  object jvm extends SharedModule
  object js  extends SharedModule with AppScalaJSModule
}
