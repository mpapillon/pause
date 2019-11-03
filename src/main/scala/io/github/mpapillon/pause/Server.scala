package io.github.mpapillon.pause

import java.util.concurrent.Executors.newCachedThreadPool

import cats.effect._
import cats.syntax.functor._
import fs2.Stream
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

  private def stream(blockingEc: ExecutionContext): Stream[IO, ExitCode] =
    for {
      client <- BlazeClientBuilder[IO](blockingEc).stream
      conf   <- Stream.eval(Configuration.load[IO]())

      db = Database.impl[IO](conf.db)
      xa <- Stream.resource(db.transactor)
      _  <- Stream.eval(db.migrate(xa))

      membersRepo = MembersRepository.impl(xa)
      teamRepo    = TeamsRepository.impl(xa)

      membersAlg = Members.impl(membersRepo)
      teamsAlg   = Teams.impl(teamRepo, membersRepo)
      jokeAlg    = Jokes.impl(client)

      implicit0(dsl: Http4sDsl[IO]) = new Http4sDsl[IO] {}

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

  def run(args: List[String]): IO[ExitCode] = {
    val blockingEc = ExecutionContext.fromExecutor(newCachedThreadPool())
    stream(blockingEc).compile.drain.as(ExitCode.Success)
  }

}
