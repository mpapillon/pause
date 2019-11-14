package io.github.mpapillon.pause.repository.query

import java.util.UUID

import io.github.mpapillon.pause.model.Person
import doobie.implicits._
import doobie.postgres.implicits._

object PersonsQueries {

  val findAll: doobie.Query0[Person] =
    sql"SELECT person_id, first_name, last_name, email FROM person".query[Person]

  def findById(personId: UUID): doobie.Query0[Person] =
    sql"SELECT person_id, first_name, last_name, email FROM person WHERE person_id = $personId"
      .query[Person]

  def insert(id: UUID, firstName: String, lastName: String, email: Option[String]): doobie.Update0 =
    sql"INSERT INTO person VALUES ($id, $firstName, $lastName, $email)".update

  def delete(personId: UUID): doobie.Update0 =
    sql"DELETE FROM person WHERE person_id = $personId".update
}
