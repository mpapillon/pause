package io.github.mpapillon.pause

import cats.effect.{Async, ContextShift, Resource, Sync}
import cats.syntax.monadError._
import com.zaxxer.hikari.HikariDataSource
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException

trait Database[F[_], A] {

  def transactor: Resource[F, Transactor.Aux[F, A]]
  def migrate(): F[Int]
  def validate(): F[Unit]
}

object Database {

  final case class DatabaseValidationError(message: String) extends RuntimeException(message)

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

      private val flyway: Resource[F, Flyway] =
        transactor.map(_.kernel).map(ds => Flyway.configure().dataSource(ds).load())

      override def migrate(): F[Int] =
        flyway.use(fw => Sync[F].delay(fw.migrate()))

      override def validate(): F[Unit] =
        flyway.use(fw => Sync[F].delay(fw.validate())).adaptError {
          case ex: FlywayException => DatabaseValidationError(ex.getLocalizedMessage)
        }
    }
}
