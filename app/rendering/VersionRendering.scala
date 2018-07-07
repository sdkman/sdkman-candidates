package rendering

import io.sdkman.repos.Version

trait VersionRendering {

  import cats.Show
  import cats.syntax.show._

  implicit val versionShow = Show.show[Version] { v =>
    s"${v.version.take(14).padTo(15, " ").mkString}"
  }

  implicit val rowShow = Show.show[VersionRow] { row =>

    def expand(ov: Option[Version]): String = ov.map(_.show).map(padTo15).getOrElse(padTo15())

    def padTo15(str: String = ""): String = str.padTo(15, " ").mkString

    def status = "     "

    Seq(row.col1, row.col2, row.col3, row.col4).map(ov => status + expand(ov)).mkString
  }
}

case class VersionRow(col1: Option[Version], col2: Option[Version], col3: Option[Version], col4: Option[Version])
