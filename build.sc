import mill._, scalalib._, scalajslib._
import $ivy.`io.github.nafg.millbundler::jsdeps::0.2.0`, io.github.nafg.millbundler.jsdeps._
import $ivy.`com.github.cornerman::mill-quillcodegen:0.2.0`, quillcodegen.plugin.QuillCodegenModule

import mill.scalajslib._
import mill.scalajslib.api._

trait AppScalaModule extends ScalaModule {
  def scalaVersion = "3.4.1"
  def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect::3.5.4",
    ivy"com.github.rssh::dotty-cps-async::0.9.21",
    ivy"com.github.rssh::cps-async-connect-cats-effect::0.9.21",
  )
  def scalacOptions = Seq(
    // TODO: https://github.com/zio/zio-quill/issues/2639
    "-Wconf:msg=Questionable row-class found:s"
  )
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule {
  def scalaJSVersion = "1.16.0"
}

object frontend extends AppScalaJSModule {
  def moduleKind       = ModuleKind.ESModule
  def moduleSplitStyle = ModuleSplitStyle.SmallModulesFor(List("frontend"))

  def moduleDeps = Seq(rpc.js)
  def ivyDeps = Agg(
    ivy"io.github.outwatch::outwatch::1.0.0",
    ivy"com.github.cornerman::sloth::0.7.1",
    ivy"com.github.rssh::dotty-cps-async::0.9.21",
    ivy"com.github.rssh::cps-async-connect-cats-effect::0.9.21",
    ivy"com.github.cornerman::http4s-jsoniter::0.1.1",
  )
}

object backend extends AppScalaModule with QuillCodegenModule {
  def quillcodegenPackagePrefix = "dbtypes"
  def quillcodegenJdbcUrl       = s"jdbc:sqlite:data_codegen.db"
  // def quillcodegenJdbcUrl = T { s"jdbc:sqlite:" + T.dest + "/tmp.db" }
  def dbSchemaFile = T.source(os.pwd / "schema.sql")
  def quillcodegenSetupTask = T.task {
    val dbpath = quillcodegenJdbcUrl.stripPrefix("jdbc:sqlite:")
    os.remove(os.pwd / dbpath)
    executeSqlFile(dbSchemaFile())
  }

  def moduleDeps = Seq(rpc.jvm)
  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.xerial:sqlite-jdbc::3.44.1.0",
    ivy"io.getquill::quill-doobie::4.8.1",
    ivy"org.tpolecat::doobie-core::1.0.0-RC5",
    ivy"com.github.cornerman::sloth::0.7.1",
    ivy"com.github.rssh::dotty-cps-async::0.9.21",
    ivy"com.github.rssh::cps-async-connect-cats-effect::0.9.21",
    ivy"org.http4s::http4s-ember-server::0.23.24",
    ivy"org.http4s::http4s-dsl::0.23.24",
    ivy"com.github.cornerman::http4s-jsoniter::0.1.1",
    ivy"com.outr::scribe-slf4j2::3.13.0", // logging
  )
}

object rpc extends Module {
  trait SharedModule extends AppScalaModule with PlatformScalaModule {
    def ivyDeps = Agg(
      ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::2.28.4",
      ivy"org.typelevel::cats-effect::3.5.4",
    )
    def compileIvyDeps = Agg(
      ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::2.28.4"
    )
  }
  object jvm extends SharedModule
  object js  extends SharedModule with AppScalaJSModule
}
