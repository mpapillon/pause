package io.github.mpapillon.pause.domain.team

import java.util.UUID

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.effect.Clock
import cats.implicits._
import io.chrisdavenport.cats.effect.time.implicits._
import io.github.mpapillon.pause.domain.team.TeamsError._
import io.github.mpapillon.pause.model.{Member, Slug, Team}
import io.github.mpapillon.pause.repository.{PersonsRepository, RepositoryError, TeamsRepository}
import io.github.mpapillon.pause.syntax.slug._

trait Teams[F[_]] {

  def all: F[Vector[Team]]
  def add(name: String): F[Either[TeamsError, Team]]
  def get(slug: Slug): F[Option[Team]]
  def membersOf(slug: Slug): F[Either[TeamsError, Vector[Member]]]
  def join(slug: Slug, personId: UUID): F[Either[TeamsError, Unit]]
  def leave(slug: Slug, personId: UUID): F[Either[TeamsError, Unit]]
}

object Teams {

  def impl[F[_]: Monad](
      teamsRepo: TeamsRepository[F],
      personsRepo: PersonsRepository[F]
  )(implicit clock: Clock[F]): Teams[F] = new Teams[F] {

    override def all: F[Vector[Team]] =
      teamsRepo.findAll()

    override def add(name: String): F[Either[TeamsError, Team]] =
      for {
        creationDate <- clock.getLocalDateUTC
        slug         = name.slugify
        teamId       <- teamsRepo.insert(name, slug, creationDate)
        team         = teamId.map(Team(_, name, slug, creationDate))
      } yield team.leftMap {
        case RepositoryError.UniqueViolationConstraintError => TeamAlreadyExists(slug)
      }

    override def get(slug: Slug): F[Option[Team]] =
      teamsRepo.findBySlug(slug)

    override def membersOf(slug: Slug): F[Either[TeamsError, Vector[Member]]] = {
      for {
        team    <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        members <- EitherT.right[TeamsError](teamsRepo.findMembers(team.id))
      } yield members
    }.value

    override def join(slug: Slug, personId: UUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        _    <- OptionT(personsRepo.findById(personId)).toRight(PersonNotFound(personId))
        _ <- EitherT(teamsRepo.insertMember(team.id, personId)).leftMap[TeamsError] {
              case RepositoryError.UniqueViolationConstraintError =>
                MembershipAlreadyExists(slug, personId)
            }
      } yield ()
    }.value

    override def leave(slug: Slug, personId: UUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        _    <- OptionT(personsRepo.findById(personId)).toRight(PersonNotFound(personId))
        _ <- EitherT
              .right[TeamsError](teamsRepo.deleteMember(team.id, personId))
              .subflatMap(nb => Either.cond(nb == 0, (), MembershipDoesNotExists(slug, personId)))
      } yield ()
    }.value
  }
}
