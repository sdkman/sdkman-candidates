package rendering

import org.scalatest.{Matchers, WordSpec}

class VersionListColumnLengthSpec extends WordSpec with Matchers {
  "VersionListColumnLength" should {

    "calculate column length for an empty version list" in new UnderTest {
      toColumnLength(0) shouldBe 15
    }

    "calculate column length for a partial version list" in new UnderTest {
      toColumnLength(10) shouldBe 15
    }

    "calculate column length for a full version list" in new UnderTest {
      toColumnLength(60) shouldBe 15
    }

    "calculate column length for a version list exceeding by one row" in new UnderTest {
      toColumnLength(61) shouldBe 16
      toColumnLength(62) shouldBe 16
      toColumnLength(63) shouldBe 16
      toColumnLength(64) shouldBe 16
    }

    "calculate column length for a version list exceeding by two rows" in new UnderTest {
      toColumnLength(65) shouldBe 17
      toColumnLength(66) shouldBe 17
      toColumnLength(67) shouldBe 17
      toColumnLength(68) shouldBe 17
    }

    "calculate column length for a version list exceeding by three rows" in new UnderTest {
      toColumnLength(69) shouldBe 18
      toColumnLength(70) shouldBe 18
      toColumnLength(71) shouldBe 18
      toColumnLength(72) shouldBe 18
    }
  }

  private class UnderTest extends VersionListColumnLength {
    override val DefaultVersionCount = 60
  }
}
