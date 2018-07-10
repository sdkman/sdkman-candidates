package rendering

trait VersionRendering {

  import cats.Show
  import cats.syntax.show._

  val VersionLength = 15

  val SegmentLength = 20

  val BlankSymbol = " "

  val CurrentSymbol = ">"

  val InstalledSymbol = "*"

  val LocalSymbol = "+"

  implicit val versionItemShow = Show.show[VersionItem] { v =>

    val current = if (v.current) CurrentSymbol else BlankSymbol

    val installed = if (v.installed) InstalledSymbol else if(v.local) LocalSymbol else BlankSymbol

    s" $current $installed ${v.version.take(VersionLength).padTo(VersionLength, " ").mkString}"
  }

  implicit val rowShow = Show.show[VersionRow] { row =>

    def padSegment(str: String = ""): String = str.padTo(SegmentLength, " ").mkString

    def expand(ov: Option[VersionItem]): String = ov.map(_.show).getOrElse(padSegment())

    Seq(row.col1, row.col2, row.col3, row.col4).map(expand).mkString
  }
}

case class VersionItem(version: String, current: Boolean = false, installed: Boolean = false, local: Boolean = false)

case class VersionRow(col1: Option[VersionItem], col2: Option[VersionItem], col3: Option[VersionItem], col4: Option[VersionItem])