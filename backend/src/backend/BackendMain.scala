package backend

import cats.effect.{IO, IOApp}
import cps.*
import cps.monads.catsEffect.{*, given}

val jdbcUrl = "jdbc:sqlite:data.db?foreign_keys=ON"

object BackendMain extends IOApp.Simple {
  val minimumLevel = Option(System.getenv("LOG_LEVEL"))
  scribe.Logger.system.installJUL()
  scribe.Logger.root.withMinimumLevel(minimumLevel.fold(scribe.Level.Info)(scribe.Level.apply)).replace()

  def run: IO[Unit] = async[IO] {
    println("backend started.")
    val appConfig = AppConfig.fromEnv()
    // println("migrating")
    // await(DbMigrations.migrate(jdbcUrl))

//    Woo.runQueryBench()

    await(HttpServer.start(appConfig))
  }
}

//object Woo {
//  val datasource = SQLiteDataSource().tap(_.setUrl(jdbcUrl))
//   val connection = datasource.getConnection()
//  // val ctx        = io.getquill.SqliteJdbcContext(Literal, datasource)
//  // import ctx._
//
//  import backend.db.schema.*
//
//  def runQueryBench() = {
//    transact(datasource) {
//      println("starting")
//      val n = 1000
//      // inline def query = quote { sql"select 1".as[io.getquill.Query[Int]] }
////       val queryString  = ctx.translate(query)
////       val q            = connection.prepareStatement("INSERT INTO myids (id) values (7)")
//      val query = sql"INSERT INTO myids (id) values (7)".update
////      val query = sql"select 1".query[Int]
//      val start = System.nanoTime()
//      for (i <- 1 to n) {
//         query.run()
////        FooRepo.insert(Foo.Creator("heinz"))
//        // await(run(FooDao.query).transact(xa))
//        // //
//        // run(MyidsDao.query.insertValue(Myids(lift(i))))
//        // run(query) // 0.7ms
////         q.executeQuery() // 0.02ms
//        // val foo = ctx.prepare(quote { sql"select 1".as[io.getquill.Query[Int]] })
//        // foo(connection) // 0.2ms
//      }
////      for (i <- 1.to(n)) {
////
////        // await(run(FooDao.query).transact(xa))
////        // //
////        // run(MyidsDao.query.insertValue(Myids(lift(i))))
////        // run(query) // 0.7ms
////        // q.executeQuery() // 0.02ms
////        // val foo = ctx.prepare(quote { sql"select 1".as[io.getquill.Query[Int]] })
////        // foo(connection) // 0.2ms
////      }
//      val end = System.nanoTime()
//      println("finished: " + ((end - start) / n.toDouble / 1000000) + "ms per query")
//    }
//  }
//}
