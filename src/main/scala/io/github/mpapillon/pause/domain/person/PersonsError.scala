package io.github.mpapillon.pause.domain.person

import io.chrisdavenport.fuuid.FUUID

sealed trait PersonsError

object PersonsError {

  final case class PersonAlreadyExist(personId: FUUID) extends PersonsError
  final case class PersonNotFound(personId: FUUID)     extends PersonsError
}
