package io.github.mpapillon.pause.domains.teams

import java.time.LocalDate

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import io.chrisdavenport.fuuid.FUUID
import io.chrisdavenport.fuuid.http4s.FUUIDVar
import io.circe._
import io.circe.syntax._
import io.github.mpapillon.pause.domains.members.Members
import io.github.mpapillon.pause.model.Team
import io.github.mpapillon.pause.syntax.response._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}

object TeamsRoutes {
  import io.circe.generic.semiauto._

  private[this] final case class TeamCreation(name: String, canonicalName: String)

  private[this] implicit val teamCreationDecoder: Decoder[TeamCreation]                            = deriveDecoder
  private[this] implicit def teamCreationEntityDecoder[F[_]: Sync]: EntityDecoder[F, TeamCreation] = jsonOf

  def routes[F[_]: Sync: Teams: Members](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    implicit val handleErrors: TeamsError => F[Response[F]] = {
      case TeamsError.TeamNotFound(name) =>
        NotFound(s"Team $name does not exists".asJson)
      case TeamsError.TeamAlreadyExists(name) =>
        Conflict(s"Team $name already exists".asJson)
      case TeamsError.MemberNotFound(id) =>
        NotFound(s"Member with id $id does not exists.".asJson)
      case TeamsError.MembershipAlreadyExists(teamName, memberID) =>
        Conflict(s"The team $teamName already contains member $memberID".asJson)
      case TeamsError.MembershipDoesNotExists(teamName, memberID) =>
        NotFound(s"The member $memberID is not part of the team $teamName".asJson)
    }

    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          teams <- Teams[F].all
          resp  <- Ok(teams.asJson)
        } yield resp

      case req @ POST -> Root =>
        for {
          TeamCreation(name, cName) <- req.as[TeamCreation]

          id   <- FUUID.randomFUUID
          team = Team(id, name, cName, LocalDate.now())
          resp <- Teams[F].add(team).toResponse(_ => Created(team.asJson))
        } yield resp

      case GET -> Root / name =>
        OptionT(Teams[F].get(name))
          .toRight(TeamsError.TeamNotFound(name))
          .toResponse(team => Ok(team.asJson))

      case GET -> Root / name / "members" =>
        Teams[F].membersOf(name).toResponse(members => Ok(members.asJson))

      case PUT -> Root / name / "members" / FUUIDVar(memberID) =>
        Teams[F].join(name, memberID).toResponse(_ => Created())

      case DELETE -> Root / name / "members" / FUUIDVar(memberID) =>
        Teams[F].leave(name, memberID).toResponse(_ => Ok())

      case GET -> Root / name / "managers" =>
        Teams[F].managersOf(name).toResponse(managers => Ok(managers.asJson))
    }
  }
}
