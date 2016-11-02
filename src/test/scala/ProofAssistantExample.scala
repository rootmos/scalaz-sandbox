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
      
      sealed trait Disjunction[T, S] {
        def toEither: Either[T, S] = this match {
          case FromT(t) => Left(t)
          case FromS(s) => Right(s)
          case FromTS(t, _) => Left(t)
        }
      }
      case class FromT[T, S](t: T) extends Disjunction[T, S]
      case class FromS[T, S](s: S) extends Disjunction[T, S]
      case class FromTS[T, S](t: T, s: S) extends Disjunction[T, S]

      object Disjunction extends LowPriorityDisjunctionProofs

      trait LowPriorityDisjunctionProofs {
        implicit def disjunctionT[T, S](implicit t: T): Disjunction[T, S] = FromT(t)
        implicit def disjunctionS[T, S](implicit s: S): Disjunction[T, S] = FromS(s)
      }

      implicit def disjunctionTS[T, S](implicit t: T, s: S): Disjunction[T, S] = FromTS(t, s)

      "A, B => (A or B)" in {
        implicit val proofA = new A
        implicit val proofB = new B

        implicitly[Disjunction[A, B]]
      }

      "not A, not B => not (A or B)" in {
        illTyped { """implicitly[Disjunction[A, B]]""" }
      }

      "A, not B => (A or B)" in {
        implicit val proof = new A
        implicitly[Disjunction[A, B]].toEither shouldBe Left(proof)
      }

      "not A, B => (A or B)" in {
        implicit val proof = new B
        implicitly[Disjunction[A, B]].toEither shouldBe Right(proof)
      }
    }

    "negation should use Nothing" when {
      trait A
      class Nothingness extends Exception
      implicit def notA(implicit a: A): Nothing = throw new Nothingness

      "with proof of the negation the world should be absurd" in {
        implicit val proofA = new A {}
        // TODO: Better scalatest dsl for this? should be thrownBy didn't work out of the box
        try {
          implicitly[Nothing]
        } catch {
          case _: Nothingness =>
        }
      }

      "without proof I can't deduce bottom" in {
        illTyped { """implicitly[Nothing]""" }
      }
    }
  }
}

