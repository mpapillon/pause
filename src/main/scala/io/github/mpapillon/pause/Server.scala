package io.github.mpapillon.pause

import java.util.concurrent.Executors.newCachedThreadPool

import cats.effect._
import cats.syntax.functor._
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.github.mpapillon.pause.domain.jokes.{Jokes, JokesRoutes}
import io.github.mpapillon.pause.domain.members.{Members, MembersRoutes}
import io.github.mpapillon.pause.domain.teams.{Teams, TeamsRoutes}
import io.github.mpapillon.pause.repository.{MembersRepository, TeamsRepository}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{Logger => HttpLogger}

import scala.concurrent.ExecutionContext

object Server extends IOApp {

  private val stream: Stream[IO, ExitCode] =
    for {
      implicit0(log: Logger[IO])    <- Stream.eval(Slf4jLogger.fromClass[IO](this.getClass))
      implicit0(dsl: Http4sDsl[IO]) = new Http4sDsl[IO] {}
      blockingEc: ExecutionContext  = ExecutionContext.fromExecutor(newCachedThreadPool())

      client <- BlazeClientBuilder[IO](blockingEc).stream
      conf   <- Stream.eval(Configuration.load[IO]())
      xa     <- Stream.resource(Database.transactor[IO](conf.db))
      _      <- Stream.eval(Database.migrate(xa))

      membersRepo = MembersRepository.impl(xa)
      teamRepo    = TeamsRepository.impl(xa)

      membersAlg = Members.impl(membersRepo)
      teamsAlg   = Teams.impl(teamRepo, membersRepo)
      jokeAlg    = Jokes.impl(client)

      router = Router(
        "/api/v1" -> Router(
          "/members" -> MembersRoutes(membersAlg),
          "/teams"   -> TeamsRoutes(teamsAlg),
          "/joke"    -> JokesRoutes(jokeAlg)
        )
      ).orNotFound

      httpApp = HttpLogger.httpApp(logHeaders = true, logBody = true)(router)
      exitCode <- BlazeServerBuilder[IO]
                   .bindHttp(conf.port, "0.0.0.0")
                   .withHttpApp(httpApp)
                   .serve
    } yield exitCode

  def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)

}
