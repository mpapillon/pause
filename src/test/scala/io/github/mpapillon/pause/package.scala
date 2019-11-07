package io.github.mpapillon

import java.time.Instant

import cats.effect.{Clock, Sync}

import scala.concurrent.duration.{MILLISECONDS, TimeUnit}

package object pause {

  def fixedClock[F[_]: Sync](instant: Instant): Clock[F] = new Clock[F] {

    override def realTime(unit: TimeUnit): F[Long] =
      Sync[F].delay(unit.convert(instant.toEpochMilli, MILLISECONDS))

    override def monotonic(unit: TimeUnit): F[Long] =
      Sync[F].delay(unit.convert(instant.toEpochMilli, MILLISECONDS))
  }
}
