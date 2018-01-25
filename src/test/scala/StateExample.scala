import org.scalatest.{WordSpec, Matchers}
import scalaz._

class StateExample extends WordSpec with Matchers {
  "State" should {
    "have a simple usage with State" in {
      import State._

      val si = for {
        init <- get[Int]
        _ <- put(7)
        _ <- modify { (i: Int) => i + 1 }
      } yield init

      si.run(1) shouldBe ((8, 1))
    }

    "combine error handling with StateT over \\/ (successful example)" in {
      type F[A] = StateT[Throwable \/ ?, Int, A]
      val S = StateT.stateTMonadState[Int, Throwable \/ ?]

      val fa: F[Int] = for {
        i <- S.get
        _ <- S.put(7)
      } yield (i + 1)

      fa.run(1) shouldBe \/-((7, 2))
    }

    "combine error handling with StateT over \\/ (failing example)" in {
      type F[A] = StateT[Throwable \/ ?, Int, A]
      val S = StateT.stateTMonadState[Int, Throwable \/ ?]

      import StateT._
      import syntax.either._

      def test(i: Int): F[Unit] = {
        if (i > 0) S.point(()) else StateMonadTrans.liftMU(-\/(new RuntimeException("Ops!")))
      }

      //test[Throwable \/ ?](1) should matchPattern { case \/-(_) => }
      //test[Throwable \/ ?](-1) should matchPattern { case -\/(_) => }

      //import MonadError._
      //import MonadTrans._
      //test[F](-1).run(0) should matchPattern { case -\/(_) => }

      //val fa: F[Int] = for {
      //  i <- S.get
      //  () <- test(i)
      //} yield (i + 1)

      //fa.run(1) shouldBe \/-((7, 2))
    }
  }
}
