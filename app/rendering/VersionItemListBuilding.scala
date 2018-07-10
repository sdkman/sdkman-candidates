package rendering

trait VersionItemListBuilding {

  def buildItems(available: Seq[String], installed: Seq[String], current: Option[String]): List[VersionItem] = {
    val combined = (available ++ installed).toSet
    combined.map(v =>
      VersionItem(v,
        installed = installed.contains(v) && available.contains(v),
        local = installed.contains(v) && !available.contains(v),
        current = current.contains(v))).toList
  }
}
