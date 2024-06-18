package backend

import backend.BackendMain.CliArg
import cats.effect.{ExitCode, IO, IOApp}
import cps.*
import cps.monads.catsEffect.{*, given}

object BackendMain extends IOApp {
  val minimumLevel = Option(System.getenv("LOG_LEVEL"))
  scribe.Logger.root.withMinimumLevel(minimumLevel.fold(scribe.Level.Info)(scribe.Level.apply)).replace()

  enum CliArg { case HttpServer, Migrate, RepairMigrations }

  def run(args: List[String]): IO[ExitCode] = async[IO] {
    println("backend started.")

    val cliArgs   = args.map(CliArg.valueOf).toSet
    val appConfig = AppConfig.fromEnv()

    if (cliArgs(CliArg.RepairMigrations)) {
      println("repairing migrations")
      await(DbMigrations.repair(appConfig.dataSource))
    }

    if (cliArgs(CliArg.Migrate)) {
      println("migrating")
      await(DbMigrations.migrate(appConfig.dataSource))
    }

    if (cliArgs.isEmpty || cliArgs(CliArg.HttpServer)) {
      println("starting http server")
      await(HttpServer.start(appConfig))
    }

    ExitCode.Success
  }

}
