package io.github.mpapillon.pause

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import io.github.mpapillon.pause.repository.query.TeamsQueries._
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext

class TeamsAnalysisTestSpec extends Specification with doobie.specs2.IOChecker {

  implicit def cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:pause",
    "postgres",
    "",
  )

  check(findAll)
  check(findByName("fake-team"))
  check(findMembers(1))
  check(findManagers(4))
  check(insertMembers)
  check(deleteMembers)
}
