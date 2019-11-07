package io.github.mpapillon.pause.domain.team

import java.time.{LocalDate, OffsetDateTime}

import cats.effect.{Clock, IO}
import io.github.mpapillon.pause.fixedClock
import io.github.mpapillon.pause.model.{Slug, Team}
import io.github.mpapillon.pause.repository.{MembersRepository, TeamsRepository}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncWordSpec, EitherValues, Matchers}

class TeamsSpec extends AsyncWordSpec with AsyncMockFactory with Matchers with EitherValues {

  "Teams service" should {

    implicit val clock: Clock[IO] = fixedClock(OffsetDateTime.parse("2019-07-09T10:15:30Z").toInstant)

    "returns a list with all teams" in {
      val teamsRepoStub   = stub[TeamsRepository[IO]]
      val membersRepoStub = stub[MembersRepository[IO]]
      val teamsAlg        = Teams.impl(teamsRepoStub, membersRepoStub)

      val teamsLst = Vector(
        Team(0, "First team", Slug("first-team"), LocalDate.parse("2019-11-07")),
        Team(1, "Second team", Slug("second-team"), LocalDate.parse("2019-11-06"))
      )

      teamsRepoStub.findAll _ when () returns IO.pure(teamsLst)

      for (result <- teamsAlg.all.unsafeToFuture()) yield {
        result should contain theSameElementsAs teamsLst
      }
    }

    "adds new team" in {
      val teamsRepoStub   = stub[TeamsRepository[IO]]
      val membersRepoStub = stub[MembersRepository[IO]]
      val teamsAlg        = Teams.impl(teamsRepoStub, membersRepoStub)

      teamsRepoStub.insert _ when (*, *, *) returns IO.pure(Right(189))

      for (team <- teamsAlg.add("Hello world !").unsafeToFuture()) yield {
        team.right.value shouldBe Team(189, "Hello world !", Slug("hello-world"), LocalDate.parse("2019-07-09"))
      }
    }
  }
}
