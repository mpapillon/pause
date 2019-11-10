package io.github.mpapillon.pause.domain.person

import cats.effect.IO
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.repository.{PersonsRepository, RepositoryError}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers, OptionValues}

class PersonsSpec extends AsyncWordSpec with AsyncMockFactory with Matchers with EitherValues with OptionValues {

  "Members service" should {

    "returns a list with all members" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)
      val membersLst = Vector(
        FUUID.fromStringOpt("0a4bfb4d-f05a-43b3-a822-c3072a2eacb7").map(Person(_, "firstName_1", "lastName_1", None)),
        FUUID.fromStringOpt("bc2063e3-ce82-4e3c-92c1-aedec159e729").map(Person(_, "firstName_2", "lastName_2", None)),
        FUUID.fromStringOpt("32ab6697-2f7b-4eb7-96ab-570da51fbb7e").map(Person(_, "firstName_3", "lastName_3", None))
      ).map(_.value)

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

      val member =
        FUUID
          .fromStringOpt("21bfb6b6-c7dc-47c7-b53c-26f23b3bff2e")
          .map(Person(_, "firstName_1", "lastName_1", None))
          .value

      for (result <- members.add(member).unsafeToFuture()) yield {
        result shouldBe 'right
      }
    }

    "not be able to add new member" in {
      val repoStub = stub[PersonsRepository[IO]]
      val members  = Persons.impl[IO](repoStub)

      repoStub.insert _ when * returns IO.pure(Left(RepositoryError.UniqueViolationConstraintError))

      val member =
        FUUID
          .fromStringOpt("21bfb6b6-c7dc-47c7-b53c-26f23b3bff2e")
          .map(Person(_, "firstName_1", "lastName_1", None))
          .value

      for (result <- members.add(member).unsafeToFuture()) yield {
        result.left.value shouldBe a[PersonsError.PersonAlreadyExist]
      }
    }
  }
}
