package ordering

import rendering.VersionItem

import scala.util.Sorting

trait VersionItemOrdering {

  implicit object OrderedVersion extends Ordering[VersionItem] with RegexStringComparison {
    def compare(v1: VersionItem, v2: VersionItem): Int = compareRegexGroups(v1.version, v2.version)
  }

  private def quickSort(as: Array[VersionItem]): Array[VersionItem] = {
    Sorting.quickSort(as)
    as
  }

  implicit class OrderedVersionItems(items: Seq[VersionItem]) {

    def ascendingOrder: Seq[VersionItem] = quickSort(items.toArray).toList

    def descendingOrder: Seq[VersionItem] = quickSort(items.toArray).toList.reverse
  }
}
