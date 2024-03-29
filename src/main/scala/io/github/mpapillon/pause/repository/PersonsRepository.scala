package io.github.mpapillon.pause.repository

import java.util.UUID

import cats.effect.Async
import doobie.util.transactor.Transactor
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.repository.RepositoryError.Result
import io.github.mpapillon.pause.repository.query.PersonsQueries

trait PersonsRepository[F[_]] {

  def findAll(): F[Vector[Person]]
  def findById(memberID: UUID): F[Option[Person]]
  def insert(m: Person): F[Result[Int]]
  def delete(memberID: UUID): F[Int]
}

object PersonsRepository {
  import doobie.implicits._

  def impl[F[_]: Async](xa: Transactor[F]): PersonsRepository[F] = new PersonsRepository[F] {
    override def findAll(): F[Vector[Person]] =
      PersonsQueries.findAll.to[Vector].transact(xa)

    override def findById(memberID: UUID): F[Option[Person]] =
      PersonsQueries.findById(memberID).option.transact(xa)

    override def insert(m: Person): F[Result[Int]] =
      PersonsQueries
        .insert(m.id, m.firstName, m.lastName, m.email)
        .run
        .attemptSomeSqlState(RepositoryError.handleSqlState)
        .transact(xa)

    override def delete(memberID: UUID): F[Int] =
      PersonsQueries.delete(memberID).run.transact(xa)
  }
}
