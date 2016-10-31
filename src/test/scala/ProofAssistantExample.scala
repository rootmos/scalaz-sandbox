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
      case class DisjunctionProof1[T, S](e: Either[Proof[T], Proof[S]]) extends Proof[Either[T, S]]
      case class DisjunctionProof2[T, S](t: Proof[T], s: Proof[S]) extends Proof[Either[T, S]]
      class Axiom[T: WeakTypeTag] extends Proof[T] {
        override def toString = s"Axiom for ${weakTypeOf[T]}"
      }

      object Proof extends LowPriorityDisjunctionProofs {
        def apply[T: WeakTypeTag]: Proof[T] = new Axiom[T]
      }

      trait LowPriorityDisjunctionProofs {
        implicit def disjunction1[T, S](implicit t: Proof[T]): Proof[Either[T,S]] = DisjunctionProof1[T, S](Left(t))
        implicit def disjunction2[T, S](implicit s: Proof[S]): Proof[Either[T,S]] = DisjunctionProof1[T, S](Right(s))
      }

      implicit def disjunction12[T, S](implicit t: Proof[T], s: Proof[S]): Proof[Either[T, S]] = DisjunctionProof2(t, s)

      "A, B => (A or B)" in {
        implicit val proofA = Proof[A]
        implicit val proofB = Proof[B]

        implicitly[Proof[Either[A, B]]]
      }

      "not A, not B => not (A or B)" in {
        illTyped { """implicitly[Proof[Either[A, B]]]""" }
      }

      "A, not B => (A or B)" in {
        implicit val proof = Proof[A]
        implicitly[Proof[Either[A, B]]]
      }

      "not A, B => not (A and B)" in {
        implicit val proof = Proof[B]
        implicitly[Proof[Either[A, B]]]
      }
    }
  }
}

