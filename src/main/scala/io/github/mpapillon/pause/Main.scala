package io.github.mpapillon.pause

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    PauseServer.stream[IO].compile.drain.as(ExitCode.Success)
}
