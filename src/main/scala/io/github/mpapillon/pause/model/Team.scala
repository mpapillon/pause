package io.github.mpapillon.pause.model

import java.time.LocalDate


final case class Team(id: Int, name: String, canonicalName: String, creationDate: LocalDate)