package ordering

import io.sdkman.repos.Version

import scala.util.Sorting

trait VersionOrdering {

  implicit object OrderedVersion extends Ordering[Version] with RegexStringComparison {
    def compare(v1: Version, v2: Version): Int = compareRegexGroups(v1.version, v2.version)
  }

  private def quickSort(as: Array[Version]): Array[Version] = {
    Sorting.quickSort(as)
    as
  }

  def ascendingOrder(versions: Seq[Version]): List[Version] = quickSort(versions.toArray).toList

  def descendingOrder(versions: Seq[Version]): List[Version] = ascendingOrder(versions).reverse

}
