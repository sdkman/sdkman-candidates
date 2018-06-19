package ordering

import org.scalatest.{Matchers, WordSpec}

import scala.util.matching.Regex

class RegexStringComparisonSpec extends WordSpec with Matchers {
  "comparison of extracted regex groups" when {

    implicit val regex: Regex = """(\d+|\w+)""".r

    "groups are numeric" should {

      "determine equality by regex" in new RegexStringComparison {
        compareRegexGroups("1.2.3", "1.2.3") shouldBe 0
      }

      "determine lexicographic inequality by regex in first segment" in new RegexStringComparison {
        compareRegexGroups("1.0.0", "0.0.1") shouldBe 1
        compareRegexGroups("0.0.1", "1.0.0") shouldBe -1
      }

      "determine lexicographic inequality by regex in second segment" in new RegexStringComparison {
        compareRegexGroups("0.1.0", "0.0.1") shouldBe 1
        compareRegexGroups("0.0.1", "0.1.0") shouldBe -1
      }

      "determine lexicographic inequality by regex in third segment" in new RegexStringComparison {
        compareRegexGroups("0.0.2", "0.0.1") shouldBe 1
        compareRegexGroups("0.0.1", "0.0.2") shouldBe -1
      }

      "determine lexicographic inequality by regex in unequal length groupings" in new RegexStringComparison {
        compareRegexGroups("0.1", "0.0.1") shouldBe 1
        compareRegexGroups("0.0.1", "0.1") shouldBe -1
      }
    }

    "groups are alpha or numeric" should {

      "determine lexicographic inequality by regex with alpha segments" in new RegexStringComparison {
        assert(compareRegexGroups("2.0.0.RC1", "2.0.0.M5") > 0)
        assert(compareRegexGroups("2.0.0.M5", "2.0.0.M4") > 0)
        assert(compareRegexGroups("2.0.0.M3", "2.0.0.M3") == 0)
        assert(compareRegexGroups("2.0.0.M3", "2.0.0.RC1") < 0)
        assert(compareRegexGroups("2.0.0.RELEASE", "2.0.0.RC1") > 0)
        assert(compareRegexGroups("2.0.0", "2.0.0.RC1") > 0)
      }
    }

  }
}
