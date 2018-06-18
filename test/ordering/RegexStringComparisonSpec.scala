package ordering

import org.scalatest.{Matchers, WordSpec}

import scala.util.matching.Regex

class RegexStringComparisonSpec extends WordSpec with Matchers {
  "compare regex groups" should {

    implicit val regex: Regex = """^(\d*)\.(\d*)\.(\d*)$""".r

    "determine equality by regex" in new RegexStringComparison {
      compareRegexGroups("1.2.3", "1.2.3") shouldBe 0
    }

    "determine right weighted lexographic inequality by regex in first segment" in new RegexStringComparison {
      compareRegexGroups("0.0.1", "1.0.0") shouldBe -1
    }

    "determine right weighted lexographic inequality by regex in second segment" in new RegexStringComparison {
      compareRegexGroups("0.0.1", "0.1.0") shouldBe -1
    }

    "determine right weighted lexographic inequality by regex in third segment" in new RegexStringComparison {
      compareRegexGroups("0.0.1", "0.0.2") shouldBe -1
    }

    "determine left weighted lexographic inequality by regex in first segment" in new RegexStringComparison {
      compareRegexGroups("1.0.0", "0.0.1") shouldBe 1
    }

    "determine left weighted lexographic inequality by regex in second segment" in new RegexStringComparison {
      compareRegexGroups("0.1.0", "0.0.1") shouldBe 1
    }

    "determine left weighted lexographic inequality by regex in third segment" in new RegexStringComparison {
      compareRegexGroups("0.0.2", "0.0.1") shouldBe 1
    }
  }
}
