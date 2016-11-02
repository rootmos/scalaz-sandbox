import org.scalatest._
import scalaz._
import Scalaz._
import shapeless.test.illTyped

class YonedaExample extends WordSpec with Matchers {

  case class F[T](t: T)

  object Instances {
    implicit val `F is a Functor` = new Functor[F] {
      def map[A, B](fa: F[A])(f: A => B): F[B] = F(f(fa.t))
    }
  }

  "Yoneda" should {
    "remember the functor" in {
      val ya = {
        import Instances._
        Yoneda(F(7))
      }

      illTyped { """F(7) map { _.toString } shouldBe F("7")""" }

      (ya map { _.toString } run) shouldBe F("7")
    }
  }

  "Coyoneda" should {
    "make a free functor" in {
      val fa = F(7)
      val co = Coyoneda(fa) { _.toString }

      illTyped { """co.run""" }

      {
        import Instances._
        co.run shouldBe F("7")
      }
    }
  }
}
