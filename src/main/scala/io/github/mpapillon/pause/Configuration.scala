package io.github.mpapillon.pause

import cats.effect.Sync
import org.http4s.Uri
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import pureconfig.module.http4s._

final case class Configuration(db: Configuration.Database, port: Int)

object Configuration {

  final case class Database(host: Uri, user: String, password: String)

  def load[F[_]: Sync](): F[Configuration] = loadConfigF[F, Configuration]("pause")
}
