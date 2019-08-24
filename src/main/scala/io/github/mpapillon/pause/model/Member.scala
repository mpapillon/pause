package io.github.mpapillon.pause.model

import io.chrisdavenport.fuuid.FUUID
import io.circe.Encoder

final case class Member(id: FUUID, firstName: String, lastName: String, email: Option[String])

object Member {
  import io.chrisdavenport.fuuid.circe._
  import io.circe.generic.semiauto._

  implicit val memberEncoder: Encoder[Member] = deriveEncoder[Member]
}
