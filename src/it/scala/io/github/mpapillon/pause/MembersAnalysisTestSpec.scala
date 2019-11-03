package io.github.mpapillon.pause

import java.util.UUID.randomUUID

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.repository.query.MembersQueries
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext

class MembersAnalysisTestSpec extends Specification with doobie.specs2.IOChecker {

  import MembersQueries._

  implicit def cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:pause",
    "postgres",
    "",
  )

  check(findAll)
  check(findById(FUUID.fromUUID(randomUUID())))
  check(insert(FUUID.fromUUID(randomUUID()), "firstName", "lastName", None))
  check(delete(FUUID.fromUUID(randomUUID())))
}
