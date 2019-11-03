package io.github.mpapillon.pause.domain.teams

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.chrisdavenport.cats.effect.time.JavaTime
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.domain.teams.TeamsError._
import io.github.mpapillon.pause.model.{Person, Team}
import io.github.mpapillon.pause.repository.{MembersRepository, RepositoryError, TeamsRepository}

trait Teams[F[_]] {

  def all: F[Vector[Team]]
  def add(name: String, canonicalName: String): F[Either[TeamsError, Team]]
  def get(canonicalName: String): F[Option[Team]]
  def membersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Member]]]
  def managersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Manager]]]
  def join(canonicalName: String, membersID: FUUID): F[Either[TeamsError, Unit]]
  def leave(canonicalName: String, membersID: FUUID): F[Either[TeamsError, Unit]]
}

object Teams {

  def impl[F[_]: Monad: JavaTime](
      teamsRepo: TeamsRepository[F],
      membersRepo: MembersRepository[F]
  ): Teams[F] = new Teams[F] {

    override def all: F[Vector[Team]] =
      teamsRepo.findAll()

    override def add(name: String, canonicalName: String): F[Either[TeamsError, Team]] =
      for {
        creationDate <- JavaTime[F].getLocalDateUTC
        teamId       <- teamsRepo.insert(name, canonicalName, creationDate)
        team         = teamId.map(Team(_, name, canonicalName, creationDate))
      } yield team.leftMap {
        case RepositoryError.UniqueViolationConstraintError => TeamAlreadyExists(canonicalName)
      }

    override def get(canonicalName: String): F[Option[Team]] =
      teamsRepo.findByName(canonicalName)

    override def membersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Member]]] = {
      for {
        team    <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamNotFound(canonicalName))
        members <- EitherT.right[TeamsError](teamsRepo.findMembers(team.id))
      } yield members
    }.value

    override def managersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Manager]]] = {
      for {
        team    <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamNotFound(canonicalName))
        members <- EitherT.right[TeamsError](teamsRepo.findManagers(team.id))
      } yield members
    }.value

    override def join(canonicalName: String, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamNotFound(canonicalName))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(MemberNotFound(memberID))
        _ <- EitherT(teamsRepo.insertMember(team.id, memberID)).leftMap[TeamsError] {
              case RepositoryError.UniqueViolationConstraintError =>
                MembershipAlreadyExists(canonicalName, memberID)
            }
      } yield ()
    }.value

    override def leave(canonicalName: String, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamNotFound(canonicalName))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(MemberNotFound(memberID))
        _ <- EitherT
              .right[TeamsError](teamsRepo.deleteMember(team.id, memberID))
              .subflatMap(nb => Either.cond(nb == 0, (), MembershipDoesNotExists(canonicalName, memberID)))
      } yield ()
    }.value
  }
}
