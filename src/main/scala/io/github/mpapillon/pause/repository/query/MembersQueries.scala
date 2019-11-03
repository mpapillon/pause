package io.github.mpapillon.pause.repository.query

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Member

object MembersQueries {
  import doobie.implicits._
  import doobie.postgres.implicits._
  import io.chrisdavenport.fuuid.doobie.implicits._

  val findAll: doobie.Query0[Member] =
    sql"SELECT member_id, first_name, last_name, email FROM member".query[Member]

  def findById(memberID: FUUID): doobie.Query0[Member] =
    sql"SELECT member_id, first_name, last_name, email FROM member WHERE member_id = $memberID"
      .query[Member]

  def insert(id: FUUID, firstName: String, lastName: String, email: Option[String]): doobie.Update0 =
    sql"INSERT INTO member VALUES ($id, $firstName, $lastName, $email)".update

  def delete(memberID: FUUID): doobie.Update0 =
    sql"DELETE FROM member WHERE member_id = $memberID".update
}
