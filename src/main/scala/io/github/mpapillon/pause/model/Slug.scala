package io.github.mpapillon.pause.model

import java.text.Normalizer

import io.circe.Encoder

final case class Slug private (value: String) extends AnyVal

object Slug {

  def apply(value: String): Slug = new Slug(slugify(value))

  private def slugify(input: String): String =
    Normalizer
      .normalize(input, Normalizer.Form.NFD)
      .replaceAll("[^\\w\\s-]", "") // Remove all non-word, non-space or non-dash characters
      .replace('-', ' ')            // Replace dashes with spaces
      .trim                         // Trim leading/trailing whitespace (including what used to be leading/trailing dashes)
      .replaceAll("\\s+", "-")      // Replace whitespace (including newlines and repetitions) with single dashes
      .toLowerCase                  // Lowercase the final results

  implicit val slugEncoder: Encoder[Slug] = Encoder.encodeString.contramap(_.value)
}
