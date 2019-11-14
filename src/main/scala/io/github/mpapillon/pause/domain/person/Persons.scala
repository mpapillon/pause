package io.github.mpapillon.pause.domain.person

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.repository.{PersonsRepository, RepositoryError}

trait Persons[F[_]] {

  def all: F[Vector[Person]]
  def get(personId: UUID): F[Option[Person]]
  def add(firstName: String, lastName: String, email: Option[String]): F[Either[PersonsError, Person]]
  def remove(personId: UUID): F[Int]
}

object Persons {

  def impl[F[_]: Sync](personsRepo: PersonsRepository[F]): Persons[F] = new Persons[F] {

    override def all: F[Vector[Person]] =
      personsRepo.findAll()

    override def get(personId: UUID): F[Option[Person]] =
      personsRepo.findById(personId)

    override def add(firstName: String, lastName: String, email: Option[String]): F[Either[PersonsError, Person]] =
      for {
        id          <- Sync[F].delay(UUID.randomUUID())
        person      = Person(id, firstName, lastName, email)
        nbOfInserts <- personsRepo.insert(person)
      } yield nbOfInserts
        .leftMap[PersonsError] {
          case RepositoryError.UniqueViolationConstraintError => PersonsError.PersonAlreadyExist(person.id)
        }
        .as(person)

    override def remove(personId: UUID): F[Int] =
      personsRepo.delete(personId)
  }
}
