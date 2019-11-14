package io.github.mpapillon.pause

import java.util.UUID.randomUUID

import io.github.mpapillon.pause.repository.query.PersonsQueries._
import org.scalatest.{FunSuite, Matchers}

class PersonsAnalysisTestSpec extends FunSuite with Matchers with DbChecker {

  test("find all") { check(findAll) }
  test("find by id") { check(findById(randomUUID())) }
  test("insert") { check(insert(randomUUID(), "firstName", "lastName", None)) }
  test("delete") { check(delete(randomUUID())) }
}
