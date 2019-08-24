package io.github.mpapillon.pause.repositories

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

  def impl: MembersRepository[doobie.ConnectionIO] = new MembersRepository[doobie.ConnectionIO] {
    override def findAll(): doobie.ConnectionIO[Vector[Member]] =
      MembersQueries.findAll.to[Vector]

    override def findById(memberID: FUUID): doobie.ConnectionIO[Option[Member]] =
      MembersQueries.findById(memberID).option

    override def insert(m: Member): doobie.ConnectionIO[Either[RepositoryError, Int]] =
      MembersQueries
        .insert(m.id, m.firstName, m.lastName, m.email)
        .run
        .attemptSomeSqlState(RepositoryError.handleSqlState)

    override def delete(memberID: FUUID): doobie.ConnectionIO[Int] =
      MembersQueries.delete(memberID).run
  }
}
