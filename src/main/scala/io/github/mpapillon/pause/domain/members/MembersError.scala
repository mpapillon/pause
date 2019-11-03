package io.github.mpapillon.pause.domain.members

import io.chrisdavenport.fuuid.FUUID

sealed trait MembersError

object MembersError {

  final case class MemberAlreadyExist(memberID: FUUID) extends MembersError
  final case class MemberNotFound(id: FUUID)           extends MembersError
}
