package io.github.mpapillon.pause.http4s

import io.github.mpapillon.pause.model.Slug

object SlugVar {

  def unapply(str: String): Option[Slug] =
    if (!str.isEmpty)
      Some(Slug(str))
    else
      None

}
