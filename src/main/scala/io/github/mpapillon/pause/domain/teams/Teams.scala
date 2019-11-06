package io.github.mpapillon.pause.domain.teams

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.implicits._
import io.chrisdavenport.cats.effect.time.JavaTime
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.domain.teams.TeamsError._
import io.github.mpapillon.pause.model.{Person, Slug, Team}
import io.github.mpapillon.pause.repository.{MembersRepository, RepositoryError, TeamsRepository}

trait Teams[F[_]] {

  def all: F[Vector[Team]]
  def add(name: String): F[Either[TeamsError, Team]]
  def get(slug: Slug): F[Option[Team]]
  def membersOf(slug: Slug): F[Either[TeamsError, Vector[Person.Member]]]
  def managersOf(slug: Slug): F[Either[TeamsError, Vector[Person.Manager]]]
  def join(slug: Slug, membersID: FUUID): F[Either[TeamsError, Unit]]
  def leave(slug: Slug, membersID: FUUID): F[Either[TeamsError, Unit]]
}

object Teams {

  def impl[F[_]: Monad: JavaTime](
      teamsRepo: TeamsRepository[F],
      membersRepo: MembersRepository[F]
  ): Teams[F] = new Teams[F] {

    override def all: F[Vector[Team]] =
      teamsRepo.findAll()

    override def add(name: String): F[Either[TeamsError, Team]] =
      for {
        creationDate <- JavaTime[F].getLocalDateUTC
        slug         = Slug(name)
        teamId       <- teamsRepo.insert(name, slug, creationDate)
        team         = teamId.map(Team(_, name, slug, creationDate))
      } yield team.leftMap {
        case RepositoryError.UniqueViolationConstraintError => TeamAlreadyExists(slug)
      }

    override def get(slug: Slug): F[Option[Team]] =
      teamsRepo.findBySlug(slug)

    override def membersOf(slug: Slug): F[Either[TeamsError, Vector[Person.Member]]] = {
      for {
        team    <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        members <- EitherT.right[TeamsError](teamsRepo.findMembers(team.id))
      } yield members
    }.value

    override def managersOf(slug: Slug): F[Either[TeamsError, Vector[Person.Manager]]] = {
      for {
        team    <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        members <- EitherT.right[TeamsError](teamsRepo.findManagers(team.id))
      } yield members
    }.value

    override def join(slug: Slug, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(MemberNotFound(memberID))
        _ <- EitherT(teamsRepo.insertMember(team.id, memberID)).leftMap[TeamsError] {
              case RepositoryError.UniqueViolationConstraintError =>
                MembershipAlreadyExists(slug, memberID)
            }
      } yield ()
    }.value

    override def leave(slug: Slug, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findBySlug(slug)).toRight(TeamNotFound(slug))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(MemberNotFound(memberID))
        _ <- EitherT
              .right[TeamsError](teamsRepo.deleteMember(team.id, memberID))
              .subflatMap(nb => Either.cond(nb == 0, (), MembershipDoesNotExists(slug, memberID)))
      } yield ()
    }.value
  }
}
