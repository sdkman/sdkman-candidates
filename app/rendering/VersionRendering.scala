package rendering

import io.sdkman.repos.Version

trait VersionRendering {

  import cats.Show
  import cats.syntax.show._

  implicit val versionShow = Show.show[Version] { v =>
    s"${v.version.take(14).padTo(15, " ").mkString}"
  }

  implicit val rowShow = Show.show[VersionRow] { row =>

    val BlankSymbol = " "

    val CurrentSymbol = ">"

    val InstalledSymbol = "*"

    def expand(ov: Option[Version]): String = ov.map(_.show).map(padTo15).getOrElse(padTo15())

    def padTo15(str: String = ""): String = str.padTo(15, " ").mkString

    def currentVersion(maybeVersion: Option[Version]): String = {
      for {
        v <- maybeVersion
        c <- row.ctx.current
        symbol = if (c == v.version) CurrentSymbol else BlankSymbol
      } yield symbol
    }.getOrElse(BlankSymbol)

    def installedVersion(maybeVersion: Option[Version]): String = {
      for {
        v <- maybeVersion
        symbol = if (row.ctx.installed.contains(v.version)) InstalledSymbol else BlankSymbol
      } yield symbol
    }.getOrElse(BlankSymbol)

    def status(ov: Option[Version]): String =
      s" ${currentVersion(ov)} ${installedVersion(ov)} "

    Seq(row.col1, row.col2, row.col3, row.col4).map(ov => status(ov) + expand(ov)).mkString
  }
}

case class VersionContext(current: Option[String], installed: List[String])

case class VersionRow(col1: Option[Version], col2: Option[Version], col3: Option[Version], col4: Option[Version])(implicit val ctx: VersionContext)