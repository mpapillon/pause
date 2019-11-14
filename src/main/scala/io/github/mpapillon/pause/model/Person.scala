package io.github.mpapillon.pause.model

import java.util.UUID

import io.circe.Encoder
import io.circe.generic.semiauto._

final case class Person(id: UUID, firstName: String, lastName: String, email: Option[String])

object Person {

  implicit val personEncoder: Encoder[Person] = deriveEncoder
}
