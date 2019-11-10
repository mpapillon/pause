package io.github.mpapillon.pause

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Server.stream[IO]().compile.lastOrError
}
