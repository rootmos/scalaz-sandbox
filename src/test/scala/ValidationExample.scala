import org.scalatest.{WordSpec, Matchers}

import scalaz._, Scalaz._, NonEmptyList._

class ValidationExample extends WordSpec with Matchers {
  "ValidationExample" should {
    "collect errors" in {
      case class Raw(i: Int, s: String)
      case class Parsed private (i: Int, s: String)
      def parse(raw: Raw): ValidationNel[String, Parsed] = {
        Applicative[ValidationNel[String, ?]].apply2(
          raw.i.success.ensure("negative")(_ >= 0).toValidationNel,
          raw.s.success.ensure("empty")(_.nonEmpty).toValidationNel) (Parsed)
      }

      parse(Raw(-1, "")) shouldBe nels("negative", "empty").failure
      parse(Raw(1, "")) shouldBe nels("empty").failure
      parse(Raw(1, "a")) shouldBe Parsed(1, "a").success
    }

    "collect errors in a for-comprehension" in {
      case class Raw(i: Int, s: String)
      case class Parsed(i: Int, s: String)

      def parse(raw: Raw): Validation[String, Parsed] = {
        import Validation.FlatMap._
        for {
          i <- raw.i.success.ensure("negative")(_ >= 0)
          s <- raw.s.success.ensure("empty")(_.nonEmpty)
        } yield Parsed(i, s)
      }

      parse(Raw(-1, "")) shouldBe "negative".failure
      parse(Raw(1, "")) shouldBe "empty".failure
      parse(Raw(1, "a")) shouldBe Parsed(1, "a").success
    }
  }
}
