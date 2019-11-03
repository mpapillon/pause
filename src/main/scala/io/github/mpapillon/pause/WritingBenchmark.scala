package io.github.mpapillon.pause

import java.util.concurrent.TimeUnit

import cats.effect._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import io.chrisdavenport.fuuid.doobie.implicits._
import io.chrisdavenport.fuuid.FUUID
import fs2._

object WritingBenchmark extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    runBenchmark(getDatabase) >> IO.pure(ExitCode.Success)

  private def uuidQuery(ps: List[(FUUID, Int)]): ConnectionIO[Int] =
    Update[(FUUID, Int)]("INSERT INTO uuid_pk VALUES (?, ?)")
      .updateMany(ps)

  private def incrementQuery(ps: List[Int]): ConnectionIO[Int] =
    Update[Int]("INSERT INTO increment_pk (index) VALUES (?)")
      .updateMany(ps)

  def measure[F[_], A](fa: F[A])(implicit F: Sync[F], clock: Clock[F]): F[(A, Long)] =
    for {
      start  <- clock.monotonic(TimeUnit.MILLISECONDS)
      result <- fa
      finish <- clock.monotonic(TimeUnit.MILLISECONDS)
    } yield (result, finish - start)

  private def runBenchmark(xa: Transactor[IO]): IO[Unit] = {
    val idx: Stream[IO, (FUUID, Int)] = Stream
      .range[IO](1, 2000)
      .evalMap(id => FUUID.randomFUUID[IO].map(_ -> id))

    val uuidIO = idx.compile.toList.map(uuidQuery)
    val incrIO = idx.map { case (_, id) => id }.compile.toList.map(incrementQuery)

    for {
      _             <- IO(print("Starts UUID"))
      (_, uuidTime) <- measure(uuidIO >>= (_.transact(xa)))
      _             <- IO(println(s".OK => $uuidTime ms"))
      _             <- IO(print("Starts incr"))
      (_, incrTime) <- measure(incrIO >>= (_.transact(xa)))
      _             <- IO(println(s".OK => $incrTime ms"))

      _ <- IO(println(s"$uuidTime\t$incrTime"))
    } yield ()
  }

  private val getDatabase: Transactor[IO] =
    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/bench",
      "postgres",
      ""
    )
}
