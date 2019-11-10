package io.github.mpapillon.pause.domain.person

import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.github.mpapillon.pause.model.Person
import io.github.mpapillon.pause.syntax.response._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

object PersonsService {

  private final case class MemberCreation(firstName: String, lastName: String, email: Option[String])

  private implicit val memberCreationDecoder: Decoder[MemberCreation] = deriveDecoder

  def apply[F[_]: Sync](members: Persons[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    implicit val handleErrors: PersonsError => F[Response[F]] = {
      case PersonsError.PersonAlreadyExist(id) =>
        Conflict(s"Person with id $id already exists.".asJson)
      case PersonsError.PersonNotFound(id) =>
        NotFound(s"The person $id does not exists.".asJson)
    }

    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          members <- members.all
          resp    <- Ok(members.asJson)
        } yield resp

      case req @ POST -> Root =>
        for {
          MemberCreation(firstName, lastName, email) <- req.as[MemberCreation]

          id     <- FUUID.randomFUUID
          member = Person(id, firstName, lastName, email)
          resp   <- EitherT(members.add(member)).toResponse(_ => Created(member.asJson))
        } yield resp

      case GET -> Root / FUUIDVar(id) =>
        OptionT(members.get(id))
          .toRight(PersonsError.PersonNotFound(id))
          .toResponse(member => Ok(member.asJson))

      case DELETE -> Root / FUUIDVar(id) =>
        members.remove(id).map(_ > 0).ifM(Ok(), NotFound())
    }
  }
}
