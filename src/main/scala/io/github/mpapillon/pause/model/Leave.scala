package io.github.mpapillon.pause.model

import java.time.LocalDate

import io.chrisdavenport.fuuid.FUUID

final case class Leave(id: FUUID,
                       member_id: FUUID,
                       startDate: LocalDate,
                       endDate: LocalDate,
                       halfStartDate: Boolean,
                       halfEndDate: Boolean,
                       `type`: LeaveType)
