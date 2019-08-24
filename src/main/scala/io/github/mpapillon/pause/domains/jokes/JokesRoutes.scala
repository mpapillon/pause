package io.github.mpapillon.pause.domains.jokes

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object JokesRoutes {

  def routes[F[_]: Sync: Jokes]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}; import dsl._
    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          joke <- Jokes[F].get
          resp <- Ok(joke)
        } yield resp
    }
  }
}
