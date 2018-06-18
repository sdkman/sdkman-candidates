package ordering

import scala.annotation.tailrec
import scala.util.matching.Regex

trait RegexStringComparison {

  def compareRegexGroups(s1: String, s2: String)(implicit regex: Regex): Int = {
    val regex(x1, x2, x3) = s1
    val regex(y1, y2, y3) = s2

    val zipped = List(x1, x2, x3) zip List(y1, y2, y3)

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
