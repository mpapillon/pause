package io.github.mpapillon.pause.domain.member

import io.chrisdavenport.fuuid.FUUID

sealed trait MembersError

object MembersError {

  final case class MemberAlreadyExist(memberID: FUUID) extends MembersError
  final case class MemberNotFound(id: FUUID)           extends MembersError
}
