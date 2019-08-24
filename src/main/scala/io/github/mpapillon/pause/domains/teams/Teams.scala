package io.github.mpapillon.pause.domains.teams

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.{Person, Team}

trait Teams[F[_]] {

  def all: F[Vector[Team]]
  def add(team: Team): F[Either[TeamsError, Unit]]
  def get(canonicalName: String): F[Option[Team]]
  def membersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Member]]]
  def managersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Manager]]]
  def join(canonicalName: String, membersID: FUUID): F[Either[TeamsError, Unit]]
  def leave(canonicalName: String, membersID: FUUID): F[Either[TeamsError, Unit]]
}

object Teams {
  implicit def apply[F[_]](implicit ev: Teams[F]): Teams[F] = ev
}
