package ordering

import scala.annotation.tailrec
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

trait RegexStringComparison {

  implicit val regex: Regex = """(\d+|\w+)""".r

  val WeightedPlaceholder = "0"

  def compareRegexGroups(s1: String, s2: String)(implicit regex: Regex): Int = {
    val l1 = regex.findAllMatchIn(s1).toList.map(_.toString)
    val l2 = regex.findAllMatchIn(s2).toList.map(_.toString)

    val max = Math.max(l1.length, l2.length)

    val zipped = l1.padTo(max, WeightedPlaceholder) zip l2.padTo(max, WeightedPlaceholder)

    compare(zipped)
  }

  @tailrec
  private def compare(segments: Seq[(String, String)]): Int = segments match {
    case (x: String, y: String) :: Nil =>
      compareSegValues(x, y)
    case (x: String, y: String) :: segs =>
      if (compareSegValues(x, y) == 0) compare(segs) else compareSegValues(x, y)
  }

  private def compareSegValues(s1: String, s2: String): Int = Try(s1.toInt - s2.toInt) match {
    case Success(s) => s
    case Failure(e) => comparePreReleaseSegments(s1, s2)
  }

  private def comparePreReleaseSegments(s1: String, s2: String): Int = {
    val diff = rank(s1) - rank(s2)
    if (diff == 0) s1 compareTo s2 else diff
  }

  private def rank(seg: String): Int = seg.toLowerCase match {
    case s if s == "0"                => 0
    case s if s.startsWith("release") => 0
    case s if s.startsWith("final")   => 0
    case s if s.startsWith("rc")      => -1
    case s if s.startsWith("m")       => -2
    case s if s.startsWith("beta")    => -3
    case s if s.startsWith("alpha")   => -4
    case s                            => s.hashCode
  }

}
