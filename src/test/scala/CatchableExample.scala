import scalaz._, Scalaz._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import scala.util.{Try, Success, Failure}
import scala.concurrent.Future

class CatchableExample extends WordSpec with Matchers with ScalaFutures {

  val ex = new RuntimeException("Oh noes")

  "CatchableExample" should {
    "make Try:s Catchable" in {

      implicit val `Try is Catchable` = new Catchable[Try] {
        def attempt[A](fa: Try[A]): Try[Throwable \/ A] = fa match {
          case Success(x) => Try(\/-(x))
          case Failure(t) => Try(-\/(t))
        }
        def fail[A](err: Throwable): Try[A] = ???
      }

      val C = Catchable[Try]
      C.attempt( Try { throw ex } ) shouldBe Success(-\/(ex))
      C.attempt( Try { 7 } ) shouldBe Success(\/-(7))
    }

    "Future:s are Catchable" in {
      import scala.concurrent.ExecutionContext.Implicits.global
      val C = Catchable[Future]
      whenReady(C.attempt( Future { throw ex } )) { _ shouldBe -\/(ex) }
      whenReady(C.attempt( Future { 7 } )) { _ shouldBe \/-(7) }
    }
  }
}
