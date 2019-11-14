package io.github.mpapillon

import java.time.Instant

import cats.Applicative
import cats.effect.Clock

import scala.concurrent.duration.{MILLISECONDS, TimeUnit}

package object pause {

  def fixedClock[F[_]: Applicative](instant: Instant): Clock[F] = new Clock[F] {

    override def realTime(unit: TimeUnit): F[Long] =
      Applicative[F].pure(unit.convert(instant.toEpochMilli, MILLISECONDS))

    override def monotonic(unit: TimeUnit): F[Long] =
      Applicative[F].pure(unit.convert(instant.toEpochMilli, MILLISECONDS))
  }
}
