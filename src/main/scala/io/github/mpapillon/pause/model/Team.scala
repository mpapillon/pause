package io.github.mpapillon.pause.model

import java.time.LocalDate

import io.circe.Encoder

final case class Team(id: Int, name: String, slug: Slug, creationDate: LocalDate)

object Team {

  implicit val teamEncoder: Encoder[Team] =
    Encoder.forProduct3("name", "slug", "creationDate")(t => (t.name, t.slug, t.creationDate))
}
