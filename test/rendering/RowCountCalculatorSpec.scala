package rendering

import org.scalatest.{Matchers, WordSpec}

class RowCountCalculatorSpec extends WordSpec with Matchers {
  "VersionListColumnLength" should {

    "calculate column length for an empty version list" in new UnderTest {
      asRowCount(0) shouldBe 15
    }

    "calculate column length for a partial version list" in new UnderTest {
      asRowCount(10) shouldBe 15
    }

    "calculate column length for a full version list" in new UnderTest {
      asRowCount(60) shouldBe 15
    }

    "calculate column length for a version list exceeding by one row lower boundary" in new UnderTest {
      asRowCount(61) shouldBe 16
    }

    "calculate column length for a version list exceeding by one row upper boundary" in new UnderTest {
      asRowCount(64) shouldBe 16
    }

    "calculate column length for a version list exceeding by two rows lower boundary" in new UnderTest {
      asRowCount(65) shouldBe 17
    }

    "calculate column length for a version list exceeding by two rows upper boundary" in new UnderTest {
      asRowCount(68) shouldBe 17
    }

    "calculate column length for a version list exceeding by three rows lower boundary" in new UnderTest {
      asRowCount(69) shouldBe 18
    }

    "calculate column length for a version list exceeding by three rows upper boundary" in new UnderTest {
      asRowCount(72) shouldBe 18
    }
  }

  private class UnderTest extends RowCountCalculator {
    override val MinCountThreshold = 60
  }
}
