package io.github.mpapillon.pause

import java.util.concurrent.Executors.newCachedThreadPool

import cats.effect._
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.github.mpapillon.pause.domains.jokes.{Jokes, JokesRoutes}
import io.github.mpapillon.pause.domains.members.{Members, MembersRoutes, MembersService}
import io.github.mpapillon.pause.repositories.{MembersRepository, TeamsRepository}
import io.github.mpapillon.pause.domains.teams.{Teams, TeamsRoutes, TeamsService}
import org.flywaydb.core.Flyway
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{Logger => HttpLogger}

import scala.concurrent.ExecutionContext

object PauseServer {

  private def transactor[F[_]: Async](
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
             te // execute JDBC operations here
           )
    } yield xa

  private def migrate[F[_]: Sync: Logger](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { ds =>
      for {
        _ <- Logger[F].info("Starting Flyway database migration")
        _ <- Sync[F].delay {
              val flyWay = Flyway.configure().dataSource(ds).load()
              flyWay.migrate()
            }
      } yield ()
    }

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, ExitCode] =
    for {
      implicit0(log: Logger[F])    <- Stream.eval(Slf4jLogger.fromClass(this.getClass))
      implicit0(dsl: Http4sDsl[F]) = new Http4sDsl[F] {}
      blockingEc: ExecutionContext = ExecutionContext.fromExecutor(newCachedThreadPool())

      client <- BlazeClientBuilder[F](blockingEc).stream
      conf   <- Stream.eval(Configuration.load[F])
      xa     <- Stream.resource(transactor(conf.db))
      _      <- Stream.eval(migrate(xa))

      membersRepo = MembersRepository.impl
      teamRepo    = TeamsRepository.impl

      implicit0(membersAlg: Members[F]) = MembersService.impl[F](membersRepo, xa)
      implicit0(teamsAlg: Teams[F])     = TeamsService.impl[F](teamRepo, membersRepo, xa)
      implicit0(jokeAlg: Jokes[F])      = Jokes.impl[F](client)

      httpApp = Router(
        "/api/v1" -> Router(
          "/members" -> MembersRoutes.routes,
          "/teams"   -> TeamsRoutes.routes,
          "/joke"    -> JokesRoutes.routes
        )
      ).orNotFound

      finalHttpApp = HttpLogger.httpApp(logHeaders = true, logBody = true)(httpApp)
      exitCode <- BlazeServerBuilder[F]
                   .bindHttp(conf.port, "0.0.0.0")
                   .withHttpApp(finalHttpApp)
                   .serve
    } yield exitCode
}
