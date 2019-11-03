package io.github.mpapillon.pause.repository

import doobie.enum.SqlState
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

sealed trait RepositoryError

object RepositoryError {

  final case object UniqueViolationConstraintError extends RepositoryError

  def handleSqlState: PartialFunction[SqlState, RepositoryError] = {
    case UNIQUE_VIOLATION => UniqueViolationConstraintError
  }
}
