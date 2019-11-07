package io.github.mpapillon.pause.domain.joke

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object JokesService {

  def apply[F[_]: Sync](jokes: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}; import dsl._
    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          joke <- jokes.get
          resp <- Ok(joke)
        } yield resp
    }
  }
}
