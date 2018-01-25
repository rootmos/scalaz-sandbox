import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ThirdPartyA {
  def complicatedIncrement(i: Int): Int = i match {
    case i if i < -1 => throw new VendorAException
    case i => i + 1
  }

  class VendorAException extends RuntimeException
}

object ThirdPartyB {
  def complicatedIncrement(i: Int): Int = i match {
    case i if i < 0 => throw new VendorBException
    case i => i + 1
  }

  class VendorBException extends RuntimeException
}

object DiligentProgram {
  def foo(i: Int): Int = ThirdPartyA.complicatedIncrement(i)
}
