import org.scalatest.{WordSpec, Matchers}
import scalaz._

class TraverseExample extends WordSpec with Matchers {
  "TraverseExample" should {
    "sequence \\/" in {
      import \/._
      import std.list._
      import syntax.traverse._
      import syntax.either._

      List(1.right,2.left,3.left).sequenceU shouldBe 2.left

      // Type-inference needs a small push sometimes
      List(1.right[Int],2.right,3.right).sequenceU shouldBe List(1,2,3).right
      List(1.right,2.right[Int],3.right).sequenceU shouldBe List(1,2,3).right

      // Or force it at the end:
      List(1.right,2.right,3.right).sequence[Nothing \/ ?, Int] shouldBe List(1,2,3).right
    }

    "traverse List with Option:s" in {
      import std.list._
      import syntax.traverse._
      import std.option._
      import syntax.std.boolean._

      List(1,2,3).traverse(x => (x > 0) option (x + 1)) shouldBe Some(List(2,3,4))
      List(1,-2,3).traverse(x => (x > 0) option (x + 1)) shouldBe None
    }
  }
}
