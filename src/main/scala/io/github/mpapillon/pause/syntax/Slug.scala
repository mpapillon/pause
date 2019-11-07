package io.github.mpapillon.pause.syntax

import io.github.mpapillon.pause.model.Slug

class StringToSlug(s: String) {
  def slugify: Slug = Slug.slugify(s)
}

trait SlugOps {

  implicit def stringToSlug(s: String): StringToSlug = new StringToSlug(s)
}
