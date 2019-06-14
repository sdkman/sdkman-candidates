package rendering

import cats.Show.show

trait VersionRendering {

  import cats.syntax.show._

  val VersionLength = 15

  val SegmentLength = 20

  val BlankSymbol = " "

  val CurrentSymbol = ">"

  val InstalledSymbol = "*"

  val LocalSymbol = "+"

  implicit val versionItemShow = show[VersionItem] { v =>

    val current = if (v.current) CurrentSymbol else BlankSymbol

    val installed = if (v.installed) InstalledSymbol else if(v.local) LocalSymbol else BlankSymbol

    s" $current $installed ${v.version.take(VersionLength).padTo(VersionLength, " ").mkString}"
  }

  implicit val rowShow = show[VersionRow] { row =>

    def padSegment(str: String = ""): String = str.padTo(SegmentLength, " ").mkString

    def expand(ov: Option[VersionItem]): String = ov.map(_.show).getOrElse(padSegment())

    Seq(row.col1, row.col2, row.col3, row.col4).map(expand).mkString
  }
}

trait JavaVersionRendering {

  val CurrentSymbol = ">>>"

  val BlankSymbol = "   "

  val StatusLength = 10

  val BasicVersionLength = 13

  val IdentifierLength = 20

  implicit val javaItemShow = show[VersionItem] { vi =>

    def current = if (vi.current) CurrentSymbol else BlankSymbol

    val status = vi match {
      case VersionItem(_, _, true, false, _) => "installed".padTo(StatusLength, ' ')
      case VersionItem(_, _, false, true, _) => "local only".padTo(StatusLength, ' ')
      case _ => "".padTo(StatusLength, ' ')
    }

    val version = vi.version.padTo(IdentifierLength, ' ')

    def basicVersion = vi.version.split('-').head.padTo(BasicVersionLength, ' ')

    def vendor = vi.vendor.getOrElse("NONE").padTo(6, ' ')

    s"| $current | $basicVersion | $vendor | $status | $version"
  }
}

case class VersionItem(version: String, current: Boolean = false, installed: Boolean = false, local: Boolean = false, vendor: Option[String] = None)

case class VersionRow(col1: Option[VersionItem], col2: Option[VersionItem], col3: Option[VersionItem], col4: Option[VersionItem])