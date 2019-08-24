package io.github.mpapillon.pause.model

import java.time.LocalDate

import io.chrisdavenport.fuuid.FUUID
import io.circe.Encoder

final case class Team(id: FUUID, name: String, canonicalName: String, creationDate: LocalDate)

object Team {
  import io.chrisdavenport.fuuid.circe._
  import io.circe.generic.semiauto._

  implicit val teamEncoder: Encoder[Team] = deriveEncoder[Team]
}
