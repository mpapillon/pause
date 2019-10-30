package io.github.mpapillon.pause.domains.members

import java.util.UUID.randomUUID

import cats.Id
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.repositories.{MembersRepository, RepositoryError}
import org.specs2.matcher.IOMatchers
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class MembersServiceSpec extends Specification with IOMatchers with Mockito {

  "Members service" >> {
    "should returns a list with all members" >> {
      val mockRepo   = mock[MembersRepository[Id]]
      val membersLst = memberBuilder(3).toVector
      val members    = Members.impl[Id](mockRepo)

      mockRepo.findAll() returns membersLst
      members.all must not be empty
      members.all must be size 3
      members.all must contain(allOf(membersLst: _*))
    }

    "should returns an empty list of members" >> {
      val mockRepo = mock[MembersRepository[Id]]
      val members  = Members.impl[Id](mockRepo)

      mockRepo.findAll() returns Vector.empty[Member]
      members.all must be empty
    }

    "should be able to add new member" >> {
      val mockRepo = mock[MembersRepository[Id]]
      val members  = Members.impl[Id](mockRepo)

      mockRepo.insert(org.mockito.ArgumentMatchers.any[Member]()) returns Right(1)
      members.add(memberBuilder(1).head) must beRight()
    }

    "should not be able to add new member" >> {
      val mockRepo = mock[MembersRepository[Id]]
      val members  = Members.impl[Id](mockRepo)

      mockRepo.insert(org.mockito.ArgumentMatchers.any[Member]()) returns {
        Left(RepositoryError.UniqueViolationConstraintError)
      }

      members.add(memberBuilder(1).head) must beLeft(beAnInstanceOf[MembersError.MemberAlreadyExist])
    }
  }

  private[this] def memberBuilder(nb: Int) =
    for (i <- 1 to nb) yield Member(FUUID.fromUUID(randomUUID()), s"firstName_$i", s"lastName_$i", None)
}
