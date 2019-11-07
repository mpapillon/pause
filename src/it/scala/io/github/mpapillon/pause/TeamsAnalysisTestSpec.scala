package io.github.mpapillon.pause

import io.github.mpapillon.pause.model.Slug
import io.github.mpapillon.pause.repository.query.TeamsQueries._
import org.scalatest.{FunSuite, Matchers}

class TeamsAnalysisTestSpec extends FunSuite with Matchers with DbChecker {

  test("find all") { check(findAll) }
  test("find by slug") { check(findBySlug(Slug("fake-team"))) }
  test("find members") { check(findMembers(1)) }
  test("find managers") { check(findManagers(4)) }
  test("insert member") { check(insertMembers) }
  test("delete member") { check(deleteMembers) }
}
