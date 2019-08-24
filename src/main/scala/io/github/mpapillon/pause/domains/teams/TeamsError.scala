package io.github.mpapillon.pause.domains.teams

import io.chrisdavenport.fuuid.FUUID

sealed trait TeamsError

object TeamsError {
  final case class TeamNotFound(canonicalName: String)                       extends TeamsError
  final case class TeamAlreadyExists(canonicalName: String)                  extends TeamsError
  final case class MemberNotFound(id: FUUID)                                 extends TeamsError
  final case class MembershipAlreadyExists(canonicalName: String, id: FUUID) extends TeamsError
  final case class MembershipDoesNotExists(canonicalName: String, id: FUUID) extends TeamsError
}
