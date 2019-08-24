package io.github.mpapillon.pause.domains.members

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member

trait Members[F[_]] {

  def all: F[Vector[Member]]
  def get(memberID: FUUID): F[Option[Member]]
  def add(member: Member): F[Either[MembersError, Unit]]
  def remove(memberID: FUUID): F[Int]
}

object Members {

  implicit def apply[F[_]](implicit ev: Members[F]): Members[F] = ev
}
