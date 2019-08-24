package io.github.mpapillon.pause.repositories

import io.chrisdavenport.fuuid.FUUID
import RepositoryError.handleSqlState
import io.github.mpapillon.pause.model.{Person, Team}
import io.github.mpapillon.pause.queries.TeamsQueries

trait TeamsRepository[F[_]] {

  def findAll(): F[Vector[Team]]
  def findByName(canonicalName: String): F[Option[Team]]
  def findMembers(teamId: FUUID): F[Vector[Person.Member]]
  def findManagers(teamId: FUUID): F[Vector[Person.Manager]]
  def insert(team: Team): F[Either[RepositoryError, Int]]
  def insertMember(teamId: FUUID, memberId: FUUID): F[Either[RepositoryError, Int]]
  def deleteMember(teamId: FUUID, memberId: FUUID): F[Int]
}

object TeamsRepository {

  def impl: TeamsRepository[doobie.ConnectionIO] = new TeamsRepository[doobie.ConnectionIO] {
    import doobie.implicits._

    override def findAll(): doobie.ConnectionIO[Vector[Team]] =
      TeamsQueries.findAll.to[Vector]

    override def findByName(canonicalName: String): doobie.ConnectionIO[Option[Team]] =
      TeamsQueries.findByName(canonicalName).option

    override def findMembers(teamId: FUUID): doobie.ConnectionIO[Vector[Person.Member]] =
      TeamsQueries.findMembers(teamId).to[Vector]

    override def findManagers(teamId: FUUID): doobie.ConnectionIO[Vector[Person.Manager]] =
      TeamsQueries.findManagers(teamId).to[Vector]

    override def insert(team: Team): doobie.ConnectionIO[Either[RepositoryError, Int]] =
      TeamsQueries
        .insert(team.id, team.name, team.canonicalName, team.creationDate)
        .run
        .attemptSomeSqlState(handleSqlState)

    override def insertMember(teamId: FUUID, memberId: FUUID): doobie.ConnectionIO[Either[RepositoryError, Int]] =
      TeamsQueries.insertMembers
        .toUpdate0((teamId, memberId))
        .run
        .attemptSomeSqlState(handleSqlState)

    override def deleteMember(teamId: FUUID, memberId: FUUID): doobie.ConnectionIO[Int] =
      TeamsQueries.deleteMembers.toUpdate0((teamId, memberId)).run
  }

}
