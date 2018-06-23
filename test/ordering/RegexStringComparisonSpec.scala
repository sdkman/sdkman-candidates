package ordering

import org.scalatest.{Matchers, WordSpec}

class RegexStringComparisonSpec extends WordSpec with Matchers {
  "comparison of extracted regex groups" when {

    "groups are numeric" should {

      "determine equality by regex" in new RegexStringComparison {
        compareRegexGroups("1.2.3", "1.2.3") shouldBe 0
      }

      "determine lexicographic inequality by regex in first segment" in new RegexStringComparison {
        assertPositive(compareRegexGroups("1.0.0", "0.0.1"))
        assertNegative(compareRegexGroups("0.0.1", "1.0.0"))
      }

      "determine lexicographic inequality by regex in second segment" in new RegexStringComparison {
        assertPositive(compareRegexGroups("0.1.0", "0.0.1"))
        assertNegative(compareRegexGroups("0.0.1", "0.1.0"))
      }

      "determine lexicographic inequality by regex in third segment" in new RegexStringComparison {
        assertPositive(compareRegexGroups("0.0.2", "0.0.1"))
        assertNegative(compareRegexGroups("0.0.1", "0.0.2"))
      }

      "determine lexicographic inequality by regex in unequal length groupings" in new RegexStringComparison {
        assertPositive(compareRegexGroups("0.1", "0.0.1"))
        assertNegative(compareRegexGroups("0.0.1", "0.1"))
        assertPositive(compareRegexGroups("0.1.1", "0.1"))
      }

      "determine ordering of 10 and 9 in equal length groups" in new RegexStringComparison {
        assertPositive(compareRegexGroups("10.0.0", "9.0.0"))
      }
    }

    "groups are alpha or numeric" should {

      "determine lexicographic inequality by regex with alpha segments" in new RegexStringComparison {
        assertPositive(compareRegexGroups("2.0.0.RC1", "2.0.0.M5"))
        assertPositive(compareRegexGroups("2.0.0.M5", "2.0.0.M4"))
        assertZero(compareRegexGroups("2.0.0.M3", "2.0.0.M3"))
        assertNegative(compareRegexGroups("2.0.0.M3", "2.0.0.RC1"))
        assertPositive(compareRegexGroups("2.0.0.RELEASE", "2.0.0.RC1"))
        assertPositive(compareRegexGroups("2.0.0.0", "2.0.0.RC1"))
        assertPositive(compareRegexGroups("2.0.0", "2.0.0.RC1"))

        assertPositive(compareRegexGroups("10.0.1-zulu", "10.0.1-oracle"))
        assertPositive(compareRegexGroups("10.0.1-oracle", "10.0.1-openjdk"))
      }

    }
  }

  private def assertPositive(i: Int) = assert(i > 0)
  private def assertNegative(i: Int) = assert(i < 0)
  private def assertZero(i: Int) = assert(i == 0)
}
