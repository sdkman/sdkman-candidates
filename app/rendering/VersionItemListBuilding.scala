package rendering

import io.sdkman.repos.Version

trait VersionItemListBuilding {

  val MaxVersions: Int

  def available(v: Seq[Version]): Seq[String] = v.map(_.version)

  def local(installed: Option[String]): Seq[String] = installed.toList.flatMap(_.split(","))

  def pad(items: Seq[VersionItem]): Seq[Option[VersionItem]] = items.map(Some(_)).padTo(MaxVersions, None)

  def items(available: Seq[String], installed: Seq[String], current: Option[String]): Seq[VersionItem] = {
    val combined = (available ++ installed).toSet
    combined.map(v =>
      VersionItem(v,
        installed = installed.contains(v) && available.contains(v),
        local = installed.contains(v) && !available.contains(v),
        current = current.contains(v))).toList
  }
}
