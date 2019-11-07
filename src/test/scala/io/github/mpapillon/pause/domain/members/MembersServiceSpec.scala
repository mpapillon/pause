package io.github.mpapillon.pause.domain.members

import java.util.UUID.randomUUID

import cats.Id
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.repository.{MembersRepository, RepositoryError}
import org.scalatest.{EitherValues, Matchers, WordSpec}
import org.scalamock.scalatest.MockFactory

class MembersServiceSpec extends WordSpec with Matchers with EitherValues with MockFactory {

  "Members service" should {
    "returns a list with all members" in {
      val repoStub   = stub[MembersRepository[Id]]
      val membersLst = memberBuilder(3).toVector
      val members    = Members.impl[Id](repoStub)

      repoStub.findAll _ when() returns  membersLst
      members.all.toList should contain theSameElementsAs membersLst
    }

    "returns an empty list of members" in {
      val repoStub = stub[MembersRepository[Id]]
      val members  = Members.impl[Id](repoStub)

      repoStub.findAll _ when() returns Vector.empty[Member]
      members.all shouldBe empty
    }

    "be able to add new member" in {
      val repoStub = stub[MembersRepository[Id]]
      val members  = Members.impl[Id](repoStub)

      repoStub.insert _ when * returns Right(1)
      members.add(memberBuilder(1).head) shouldBe 'right
    }

    "not be able to add new member" in {
      val repoStub = stub[MembersRepository[Id]]
      val members  = Members.impl[Id](repoStub)

      repoStub.insert _ when * returns Left(RepositoryError.UniqueViolationConstraintError)
      members.add(memberBuilder(1).head).left.value shouldBe a [MembersError.MemberAlreadyExist]
    }
  }

  private[this] def memberBuilder(nb: Int) =
    for (i <- 1 to nb) yield Member(FUUID.fromUUID(randomUUID()), s"firstName_$i", s"lastName_$i", None)
}
