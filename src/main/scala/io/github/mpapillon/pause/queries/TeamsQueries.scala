package io.github.mpapillon.pause.queries

import java.time.LocalDate

import doobie.util.Read
import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.{Person, Team}

object TeamsQueries {
  import doobie.implicits._
  import doobie.postgres.implicits._
  import doobie.util.update.Update
  import io.chrisdavenport.fuuid.doobie.implicits._

  val findAll: doobie.Query0[Team] =
    sql"SELECT team_id, name, name_canonical, creation_date FROM team"
      .query[Team]

  def insert(id: FUUID, name: String, canonicalName: String, creationDate: LocalDate): doobie.Update0 =
    sql"INSERT INTO team (team_id, name, name_canonical, creation_date) values ($id, $name, $canonicalName, $creationDate)".update

  def findByName(canonicalName: String): doobie.Query0[Team] =
    sql"SELECT team_id, name, name_canonical, creation_date FROM team WHERE name_canonical = $canonicalName"
      .query[Team]

  def findMembers(teamID: FUUID): doobie.Query0[Person.Member] =
    findTeamMember[Person.Member](teamID, isManager = false)

  def findManagers(teamID: FUUID): doobie.Query0[Person.Manager] =
    findTeamMember[Person.Manager](teamID, isManager = true)

  private def findTeamMember[T: Read](teamID: FUUID, isManager: Boolean): doobie.Query0[T] =
    sql"""SELECT m.member_id, m.first_name, m.last_name, m.email
            FROM team_member tm
            INNER JOIN member m ON tm.member_id = m.member_id
            WHERE tm.team_id = $teamID
            AND tm.is_manager = $isManager""".query[T]

  val insertMembers: Update[(FUUID, FUUID)] =
    //language=SQL
    Update[(FUUID, FUUID)]("INSERT INTO team_member (team_id, member_id) VALUES (?, ?)")

  val deleteMembers: Update[(FUUID, FUUID)] =
    //language=SQL
    Update[(FUUID, FUUID)]("DELETE FROM team_member WHERE team_id = ? and member_id = ?")
}
