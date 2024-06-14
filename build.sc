import mill._, scalalib._, scalajslib._

import $repo.`https://oss.sonatype.org/content/repositories/snapshots`
// import $repo.`https://oss.sonatype.org/content/repositories/public`

import $ivy.`com.github.cornerman::mill-db-codegen:0.4.1+2-918d6203-SNAPSHOT`, dbcodegen.plugin._

import mill.scalajslib._
import mill.scalajslib.api._

trait AppScalaModule extends ScalaModule {
  def scalaVersion = "3.4.1"
  val versions = new {
    val authn    = "0.1.2"
    val colibri  = "0.8.4"
    val outwatch = "1.0.0"
  }
  def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect::3.5.4",
    ivy"com.github.rssh::dotty-cps-async::0.9.21",
    ivy"com.github.rssh::cps-async-connect-cats-effect::0.9.21",
  )
  def scalacOptions = T { super.scalacOptions() ++ Seq("-Wunused:imports") }
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule {
  def scalaJSVersion = "1.16.0"
}

object frontend extends AppScalaJSModule {
  def moduleKind       = ModuleKind.ESModule
  def moduleSplitStyle = ModuleSplitStyle.SmallModulesFor(List("frontend"))

  def moduleDeps = Seq(rpc.js)
  def ivyDeps = Agg(
    ivy"io.github.outwatch::outwatch::${versions.outwatch}",
    ivy"com.github.cornerman::colibri::${versions.colibri}",
    ivy"com.github.cornerman::colibri-reactive::${versions.colibri}",
    ivy"com.github.cornerman::colibri-fs2::${versions.colibri}",
    ivy"org.http4s::http4s-dom::0.2.11",
    ivy"com.github.cornerman::sloth-http4s-client::0.7.3",
    ivy"com.github.cornerman::keratin-authn-frontend::${versions.authn}",
  )

  def scalacOptions = T {
    // vite serves source maps from the out-folder. Fix the relative path to the source files:
    super.scalacOptions() ++ Seq(s"-scalajs-mapSourceURI:${T.workspace.toIO.toURI}->../../../.")
  }
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
    ivy"com.augustnagro::magnum::1.1.1", // db access
    ivy"com.github.cornerman::sloth-http4s-server::0.7.3",
    ivy"org.http4s::http4s-ember-server::0.23.24",
    ivy"org.http4s::http4s-dsl::0.23.24",
    ivy"com.outr::scribe-slf4j2::3.13.0",  // logging
    ivy"org.flywaydb:flyway-core::10.6.0", // migrations
    ivy"com.github.cornerman::keratin-authn-backend::${versions.authn}",
  )
}

object rpc extends Module {
  trait SharedModule extends AppScalaModule with PlatformScalaModule {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.github.cornerman::sloth::0.7.1", // rpc
      ivy"com.lihaoyi::upickle::3.3.1",        // json and msgpack
    )
  }
  object jvm extends SharedModule
  object js  extends SharedModule with AppScalaJSModule
}
