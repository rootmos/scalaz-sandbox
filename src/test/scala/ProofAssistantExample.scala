import org.scalatest._
import shapeless.test.illTyped
import scala.reflect.runtime.universe._

class ProofAssistantExample extends WordSpec with Matchers {
  "ProofAssistantExample" should {
    class A
    class B

    "not prove something without proof" in {
      illTyped { """implicitly[A]""" }
    }

    "prove A with proof of A" in {
      implicit val proof = new A
      implicitly[A] shouldBe proof
      illTyped { """implicitly[B]""" }
    }

    "tuples are conjunction" when {
      implicit def conjuction[T, S](implicit t: T, s: S): (T,S) = (t,s)

      "A, B => (A and B)" in {
        implicit val proofA = new A
        implicit val proofB = new B

        implicitly[(A, B)]
      }

      "not A, not B => not (A and B)" in {
        illTyped { """implicitly[(A, B)]""" }
      }

      "A, not B => not (A and B)" in {
        implicit val proof = new A
        illTyped { """implicitly[(A, B)]""" }
      }

      "not A, B => not (A and B)" in {
        implicit val proof = new B
        illTyped { """implicitly[(A, B)]""" }
      }
    }

    "either is disjunction" when {
      sealed trait Proof[T]
      class DisjunctionProofT[T: WeakTypeTag, S: WeakTypeTag](t: Proof[T]) extends Proof[Either[T, S]] {
        override def toString = s"$t => ${weakTypeOf[T]} || ${weakTypeOf[S]}"
      }
      class DisjunctionProofS[T: WeakTypeTag, S: WeakTypeTag](s: Proof[S]) extends Proof[Either[T, S]] {
        override def toString = s"$s => ${weakTypeOf[T]} || ${weakTypeOf[S]}"
      }

      class DisjunctionProofTS[T: WeakTypeTag, S: WeakTypeTag](t: Proof[T], s: Proof[S]) extends Proof[Either[T, S]] {
        override def toString = s"$t && $s => ${weakTypeOf[T]} || ${weakTypeOf[S]}"
      }

      class Axiom[T: WeakTypeTag] extends Proof[T] {
        override def toString = weakTypeOf[T].toString
      }

      object Proof extends LowPriorityDisjunctionProofs {
        def apply[T: WeakTypeTag]: Proof[T] = new Axiom[T]
      }

      trait LowPriorityDisjunctionProofs {
        implicit def disjunctionT[T: WeakTypeTag, S: WeakTypeTag](implicit t: Proof[T]): Proof[Either[T,S]] = new DisjunctionProofT[T, S](t)
        implicit def disjunctionS[T: WeakTypeTag, S: WeakTypeTag](implicit s: Proof[S]): Proof[Either[T,S]] = new DisjunctionProofS[T, S](s)
      }

      implicit def disjunctionTS[T: WeakTypeTag, S: WeakTypeTag](implicit t: Proof[T], s: Proof[S]): Proof[Either[T, S]] = new DisjunctionProofTS(t, s)

      "A, B => (A or B)" in {
        implicit val proofA = Proof[A]
        implicit val proofB = Proof[B]

        println(implicitly[Proof[Either[A, B]]])
      }

      "not A, not B => not (A or B)" in {
        illTyped { """implicitly[Proof[Either[A, B]]]""" }
      }

      "A, not B => (A or B)" in {
        implicit val proof = Proof[A]
        println(implicitly[Proof[Either[A, B]]])
      }

      "not A, B => not (A and B)" in {
        implicit val proof = Proof[B]
        println(implicitly[Proof[Either[A, B]]])
      }
    }
  }
}

