package backend

import cats.effect.IO
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DbMigrations {
  // https://documentation.red-gate.com/flyway/flyway-cli-and-api/usage/api-java

  def migrate(dataSource: DataSource): IO[Unit] = IO.blocking {
    val flyway = defaultFlyway(dataSource).load()
    flyway.migrate()
  }.void

  def repair(dataSource: DataSource): IO[Unit] = IO.blocking {
    val flyway = defaultFlyway(dataSource).load()
    flyway.repair()
  }.void

  private def defaultFlyway(dataSource: DataSource) =
    Flyway.configure().dataSource(dataSource).locations("classpath:migrations").failOnMissingLocations(true)
}
