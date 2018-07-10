package rendering

import io.sdkman.repos.Version
import ordering.VersionItemOrdering

trait VersionItemListBuilding {

  self: VersionItemOrdering =>

  val MaxVersions: Int

  val ColumnLength: Int

  def available(v: Seq[Version]): Seq[String] = v.map(_.version)

  def local(installed: Option[String]): Seq[String] = installed.toList.flatMap(_.split(","))

  def pad(items: List[VersionItem]): Seq[Option[VersionItem]] = items.descendingOrder.map(Some(_)).padTo(MaxVersions, None)

  def items(available: Seq[String], installed: Seq[String], current: Option[String]): List[VersionItem] = {
    val combined = (available ++ installed).toSet
    combined.map(v =>
      VersionItem(v,
        installed = installed.contains(v) && available.contains(v),
        local = installed.contains(v) && !available.contains(v),
        current = current.contains(v))).toList
  }
}
