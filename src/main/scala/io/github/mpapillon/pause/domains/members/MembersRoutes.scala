package io.github.mpapillon.pause.domains.members

import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.github.mpapillon.pause.model.Member
import io.github.mpapillon.pause.syntax.response._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}

object MembersRoutes {
  private[this] final case class MemberCreation(firstName: String, lastName: String, email: Option[String])

  private[this] implicit val memberCreationDecoder: Decoder[MemberCreation]                            = deriveDecoder
  private[this] implicit def memberCreationEntityDecoder[F[_]: Sync]: EntityDecoder[F, MemberCreation] = jsonOf

  def routes[F[_]: Sync: Members](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    implicit val handleErrors: MembersError => F[Response[F]] = {
      case MembersError.MemberAlreadyExist(id) =>
        Conflict(s"Member with id $id already exists.".asJson)
      case MembersError.MemberNotFound(id) =>
        NotFound(s"The member $id does not exists.".asJson)
    }

    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          members <- Members[F].all
          resp    <- Ok(members.asJson)
        } yield resp

      case req @ POST -> Root =>
        for {
          MemberCreation(firstName, lastName, email) <- req.as[MemberCreation]

          id     <- FUUID.randomFUUID
          member = Member(id, firstName, lastName, email)
          resp   <- EitherT(Members[F].add(member)).toResponse(_ => Created(member.asJson))
        } yield resp

      case GET -> Root / FUUIDVar(id) =>
        OptionT(Members[F].get(id))
          .toRight(MembersError.MemberNotFound(id))
          .toResponse(member => Ok(member.asJson))

      case DELETE -> Root / FUUIDVar(id) =>
        for {
          nbOfRemoves <- Members[F].remove(id)
          resp        <- if (nbOfRemoves > 0) Ok() else NotFound()
        } yield resp
    }
  }
}
