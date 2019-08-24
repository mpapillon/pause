package io.github.mpapillon.pause.domains.teams

import cats.implicits._
import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import doobie.util.transactor.Transactor
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.repositories.{MembersRepository, RepositoryError, TeamsRepository}
import io.github.mpapillon.pause.model.{Person, Team}

object TeamsService {

  def impl[F[_]: Sync](
      teamsRepo: TeamsRepository[doobie.ConnectionIO],
      membersRepo: MembersRepository[doobie.ConnectionIO],
      xa: Transactor[F]
  ): Teams[F] = new Teams[F] {
    import doobie.implicits._

    override def all: F[Vector[Team]] =
      teamsRepo.findAll().transact(xa)

    override def add(team: Team): F[Either[TeamsError, Unit]] =
      EitherT(teamsRepo.insert(team).transact(xa))
        .leftMap[TeamsError] {
          case RepositoryError.UniqueViolationConstraintError =>
            TeamsError.TeamAlreadyExists(team.canonicalName)
        }
        .as(())
        .value

    override def get(canonicalName: String): F[Option[Team]] =
      teamsRepo.findByName(canonicalName).transact(xa)

    override def membersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Member]]] = {
      for {
        team    <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamsError.TeamNotFound(canonicalName))
        members <- EitherT.right[TeamsError](teamsRepo.findMembers(team.id))
      } yield members
    }.transact(xa).value

    override def managersOf(canonicalName: String): F[Either[TeamsError, Vector[Person.Manager]]] = {
      for {
        team    <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamsError.TeamNotFound(canonicalName))
        members <- EitherT.right[TeamsError](teamsRepo.findManagers(team.id))
      } yield members
    }.transact(xa).value

    override def join(canonicalName: String, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamsError.TeamNotFound(canonicalName))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(TeamsError.MemberNotFound(memberID))
        _ <- EitherT(teamsRepo.insertMember(team.id, memberID)).leftMap[TeamsError] {
              case RepositoryError.UniqueViolationConstraintError =>
                TeamsError.MembershipAlreadyExists(canonicalName, memberID)
            }
      } yield ()
    }.transact(xa).value

    override def leave(canonicalName: String, memberID: FUUID): F[Either[TeamsError, Unit]] = {
      for {
        team <- OptionT(teamsRepo.findByName(canonicalName)).toRight(TeamsError.TeamNotFound(canonicalName))
        _    <- OptionT(membersRepo.findById(memberID)).toRight(TeamsError.MemberNotFound(memberID))
        _ <- EitherT
              .right[TeamsError](teamsRepo.deleteMember(team.id, memberID))
              .subflatMap(nb => Either.cond(nb == 0, (), TeamsError.MembershipDoesNotExists(canonicalName, memberID)))
      } yield ()
    }.transact(xa).value
  }
}
