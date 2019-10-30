package io.github.mpapillon.pause.repositories

import cats.effect.Async
import doobie.util.transactor.Transactor
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.queries.MembersQueries

trait MembersRepository[F[_]] {

  def findAll(): F[Vector[Member]]
  def findById(memberID: FUUID): F[Option[Member]]
  def insert(m: Member): F[Either[RepositoryError, Int]]
  def delete(memberID: FUUID): F[Int]
}

object MembersRepository {
  import doobie.implicits._

  def impl[F[_]: Async](xa: Transactor[F]): MembersRepository[F] = new MembersRepository[F] {
    override def findAll(): F[Vector[Member]] =
      MembersQueries.findAll.to[Vector].transact(xa)

    override def findById(memberID: FUUID): F[Option[Member]] =
      MembersQueries.findById(memberID).option.transact(xa)

    override def insert(m: Member): F[Either[RepositoryError, Int]] =
      MembersQueries
        .insert(m.id, m.firstName, m.lastName, m.email)
        .run
        .attemptSomeSqlState(RepositoryError.handleSqlState)
        .transact(xa)

    override def delete(memberID: FUUID): F[Int] =
      MembersQueries.delete(memberID).run.transact(xa)
  }
}
