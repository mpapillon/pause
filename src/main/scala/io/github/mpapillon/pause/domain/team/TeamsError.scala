package io.github.mpapillon.pause.domain.team

import java.util.UUID

import io.github.mpapillon.pause.model.Slug

sealed trait TeamsError

object TeamsError {
  final case class TeamNotFound(slug: Slug)                       extends TeamsError
  final case class TeamAlreadyExists(slug: Slug)                  extends TeamsError
  final case class PersonNotFound(id: UUID)                      extends TeamsError
  final case class MembershipAlreadyExists(slug: Slug, id: UUID) extends TeamsError
  final case class MembershipDoesNotExists(slug: Slug, id: UUID) extends TeamsError
}
