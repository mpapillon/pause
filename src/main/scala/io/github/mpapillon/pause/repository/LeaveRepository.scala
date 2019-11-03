package io.github.mpapillon.pause.repository

import io.github.mpapillon.pause.model.Leave

trait LeaveRepository[F[_]] {

  def findAll(): F[Vector[Leave]]
  def insert(leave: Leave): F[Int]
}