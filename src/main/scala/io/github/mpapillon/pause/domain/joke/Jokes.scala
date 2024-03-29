package io.github.mpapillon.pause.domain.joke

import cats.Applicative
import cats.effect.Sync
import cats.syntax.monadError._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

trait Jokes[F[_]] {

  def get: F[Jokes.Joke]
}

object Jokes {

  final case class Joke(joke: String) extends AnyVal

  object Joke {
    implicit val jokeDecoder: Decoder[Joke]                                   = deriveDecoder[Joke]
    implicit def jokeEntityDecoder[F[_]: Sync]: EntityDecoder[F, Joke]        = jsonOf
    implicit val jokeEncoder: Encoder[Joke]                                   = deriveEncoder[Joke]
    implicit def jokeEntityEncoder[F[_]: Applicative]: EntityEncoder[F, Joke] = jsonEncoderOf
  }

  final case class JokeError(e: Throwable) extends RuntimeException

  def impl[F[_]: Sync](C: Client[F]): Jokes[F] = new Jokes[F] {
    private val dsl = new Http4sClientDsl[F] {}
    import dsl._

    def get: F[Jokes.Joke] = {
      C.expect[Joke](GET(uri"https://icanhazdadjoke.com/"))
        .adaptError { case t => JokeError(t) } // Prevent Client Json Decoding Failure Leaking
    }
  }
}
