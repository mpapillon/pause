package io.github.mpapillon.pause.model

import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.circe._
import io.circe.Encoder
import io.circe.generic.semiauto._

final case class Person(id: FUUID, firstName: String, lastName: String, email: Option[String])

object Person {

  implicit val personEncoder: Encoder[Person] = deriveEncoder
}
