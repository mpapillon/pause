package io.github.mpapillon.pause

import cats.effect.{Async, ContextShift, Resource, Sync}
import com.zaxxer.hikari.HikariDataSource
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway

trait Database[F[_], A] {

  def transactor: Resource[F, Transactor.Aux[F, A]]
  def migrate(): F[Int]
}

object Database {

  def impl[F[_]: Async](
      dbConf: Configuration.Database
  )(implicit C: ContextShift[F]): Database[F, HikariDataSource] =
    new Database[F, HikariDataSource] {

      override lazy val transactor: Resource[F, HikariTransactor[F]] =
        for {
          ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
          te <- ExecutionContexts.cachedThreadPool[F]    // our transaction EC
          xa <- HikariTransactor.newHikariTransactor[F](
                 "org.postgresql.Driver",
                 dbConf.host.renderString,
                 dbConf.user,
                 dbConf.password,
                 ce, // await connection here
                 te  // execute JDBC operations here
               )
        } yield xa

      override def migrate(): F[Int] =
        transactor.use(_.configure { ds =>
          Sync[F].delay {
            val flyWay = Flyway.configure().dataSource(ds).load()
            flyWay.migrate()
          }
        })
    }
}
