package io.github.mpapillon.pause.repository.query

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.Person

object PersonsQueries {
  import doobie.implicits._
  import doobie.postgres.implicits._
  import io.chrisdavenport.fuuid.doobie.implicits._

  val findAll: doobie.Query0[Person] =
    sql"SELECT person_id, first_name, last_name, email FROM person".query[Person]

  def findById(personId: FUUID): doobie.Query0[Person] =
    sql"SELECT person_id, first_name, last_name, email FROM person WHERE person_id = $personId"
      .query[Person]

  def insert(id: FUUID, firstName: String, lastName: String, email: Option[String]): doobie.Update0 =
    sql"INSERT INTO person VALUES ($id, $firstName, $lastName, $email)".update

  def delete(personId: FUUID): doobie.Update0 =
    sql"DELETE FROM person WHERE person_id = $personId".update
}
