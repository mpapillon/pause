package io.github.mpapillon.pause.syntax

import io.github.mpapillon.pause.model.Slug

class StringToSlug(s: String) {
  def slug = Slug(s)
}

trait SlugOps {

  implicit def stringToSlug(s: String): StringToSlug = new StringToSlug(s)
}
