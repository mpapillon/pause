package io.github.mpapillon.pause.model

import io.chrisdavenport.fuuid.FUUID
import io.circe.Encoder

sealed trait Person

object Person {
  import io.chrisdavenport.fuuid.circe._
  import io.circe.generic.semiauto._

  final case class Member(id: FUUID, firstName: String, lastName: String, email: Option[String])  extends Person
  final case class Manager(id: FUUID, firstName: String, lastName: String, email: Option[String]) extends Person

  implicit val memberEncoder: Encoder[Member]   = deriveEncoder[Member]
  implicit val managerEncoder: Encoder[Manager] = deriveEncoder[Manager]
}
