package io.github.mpapillon.pause.repositories

import cats.effect.Async
import doobie.util.transactor.Transactor
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.{Person, Team}
import io.github.mpapillon.pause.queries.TeamsQueries
import io.github.mpapillon.pause.repositories.RepositoryError.handleSqlState

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

  def impl[F[_]: Async](xa: Transactor[F]): TeamsRepository[F] = new TeamsRepository[F] {
    import doobie.implicits._

    override def findAll(): F[Vector[Team]] =
      TeamsQueries.findAll.to[Vector].transact(xa)

    override def findByName(canonicalName: String): F[Option[Team]] =
      TeamsQueries.findByName(canonicalName).option.transact(xa)

    override def findMembers(teamId: FUUID): F[Vector[Person.Member]] =
      TeamsQueries.findMembers(teamId).to[Vector].transact(xa)

    override def findManagers(teamId: FUUID): F[Vector[Person.Manager]] =
      TeamsQueries.findManagers(teamId).to[Vector].transact(xa)

    override def insert(team: Team): F[Either[RepositoryError, Int]] =
      TeamsQueries
        .insert(team.id, team.name, team.canonicalName, team.creationDate)
        .run
        .attemptSomeSqlState(handleSqlState)
        .transact(xa)

    override def insertMember(teamId: FUUID, memberId: FUUID): F[Either[RepositoryError, Int]] =
      TeamsQueries.insertMembers
        .toUpdate0((teamId, memberId))
        .run
        .attemptSomeSqlState(handleSqlState)
        .transact(xa)

    override def deleteMember(teamId: FUUID, memberId: FUUID): F[Int] =
      TeamsQueries.deleteMembers.toUpdate0((teamId, memberId)).run.transact(xa)
  }

}
