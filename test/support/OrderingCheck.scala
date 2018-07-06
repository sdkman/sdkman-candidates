package support

import io.sdkman.repos.Version
import org.scalatest.{FunSpec, Matchers}

import scala.annotation.tailrec

trait OrderingCheck {
  @tailrec
  final def orderCheck(actual: Seq[Version], expected: Seq[Version]): Boolean = (actual, expected) match {
    case (a :: Nil, e :: Nil) => a == e
    case (a :: as, e :: Nil) => false
    case (a :: Nil, e:: es) => if(a == e) true else orderCheck(a :: Nil, es)
    case (a :: as, e :: es) => if(a == e) orderCheck(as, es) else orderCheck(a :: as, es)
  }
}

class OrderingCheckTest extends FunSpec with OrderingCheck with Matchers {

  it("should successfully compare sub-list ordering") {
    orderCheck(List("1").versions, List("1").versions) shouldBe true
    orderCheck(List("1").versions, List("2").versions)shouldBe false

    orderCheck(List("1", "2").versions, List("1", "2").versions) shouldBe true
    orderCheck(List("2", "1").versions, List("1", "2").versions) shouldBe false

    orderCheck(List("5").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("4").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("3").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("2").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("1").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("0").versions, List("5", "4", "3", "2", "1").versions) shouldBe false

    orderCheck(List("5", "4").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("4", "3").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("3", "2").versions, List("5", "4", "3", "2", "1").versions) shouldBe true
    orderCheck(List("2", "1").versions, List("5", "4", "3", "2", "1").versions) shouldBe true

    orderCheck(List("1").versions, List("1").versions) shouldBe true
    orderCheck(List("1", "2").versions, List("2").versions) shouldBe false

    orderCheck(List("1", "2", "3", "4").versions, List("1").versions) shouldBe false
    orderCheck(List("1", "2", "3", "4").versions, List("1", "2").versions) shouldBe false
    orderCheck(List("1", "2", "3", "4").versions, List("1", "2", "3").versions) shouldBe false
    orderCheck(List("1", "2", "3", "4").versions, List("1", "2", "3", "4").versions) shouldBe true
    orderCheck(List("2", "3", "4").versions, List("1", "2", "3", "4").versions) shouldBe true
    orderCheck(List("3", "4").versions, List("1", "2", "3", "4").versions) shouldBe true
    orderCheck(List("4").versions, List("1", "2", "3", "4").versions) shouldBe true

    orderCheck(List("1", "2", "3", "4").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("2", "3", "4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("1", "3", "4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("1", "2", "4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("1", "2", "3", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true

    orderCheck(List("1", "2").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("2", "3").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("3", "4").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true

    orderCheck(List("1", "3", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("2", "3", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
    orderCheck(List("2", "4").versions, List("1", "2", "3", "4", "5").versions) shouldBe true

    orderCheck(List("1", "4", "2", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe false
    orderCheck(List("1", "2", "5", "4").versions, List("1", "2", "3", "4", "5").versions) shouldBe false
    orderCheck(List("2", "1", "4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe false

    orderCheck(List("1", "2", "3", "4", "5").versions, List("2", "3", "4", "5").versions) shouldBe false
    orderCheck(List("2", "3", "4", "5").versions, List("1", "2", "3", "4", "5").versions) shouldBe true
  }

  implicit class VersionString(vs: List[String]) {
    def versions: List[Version] = vs.map(Version("candidate", _, "platform", "url"))
  }
}
