import org.scalatest._
import shapeless.test.illTyped

class ProofAssistantExample extends WordSpec with Matchers {
  "ProofAssistantExample" should {
    class A
    class B
    class Proof[P]

    "not prove something without proof" in {
      illTyped { """implicitly[Proof[A]]""" }
    }

    "prove A with proof of A" in {
      implicit val proof = new Proof[A]
      implicitly[Proof[A]] shouldBe proof
      illTyped { """implicitly[Proof[B]]""" }
    }

    "tuples are conjunction" when {
      implicit def conjuction[T, S](implicit t: T, s: S): (T,S) = (t,s)

      "A, B => (A and B)" in {
        implicit val proofA = new Proof[A]
        implicit val proofB = new Proof[B]

        implicitly[(Proof[A], Proof[B])]
      }

      "not A, not B => not (A and B)" in {
        illTyped { """implicitly[(Proof[A], Proof[B])]""" }
      }

      "A, not B => not (A and B)" in {
        implicit val proof = new Proof[A]
        illTyped { """implicitly[(Proof[A], Proof[B])]""" }
      }

      "not A, B => not (A and B)" in {
        implicit val proof = new Proof[B]
        illTyped { """implicitly[(Proof[A], Proof[B])]""" }
      }
    }

    "either is disjunction" when {
      implicit def disjunction1[T, S](implicit t: T): Either[T,S] = Left(t)
      implicit def disjunction2[T, S](implicit s: S): Either[T,S] = Right(s)

      // TODO: Can this ever be unambiguous?
      //"A, B => (A or B)" in {
      //  implicit val proofA = new Proof[A]
      //  implicit val proofB = new Proof[B]

      //  implicitly[Either[Proof[A], Proof[B]]]
      //}

      "not A, not B => not (A or B)" in {
        illTyped { """implicitly[Either[Proof[A], Proof[B]]]""" }
      }

      "A, not B => (A or B)" in {
        implicit val proof = new Proof[A]
        implicitly[Either[Proof[A], Proof[B]]]
      }

      "not A, B => not (A and B)" in {
        implicit val proof = new Proof[B]
        implicitly[Either[Proof[A], Proof[B]]]
      }
    }
  }
}

