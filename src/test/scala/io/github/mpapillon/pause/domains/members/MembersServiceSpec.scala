package io.github.mpapillon.pause.domains.members

import java.sql.Connection
import java.util.UUID.randomUUID

import cats.effect.IO
import cats.free.Free
import doobie.util.transactor.Transactor.Aux
import doobie.util.transactor.{Strategy, Transactor}
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.repositories.{MembersRepository, RepositoryError}
import org.specs2.matcher.IOMatchers
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

import scala.concurrent.ExecutionContext

class MembersServiceSpec extends Specification with IOMatchers with Mockito {

  "Members service" >> {
    "should returns a list with all members" >> {
      val mockRepo   = mock[MembersRepository[doobie.ConnectionIO]]
      val membersLst = memberBuilder(3).toVector
      val members    = MembersService.impl[IO](mockRepo, xa)

      mockRepo.findAll() returns Free.pure(membersLst)
      members.all.unsafeRunSync() must not be empty
      members.all.unsafeRunSync() must be size 3
      members.all.unsafeRunSync() must contain(allOf(membersLst: _*))
    }

    "should returns an empty list of members" >> {
      val mockRepo = mock[MembersRepository[doobie.ConnectionIO]]
      val members  = MembersService.impl[IO](mockRepo, xa)

      mockRepo.findAll() returns Free.pure(Vector.empty[Member])
      members.all.unsafeRunSync() must be empty
    }

    "should be able to add new member" >> {
      val mockRepo = mock[MembersRepository[doobie.ConnectionIO]]
      val members  = MembersService.impl[IO](mockRepo, xa)

      mockRepo.insert(org.mockito.ArgumentMatchers.any[Member]()) returns Free.pure(Right(1))
      members.add(memberBuilder(1).head).unsafeRunSync() must beRight()
    }

    "should not be able to add new member" >> {
      val mockRepo = mock[MembersRepository[doobie.ConnectionIO]]
      val members  = MembersService.impl[IO](mockRepo, xa)

      mockRepo.insert(org.mockito.ArgumentMatchers.any[Member]()) returns Free.pure(
        Left(RepositoryError.UniqueViolationConstraintError)
      )
      members.add(memberBuilder(1).head).unsafeRunSync() must beLeft(beAnInstanceOf[MembersError.MemberAlreadyExist])
    }
  }

  private[this] val xa: Aux[IO, Connection] =
    Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy.void)

  private[this] def memberBuilder(nb: Int) =
    for (i <- 1 to nb) yield Member(FUUID.fromUUID(randomUUID()), s"firstName_$i", s"lastName_$i", None)
}
