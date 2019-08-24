package io.github.mpapillon.pause

import java.util.UUID.randomUUID

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.specs2.mutable.Specification
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.queries.TeamsQueries._

import scala.concurrent.ExecutionContext

class TeamsAnalysisTestSpec extends Specification with doobie.specs2.IOChecker {

  implicit def cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:team_wip",
    "postgres",
    "",
  )

  check(findAll)
  check(findByName("fake-team"))
  check(findMembers(FUUID.fromUUID(randomUUID())))
  check(findManagers(FUUID.fromUUID(randomUUID())))
  check(insertMembers)
  check(deleteMembers)
}
