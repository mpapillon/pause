package io.github.mpapillon.pause.domain.member

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.repository.{MembersRepository, RepositoryError}

trait Members[F[_]] {

  def all: F[Vector[Member]]
  def get(memberID: FUUID): F[Option[Member]]
  def add(member: Member): F[Either[MembersError, Unit]]
  def remove(memberID: FUUID): F[Int]
}

object Members {

  def impl[F[_]: Monad](membersRepo: MembersRepository[F]): Members[F] = new Members[F] {

    override def all: F[Vector[Member]] =
      membersRepo.findAll()

    override def get(memberID: FUUID): F[Option[Member]] =
      membersRepo.findById(memberID)

    override def add(member: Member): F[Either[MembersError, Unit]] =
      EitherT(membersRepo.insert(member))
        .leftMap[MembersError] {
          case RepositoryError.UniqueViolationConstraintError => MembersError.MemberAlreadyExist(member.id)
        }
        .as(())
        .value

    override def remove(memberID: FUUID): F[Int] =
      membersRepo.delete(memberID)
  }
}
