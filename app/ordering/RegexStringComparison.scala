package ordering

import scala.annotation.tailrec
import scala.util.matching.Regex

trait RegexStringComparison {

  implicit val regex: Regex = """(\d+|\w+)""".r

  val WeightedPlaceholder = "Z"

  def compareRegexGroups(s1: String, s2: String)(implicit regex: Regex): Int = {
    val l1 = regex.findAllMatchIn(s1).toList.map(_.toString)
    val l2 = regex.findAllMatchIn(s2).toList.map(_.toString)

    val max = Math.max(l1.length, l2.length)

    val zipped = l1.padTo(max, WeightedPlaceholder) zip l2.padTo(max, WeightedPlaceholder)

    @tailrec
    def compare(segments: Seq[(String, String)]): Int = segments match {
      case (x: String, y: String) :: Nil =>
        x compareTo y
      case (x: String, y: String) :: segs =>
        if((x compareTo y) == 0) compare(segs) else x compareTo y
    }

    compare(zipped)
  }

}
