package io.github.mpapillon.pause.domain.teams

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe._
import io.circe.syntax._
import io.github.mpapillon.pause.http4s.SlugVar
import io.github.mpapillon.pause.model.Team
import io.github.mpapillon.pause.syntax.response._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object TeamsRoutes {

  private[this] case class TeamName(value: String) extends AnyVal

  private[this] implicit val teamCreationDecoder: Decoder[TeamName] =
    Decoder.forProduct1("name")((name: String) => TeamName(name))

  private[this] implicit val teamDecoder: Encoder[Team] =
    Encoder.forProduct3("name", "slug", "creationDate")(t => (t.name, t.slug, t.creationDate))

  def apply[F[_]: Sync](teams: Teams[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    implicit val handleErrors: TeamsError => F[Response[F]] = {
      case TeamsError.TeamNotFound(slug) =>
        NotFound(s"Team with slug ${slug.value} does not exists".asJson)
      case TeamsError.TeamAlreadyExists(slug) =>
        Conflict(s"Team with slug ${slug.value} already exists".asJson)
      case TeamsError.MemberNotFound(id) =>
        NotFound(s"Member with id $id does not exists.".asJson)
      case TeamsError.MembershipAlreadyExists(slug, memberID) =>
        Conflict(s"The team with slug ${slug.value} already contains member $memberID".asJson)
      case TeamsError.MembershipDoesNotExists(slug, memberID) =>
        NotFound(s"The member $memberID is not part of the team with slug ${slug.value}".asJson)
    }

    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          teams <- teams.all
          resp  <- Ok(teams.asJson)
        } yield resp

      case req @ POST -> Root =>
        for {
          name <- req.as[TeamName]
          resp <- teams.add(name.value).toResponse(t => Created(t.asJson))
        } yield resp

      case GET -> Root / SlugVar(slug) =>
        OptionT(teams.get(slug))
          .toRight(TeamsError.TeamNotFound(slug))
          .toResponse(team => Ok(team.asJson))

      case GET -> Root / SlugVar(slug) / "members" =>
        teams.membersOf(slug).toResponse(members => Ok(members.asJson))

      case PUT -> Root / SlugVar(slug) / "members" / FUUIDVar(memberId) =>
        teams.join(slug, memberId).toResponse(_ => Created())

      case DELETE -> Root / SlugVar(slug) / "members" / FUUIDVar(memberId) =>
        teams.leave(slug, memberId).toResponse(_ => Ok())

      case GET -> Root / SlugVar(slug) / "managers" =>
        teams.managersOf(slug).toResponse(managers => Ok(managers.asJson))
    }
  }
}
