package io.github.mpapillon.pause.repository.query

import java.time.LocalDate

import io.chrisdavenport.fuuid.FUUID
import io.github.mpapillon.pause.model.{Leave, LeaveType}

object LeaveQueries {
  import doobie.implicits._
  import doobie.postgres.implicits._
  import io.chrisdavenport.fuuid.doobie.implicits._

  val findAll: doobie.Query0[Leave] =
    sql"SELECT leave_id, member_id, start_date, end_date, half_start_date, half_end_date, type FROM leave".query[Leave]

  def insert(
      leaveId: FUUID,
      memberId: FUUID,
      startDate: LocalDate,
      endDate: LocalDate,
      halfStartDate: Boolean,
      halfEndDate: Boolean,
      leaveType: LeaveType
  ): doobie.Update0 =
    sql"""
      INSERT INTO leave(leave_id, member_id, start_date, end_date, half_start_date, half_end_date, type)
      VALUES ($leaveId, $memberId, $startDate, $endDate, $halfStartDate, $halfEndDate, $leaveType)
       """.update
}
