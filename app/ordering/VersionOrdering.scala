package ordering

import io.sdkman.repos.Version

import scala.util.Sorting

trait VersionOrdering {

  implicit object OrderedVersion extends Ordering[Version] with RegexStringComparison {
    def compare(v1: Version, v2: Version): Int = compareRegexGroups(v1.version, v2.version)
  }

  def reverseOrder(versions: Seq[Version]): List[Version] = {
    val vs = versions.toArray
    Sorting.quickSort(vs)
    vs.reverse.toList
  }
}
