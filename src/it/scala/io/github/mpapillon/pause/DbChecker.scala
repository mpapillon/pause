package io.github.mpapillon.pause

import cats.effect.{ContextShift, IO, Resource}
import cats.syntax.flatMap._
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.flywaydb.core.Flyway
import org.scalatest.Assertions
import pureconfig.module.catseffect.loadConfigF
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import pureconfig.module.http4s._

import scala.concurrent.ExecutionContext

trait DbChecker extends IOChecker { self: Assertions =>

  implicit def cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val dbAlg: Database[IO, Unit] = new Database[IO, Unit] {

    private val config = loadConfigF[IO, Configuration.Database]("pause.it.db")

    override val transactor: Resource[IO, Aux[IO, Unit]] =
      Resource.liftF {
        config.map { conf =>
          Transactor.fromDriverManager[IO](
            "org.postgresql.Driver",
            conf.host.renderString,
            conf.user,
            conf.password
          )
        }
      }


    override def validate(): IO[Unit] = IO.unit

    override def migrate(): IO[Int] =
      config.flatMap { conf =>
        IO.delay {
          Flyway
            .configure()
            .dataSource(conf.host.renderString, conf.user, conf.password)
            .load()
            .migrate()
        }
      }
  }

  override val transactor: Transactor[IO] = (dbAlg.migrate() >> dbAlg.transactor.allocated).unsafeRunSync()._1
}
