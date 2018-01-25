import org.scalatest._
import scalaz._
import Scalaz._
import shapeless.test.illTyped
import scala.language.postfixOps

class YonedaExample extends WordSpec with Matchers {

  case class F[T](t: T)

  sealed trait Tree[T]
  case class Leaf[T](x: T) extends Tree[T]
  case class Node[T](l: Tree[T], x: T, r: Tree[T]) extends Tree[T]

  def depth[T](n: Int, x: T): Tree[T] =
    if (n == 0) Leaf(x)
    else {
      val subtree = depth(n-1, x)
      Node(subtree, x, subtree)
    }

  object Instances {
    implicit val `F is a Functor` = new Functor[F] {
      def map[A, B](fa: F[A])(f: A => B): F[B] = F(f(fa.t))
    }

    implicit val `Tree is a Functor` = new Functor[Tree] {
      def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
        case Leaf(x) => Leaf(f(x))
        case Node(l, x, r) => Node(map(l)(f), f(x), map(r)(f))
      }
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

    "map on tree" in {
      import Instances._
      val t = depth(20, "hello")
      t map (_ + "5") map (_ + "7") map (_ + "hehe")
    }

    "map on tree faster with coyoneda" in {
      val t = depth(20, "hello")
      val co = Coyoneda(t)(_ + "5") map (_ + "7") map (_ + "hehe")

      {
        import Instances._
        co.run
      }
    }
  }
}
