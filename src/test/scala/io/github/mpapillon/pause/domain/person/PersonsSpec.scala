package io.github.mpapillon.pause.domain.person

import java.util.UUID

import cats.effect.IO
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.repository.{PersonsRepository, RepositoryError}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers}

class PersonsSpec extends AsyncWordSpec with AsyncMockFactory with Matchers with EitherValues {

  "Persons" should {

    "returns a list with all members" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)
      val membersLst = Vector(
        Person(UUID.fromString("0a4bfb4d-f05a-43b3-a822-c3072a2eacb7"), "firstName_1", "lastName_1", None),
        Person(UUID.fromString("bc2063e3-ce82-4e3c-92c1-aedec159e729"), "firstName_2", "lastName_2", None),
        Person(UUID.fromString("32ab6697-2f7b-4eb7-96ab-570da51fbb7e"), "firstName_3", "lastName_3", None)
      )

      repoStub.findAll _ when () returns IO.pure(membersLst)

      for (result <- members.all.unsafeToFuture()) yield {
        result should contain theSameElementsAs membersLst
      }
    }

    "returns an empty list of members" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)

      repoStub.findAll _ when () returns IO.pure(Vector.empty[Person])

      for (result <- members.all.unsafeToFuture()) yield {
        result shouldBe empty
      }
    }

    "be able to add new member" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)

      repoStub.insert _ when * returns IO.pure(Right(1))

      for (result <- members.add("firstName_1", "lastName_1", None).unsafeToFuture()) yield {
        val person = result.right.value
        person.firstName shouldBe "firstName_1"
        person.lastName shouldBe "lastName_1"
        person.email shouldBe None
      }
    }

    "not be able to add new member" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)

      repoStub.insert _ when * returns IO.pure(Left(RepositoryError.UniqueViolationConstraintError))

      for (result <- members.add("firstName_1", "lastName_1", None).unsafeToFuture()) yield {
        result.left.value shouldBe a[PersonsError.PersonAlreadyExist]
      }
    }
  }
}
