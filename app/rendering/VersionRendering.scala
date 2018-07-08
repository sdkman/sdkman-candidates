package rendering

trait VersionRendering {

  import cats.Show
  import cats.syntax.show._

  val VersionLength = 15

  implicit val versionItemShow = Show.show[VersionItem] { v =>
    s"${v.version.take(VersionLength).padTo(VersionLength, " ").mkString}"
  }

  implicit val rowShow = Show.show[VersionRow] { row =>

    val BlankSymbol = " "

    val CurrentSymbol = ">"

    val InstalledSymbol = "*"

    def expand(ov: Option[VersionItem]): String = ov.map(_.show).map(padTo15).getOrElse(padTo15())

    def padTo15(str: String = ""): String = str.padTo(15, " ").mkString

    def currentVersion(maybeVersion: Option[VersionItem]): String = {
      for {
        v <- maybeVersion
        c <- row.ctx.current
        symbol = if (c == v.version) CurrentSymbol else BlankSymbol
      } yield symbol
    }.getOrElse(BlankSymbol)

    def installedVersion(maybeVersion: Option[VersionItem]): String = {
      for {
        v <- maybeVersion
        symbol = if (row.ctx.installed.contains(v.version)) InstalledSymbol else BlankSymbol
      } yield symbol
    }.getOrElse(BlankSymbol)

    def status(ov: Option[VersionItem]): String =
      s" ${currentVersion(ov)} ${installedVersion(ov)} "

    Seq(row.col1, row.col2, row.col3, row.col4).map(ov => status(ov) + expand(ov)).mkString
  }
}

case class VersionContext(current: Option[String], installed: List[String])

case class VersionItem(version: String, current: Boolean = false, installed: Boolean = false, local: Boolean = false)

case class VersionRow(col1: Option[VersionItem], col2: Option[VersionItem], col3: Option[VersionItem], col4: Option[VersionItem])(implicit val ctx: VersionContext)