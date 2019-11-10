package io.github.mpapillon.pause.domain.person

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.repository.{PersonsRepository, RepositoryError}

trait Persons[F[_]] {

  def all: F[Vector[Person]]
  def get(personId: FUUID): F[Option[Person]]
  def add(person: Person): F[Either[PersonsError, Unit]]
  def remove(personId: FUUID): F[Int]
}

object Persons {

  def impl[F[_]: Monad](personsRepo: PersonsRepository[F]): Persons[F] = new Persons[F] {

    override def all: F[Vector[Person]] =
      personsRepo.findAll()

    override def get(personId: FUUID): F[Option[Person]] =
      personsRepo.findById(personId)

    override def add(person: Person): F[Either[PersonsError, Unit]] =
      EitherT(personsRepo.insert(person))
        .leftMap[PersonsError] {
          case RepositoryError.UniqueViolationConstraintError => PersonsError.PersonAlreadyExist(person.id)
        }
        .as(())
        .value

    override def remove(personId: FUUID): F[Int] =
      personsRepo.delete(personId)
  }
}
