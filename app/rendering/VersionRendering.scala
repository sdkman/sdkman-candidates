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

    val installed = if (v.installed) InstalledSymbol else if (v.local) LocalSymbol else BlankSymbol

    s" $current $installed ${v.version.take(VersionLength).padTo(VersionLength, " ").mkString}"
  }

  implicit val rowShow = show[VersionRow] { row =>
    def padSegment(str: String = ""): String = str.padTo(SegmentLength, " ").mkString

    def expand(ov: Option[VersionItem]): String = ov.map(_.show).getOrElse(padSegment())

    Seq(row.col1, row.col2, row.col3, row.col4).map(expand).mkString
  }
}

trait JavaVersionRendering {

  val VersionLength = 18

  val IdentifierLength = 35

  val BlankSymbol = " "

  val CurrentSymbol = ">"

  val InstalledSymbol = "*"

  val LocalSymbol = "+"

  val TruncationMarker = ">"

  implicit val javaItemShow = show[VersionItem] { vi =>
    val current = if (vi.current) CurrentSymbol else BlankSymbol

    val installed =
      if (vi.installed) InstalledSymbol else if (vi.local) LocalSymbol else BlankSymbol

    val use = s"$current $installed"

    val version = truncate(qualifiedVersion(vi), VersionLength).padTo(VersionLength, ' ')

    val identifier = truncate(vi.version, IdentifierLength)

    s"| $use | $version | $identifier"
  }

  /** The value cut to the column width, with the last character replaced by the truncation marker
    * when anything was cut. A silently clipped identifier reads as an installable one; the marker
    * makes the elision visible.
    */
  def truncate(value: String, width: Int): String =
    if (value.length <= width) value
    else value.take(width - 1) + TruncationMarker

  /** The identifier without its trailing vendor shortcode. Only a suffix matching the item's own
    * vendor is removed, so hyphen-introduced qualifiers such as `-fx+1.1` stay with the version.
    */
  private def qualifiedVersion(vi: VersionItem): String = {
    val identifier = vi.version
    vi.vendor
      .map(vendor => s"-$vendor")
      .filter(identifier.endsWith)
      .map(suffix => identifier.dropRight(suffix.length))
      .getOrElse(dropTrailingSegment(identifier))
  }

  /** The trailing segment is dropped only when it has shortcode shape, so a hyphen-introduced
    * qualifier such as `-crac+1.2` survives on a local install whose vendor never matched. Rows
    * that differ at all must differ visibly, and a qualifier is part of the version.
    */
  private def dropTrailingSegment(identifier: String): String =
    identifier.lastIndexOf('-') match {
      case -1 => identifier
      case index =>
        val segment = identifier.drop(index + 1)
        if (ShortcodeSegment.pattern.matcher(segment).matches) identifier.take(index)
        else identifier
    }

  private val ShortcodeSegment = "[a-z]+".r
}

case class VersionItem(
    version: String,
    current: Boolean = false,
    installed: Boolean = false,
    local: Boolean = false,
    vendor: Option[String] = None
)

case class VersionRow(
    col1: Option[VersionItem],
    col2: Option[VersionItem],
    col3: Option[VersionItem],
    col4: Option[VersionItem]
)
