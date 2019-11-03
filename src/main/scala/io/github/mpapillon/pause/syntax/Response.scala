package io.github.mpapillon.pause.syntax

import cats.Monad
import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import org.http4s.Response

class EitherResponseOps[F[_]: Monad, +E, A](ma: Either[E, A]) {

  def toResponse(resp: A => F[Response[F]])(implicit errHandler: E => F[Response[F]]): F[Response[F]] =
    ma match {
      case Left(error)  => errHandler(error)
      case Right(value) => resp(value)
    }
}

class ResponseOps[F[_]: Monad, +E, A](ma: F[Either[E, A]]) {

  def toResponse(resp: A => F[Response[F]])(implicit errHandler: E => F[Response[F]]): F[Response[F]] =
    ma.flatMap {
      case Left(error)  => errHandler(error)
      case Right(value) => resp(value)
    }
}

class EitherTResponseOps[F[_]: Monad, +E, A](ma: EitherT[F, E, A]) {

  def toResponse(resp: A => F[Response[F]])(implicit errHandler: E => F[Response[F]]): F[Response[F]] =
    ma.value.flatMap {
      case Left(error)  => errHandler(error)
      case Right(value) => resp(value)
    }
}

trait ToResponseOps {

  implicit def toResponseOps[F[_]: Sync, E, A](ma: F[Either[E, A]]): ResponseOps[F, E, A] =
    new ResponseOps(ma)

  implicit def toEitherResponseOps[F[_]: Sync, E, A](ma: Either[E, A]): EitherResponseOps[F, E, A] =
    new EitherResponseOps(ma)

  implicit def toEitherTResponseOps[F[_]: Sync, E, A](ma: EitherT[F, E, A]): EitherTResponseOps[F, E, A] =
    new EitherTResponseOps(ma)
}
