package io.github.mpapillon.pause.domain.team

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Slug

sealed trait TeamsError

object TeamsError {
  final case class TeamNotFound(slug: Slug)                       extends TeamsError
  final case class TeamAlreadyExists(slug: Slug)                  extends TeamsError
  final case class MemberNotFound(id: FUUID)                      extends TeamsError
  final case class MembershipAlreadyExists(slug: Slug, id: FUUID) extends TeamsError
  final case class MembershipDoesNotExists(slug: Slug, id: FUUID) extends TeamsError
}
