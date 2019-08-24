package io.github.mpapillon.pause.model

import enumeratum.values._

import scala.collection.immutable

sealed abstract class LeaveType(val value: Short) extends ShortEnumEntry

object LeaveType extends ShortEnum[LeaveType] with ShortDoobieEnum[LeaveType] with ShortCirceEnum[LeaveType] {

  override def values: immutable.IndexedSeq[LeaveType] = findValues

  case object Unknown      extends LeaveType(0)
  case object Paid         extends LeaveType(1)
  case object Unpaid       extends LeaveType(2)
  case object Sick         extends LeaveType(3)
  case object Formation    extends LeaveType(4)
  case object BusinessTrip extends LeaveType(5)
}
