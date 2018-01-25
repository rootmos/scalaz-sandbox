import scala.annotation.tailrec

object Nothingness extends App {
  @tailrec
  def diverge[A](): A = diverge[A]()

  trait A
  implicit def `A and ¬A is a contradiction`(implicit a: A, notA: A => Nothing): Nothing = notA(a)

  implicit val notA: A => Nothing = { _ => diverge() }
  implicit val proofOfA = new A {}

  implicit def `Embrace the nothingness`[P, Q](implicit p: P): Q = implicitly[Nothing]

  trait B
  trait C
  val b: B = new C {}
  val i: Int = b
}
