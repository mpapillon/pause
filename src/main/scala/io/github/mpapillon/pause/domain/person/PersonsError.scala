package io.github.mpapillon.pause.domain.person

import java.util.UUID

sealed trait PersonsError

object PersonsError {

  final case class PersonAlreadyExist(personId: UUID) extends PersonsError
  final case class PersonNotFound(personId: UUID)     extends PersonsError
}
