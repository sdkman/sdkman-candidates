package ordering

import rendering.VersionItem

import scala.util.Sorting

trait JavaVersionItemOrdering {

  implicit object OrderedJavaVersion extends Ordering[VersionItem] with RegexStringComparison {
    def compare(v1: VersionItem, v2: VersionItem): Int = {
      (javaVendor(v1)compare javaVendor(v2)) match {
        case 0 => compareRegexGroups(basicJavaVersion(v1), basicJavaVersion(v2))
        case c => c
      }
    }
  }

  private def javaVendor(item: VersionItem): String = item.vendor.getOrElse("none")

  private def basicJavaVersion(item: VersionItem): String = item.version.split('-').head

  private def quickSort(as: Array[VersionItem]): Array[VersionItem] = {
    Sorting.quickSort(as)
    as
  }

  implicit class OrderedVersionItems(items: Seq[VersionItem]) {

    def ascendingOrder: Seq[VersionItem] = quickSort(items.toArray).toList

    def descendingOrder: Seq[VersionItem] = quickSort(items.toArray).toList.reverse
  }
}
