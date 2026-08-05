package rendering

import domain.Version

trait VersionItemListBuilder {

  val MinCountThreshold: Int

  def available(v: Seq[Version]): Seq[String] = v.map(_.version)

  def local(installed: Option[String]): Seq[String] = installed.toList.flatMap(_.split(","))

  def pad(items: Seq[VersionItem], upperBound: Int): Seq[Option[VersionItem]] =
    items.map(Some(_)).padTo(Math.max(MinCountThreshold, upperBound), None)

  def items(
      available: Seq[String],
      installed: Seq[String],
      current: Option[String],
      vendor: Option[String] = None
  ): Seq[VersionItem] = {
    // Play folds a query-string `+` to a space on bind (spec 06 §3), so an
    // installed/current identifier carrying build metadata (e.g. `21.0.1+12-open`)
    // reaches the controller as `21.0.1 12-open` and would never match the
    // `+`-bearing `available` values read from the State API body — leaving a
    // freshly installed DISCO java rendered as a phantom `local`-only row with no
    // `*` marker and no `>` default arrow. A version identifier never legitimately
    // contains a space, so remap space→`+` on the received values before comparison.
    // `available` already carries the literal `+` and must stay untouched.
    val normalisedInstalled = installed.map(_.replace(' ', '+'))
    val normalisedCurrent   = current.map(_.replace(' ', '+'))
    val combined            = (available ++ normalisedInstalled).toSet
    combined
      .map(v =>
        VersionItem(
          v,
          installed = normalisedInstalled.contains(v) && available.contains(v),
          local = normalisedInstalled.contains(v) && !available.contains(v),
          current = normalisedCurrent.contains(v),
          vendor = vendor
        )
      )
      .toList
  }
}
