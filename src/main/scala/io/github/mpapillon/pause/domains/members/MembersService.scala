package io.github.mpapillon.pause.domains.members

import cats.data.EitherT
import cats.implicits._
import cats.effect.Async
import doobie.util.transactor.Transactor
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.repositories.{MembersRepository, RepositoryError}

object MembersService {

  def impl[F[_]: Async](membersRepo: MembersRepository[doobie.ConnectionIO], xa: Transactor[F]): Members[F] =
    new Members[F] {
      import doobie.implicits._

      override def all: F[Vector[Member]] =
        membersRepo.findAll().transact(xa)

      override def get(memberID: FUUID): F[Option[Member]] =
        membersRepo.findById(memberID).transact(xa)

      override def add(member: Member): F[Either[MembersError, Unit]] =
        EitherT(membersRepo.insert(member).transact(xa))
          .leftMap[MembersError] {
            case RepositoryError.UniqueViolationConstraintError => MembersError.MemberAlreadyExist(member.id)
          }
          .as(())
          .value

      override def remove(memberID: FUUID): F[Int] =
        membersRepo.delete(memberID).transact(xa)
    }
}
