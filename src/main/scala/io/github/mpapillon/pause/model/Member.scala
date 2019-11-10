package io.github.mpapillon.pause.model

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class Member(person: Person, isManager: Boolean)

object Member {

  implicit val memberEncoder: Encoder[Member] = deriveEncoder
}
