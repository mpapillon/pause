package io.github.mpapillon.pause

import cats.effect.{Async, ContextShift, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.zaxxer.hikari.HikariDataSource
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.github.mpapillon.pause.Database.DataSourceTransactor
import javax.sql.DataSource
import org.flywaydb.core.Flyway

trait Database[F[_], A <: DataSource] {

  def transactor: Resource[F, DataSourceTransactor[F, A]]
  def migrate(transactor: DataSourceTransactor[F, A]): F[Unit]
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

      override def migrate(transactor: DataSourceTransactor[F, HikariDataSource]): F[Unit] =
        transactor.configure { ds =>
          for {
            logger <- Slf4jLogger.create[F]
            _      <- logger.info("Starting Flyway database migration")
            _      <- Sync[F].delay {
                       val flyWay = Flyway.configure().dataSource(ds).load()
                       flyWay.migrate()
                     }
          } yield ()
        }
    }
}
