package io.github.mpapillon.pause

import cats.effect.{Async, ContextShift, Resource, Sync}
import com.zaxxer.hikari.HikariDataSource
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.github.mpapillon.pause.Database.DataSourceTransactor
import javax.sql.DataSource
import org.flywaydb.core.Flyway

import scala.util.Try

trait Database[F[_], A <: DataSource] {

  def transactor: Resource[F, DataSourceTransactor[F, A]]
  def migrate(): F[Int]
}

object Database {

  type DataSourceTransactor[M[_], A <: DataSource] = Transactor.Aux[M, A]

  def impl[F[_]: Async](
      dbConf: Configuration.Database
  )(implicit C: ContextShift[F]): Database[F, HikariDataSource] =
    new Database[F, HikariDataSource] {

      override lazy val transactor: Resource[F, DataSourceTransactor[F, HikariDataSource]] =
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
