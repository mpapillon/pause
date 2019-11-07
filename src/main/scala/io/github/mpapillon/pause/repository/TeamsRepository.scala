package io.github.mpapillon.pause.repository

import java.time.LocalDate

import cats.effect.Async
import doobie.util.transactor.Transactor
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.{Person, Slug, Team}
import io.github.mpapillon.pause.repository.RepositoryError.{Result, handleSqlState}
import io.github.mpapillon.pause.repository.TeamsRepository.TeamId
import io.github.mpapillon.pause.repository.query.TeamsQueries

trait TeamsRepository[F[_]] {

  def findAll(): F[Vector[Team]]
  def findBySlug(slug: Slug): F[Option[Team]]
  def findMembers(teamId: TeamId): F[Vector[Person.Member]]
  def findManagers(teamId: TeamId): F[Vector[Person.Manager]]
  def insert(name: String, slug: Slug, creationDate: LocalDate): F[Result[TeamId]]
  def insertMember(teamId: TeamId, memberId: FUUID): F[Result[Int]]
  def deleteMember(teamId: TeamId, memberId: FUUID): F[Int]
}

object TeamsRepository {

  type TeamId = Int

  def impl[F[_]: Async](xa: Transactor[F]): TeamsRepository[F] = new TeamsRepository[F] {
    import doobie.implicits._

    override def findAll(): F[Vector[Team]] =
      TeamsQueries.findAll.to[Vector].transact(xa)

    override def findBySlug(slug: Slug): F[Option[Team]] =
      TeamsQueries.findBySlug(slug).option.transact(xa)

    override def findMembers(teamId: TeamId): F[Vector[Person.Member]] =
      TeamsQueries.findMembers(teamId).to[Vector].transact(xa)

    override def findManagers(teamId: TeamId): F[Vector[Person.Manager]] =
      TeamsQueries.findManagers(teamId).to[Vector].transact(xa)

    override def insert(
        name: String,
        slug: Slug,
        creationDate: LocalDate
    ): F[Result[TeamId]] =
      TeamsQueries
        .insert(name, slug, creationDate)
        .withUniqueGeneratedKeys[TeamId]("team_id")
        .attemptSomeSqlState(handleSqlState)
        .transact(xa)

    override def insertMember(teamId: TeamId, memberId: FUUID): F[Result[Int]] =
      TeamsQueries.insertMembers
        .toUpdate0(teamId -> memberId)
        .run
        .attemptSomeSqlState(handleSqlState)
        .transact(xa)

    override def deleteMember(teamId: TeamId, memberId: FUUID): F[Int] =
      TeamsQueries.deleteMembers.toUpdate0(teamId -> memberId).run.transact(xa)
  }

}
