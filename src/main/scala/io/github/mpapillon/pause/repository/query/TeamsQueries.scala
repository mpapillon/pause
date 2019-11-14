package io.github.mpapillon.pause.repository.query

import java.time.LocalDate
import java.util.UUID

import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.update.Update
import io.github.mpapillon.pause.model.{Member, Slug, Team}

object TeamsQueries {

  val findAll: doobie.Query0[Team] =
    sql"SELECT team_id, name, slug, creation_date FROM team"
      .query[Team]

  def insert(name: String, slug: Slug, creationDate: LocalDate): doobie.Update0 =
    sql"INSERT INTO team (name, slug, creation_date) values ($name, $slug, $creationDate)".update

  def findBySlug(slug: Slug): doobie.Query0[Team] =
    sql"SELECT team_id, name, slug, creation_date FROM team WHERE slug = $slug"
      .query[Team]

  def findMembers(teamId: Int): doobie.Query0[Member] =
    sql"""SELECT m.person_id, m.first_name, m.last_name, m.email, tm.is_manager
          FROM team_person tm
          INNER JOIN person m ON tm.person_id = m.person_id
          WHERE tm.team_id = $teamId""".query[Member]

  val insertMembers: Update[(Int, UUID)] =
    //language=SQL
    Update[(Int, UUID)]("INSERT INTO team_person (team_id, person_id) VALUES (?, ?)")

  val deleteMembers: Update[(Int, UUID)] =
    //language=SQL
    Update[(Int, UUID)]("DELETE FROM team_person WHERE team_id = ? and person_id = ?")
}
