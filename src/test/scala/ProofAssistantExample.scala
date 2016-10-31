import org.scalatest._
import shapeless.test.illTyped

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
      class Proof[T]

      object Proof extends LowPriorityDisjunctionProofs

      trait LowPriorityDisjunctionProofs {
        implicit def disjunction1[T, S](implicit t: Proof[T]): Proof[Either[T,S]] = new Proof[Either[T, S]]
        implicit def disjunction2[T, S](implicit s: Proof[S]): Proof[Either[T,S]] = new Proof[Either[T, S]]
      }

      object Proofs {
        implicit def disjunctionP12[T, S](implicit t: Proof[T], s: Proof[S]): Proof[Either[T, S]] = new Proof[Either[T, S]]
      }

      import Proofs._

      "A, B => (A or B)" in {
        implicit val proofA = new Proof[A]
        implicit val proofB = new Proof[B]

        implicitly[Proof[Either[A, B]]]
      }

      "not A, not B => not (A or B)" in {
        illTyped { """implicitly[Proof[Either[A, B]]]""" }
      }

      "A, not B => (A or B)" in {
        implicit val proof = new Proof[A]
        implicitly[Proof[Either[A, B]]]
      }

      "not A, B => not (A and B)" in {
        implicit val proof = new Proof[B]
        implicitly[Proof[Either[A, B]]]
      }
    }
  }
}

