import scalaz._
import Scalaz._

import org.scalatest._

class MonadErrorExample extends WordSpec with Matchers {

  case class Result(i: Int)
  implicit val `Result is a Semigroup`: Semigroup[Result] = new Semigroup[Result] {
    def append(r1: Result, r2: => Result): Result = Result(r1.i + r2.i)
  }

  sealed trait Error
  case object SillyError extends Error

  def calculation[F[_]](working: Boolean)(implicit F: MonadError[F, Error]): F[Result] = {
    if (working)
      F.point(Result(7))
    else
      F.raiseError(SillyError)
  }

  "MonadErrorExample" should {
    "run with Either" in {
      calculation[Either[Error, ?]](true) shouldBe Right(Result(7))
      calculation[Either[Error, ?]](false) shouldBe Left(SillyError)
    }

    "run with disjunction" in {
      calculation[Error \/ ?](true) shouldBe \/-(Result(7))
      calculation[Error \/ ?](false) shouldBe -\/(SillyError)
    }

    "run in a for-comprehension" in {
      def sum[F[_]](implicit F: MonadError[F, Error]): F[Result] =
        for {
          i <- calculation(true)
          j <- calculation(true)
        } yield i |+| j

      sum[Either[Error, ?]] shouldBe Right(Result(14))
    }

    "fail in a for-comprehension" in {
      def sum[F[_]](implicit F: MonadError[F, Error]): F[Result] =
        for {
          i <- calculation(true)
          j <- calculation(false)
        } yield i |+| j

      sum[Error \/ ?] shouldBe -\/(SillyError)
    }
  }
}
