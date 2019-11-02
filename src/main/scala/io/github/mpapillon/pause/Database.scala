package io.github.mpapillon.pause

import cats.effect.{Async, ContextShift, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.chrisdavenport.log4cats.Logger
import org.flywaydb.core.Flyway

object Database {

  def transactor[F[_]: Async](
      dbConf: Configuration.Database
  )(implicit C: ContextShift[F]): Resource[F, HikariTransactor[F]] =
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

  def migrate[F[_]: Sync: Logger](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { ds =>
      for {
        _ <- Logger[F].info("Starting Flyway database migration")
        _ <- Sync[F].delay {
              val flyWay = Flyway.configure().dataSource(ds).load()
              flyWay.migrate()
            }
      } yield ()
    }
}
