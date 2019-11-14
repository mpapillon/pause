package io.github.mpapillon.pause

import java.util.concurrent.Executors.newCachedThreadPool

import cats.effect._
import fs2.Stream
import io.github.mpapillon.pause.domain.joke.{Jokes, JokesService}
import io.github.mpapillon.pause.domain.person.{Persons, PersonsService}
import io.github.mpapillon.pause.domain.team.{Teams, TeamsService}
import io.github.mpapillon.pause.repository.{PersonsRepository, TeamsRepository}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{Logger => HttpLogger}

import scala.concurrent.ExecutionContext

object Server {

  private val blockingEc = ExecutionContext.fromExecutor(newCachedThreadPool())

  def stream[F[_]: ConcurrentEffect]()(implicit cs: ContextShift[F], timer: Timer[F]): Stream[F, ExitCode] = {
    implicit val dsl: Http4sDsl[F] = Http4sDsl[F]
    for {
      client <- BlazeClientBuilder[F](blockingEc).stream
      conf   <- Stream.eval(Configuration.load[F]())

      db = Database.impl[F](conf.db)
      xa <- Stream.resource(db.transactor)
      _  <- Stream.eval(db.migrate())

      personsRepo = PersonsRepository.impl(xa)
      teamRepo    = TeamsRepository.impl(xa)

      personsAlg = Persons.impl(personsRepo)
      teamsAlg   = Teams.impl(teamRepo, personsRepo)
      jokeAlg    = Jokes.impl(client)

      router = Router(
        "/api/v1" -> Router(
          "/persons" -> PersonsService(personsAlg),
          "/teams"   -> TeamsService(teamsAlg),
          "/joke"    -> JokesService(jokeAlg)
        )
      ).orNotFound

      httpApp = HttpLogger.httpApp(logHeaders = true, logBody = true)(router)
      exitCode <- BlazeServerBuilder[F]
                   .withBanner(Nil)
                   .bindHttp(conf.port, "0.0.0.0")
                   .withHttpApp(httpApp)
                   .serve
    } yield exitCode
  }

}
