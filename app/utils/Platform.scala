package utils

object Platform {

  type PlatformIdentifier = String

  def apply(uname: String): Option[PlatformIdentifier] = uname match {
    case "linux" => Some(Linux)
    case "darwin" => Some(MacOSX)
    case "freebsd" => Some(FreeBSD)
    case "sunos" => Some(SunOS)
    case CygwinPattern(c) => Some(Windows64)
    case _ => None
  }

  private val CygwinPattern = "(cygwin|mingw|msys).*".r

  val Linux = "LINUX"
  val MacOSX = "MAC_OSX"
  val Windows64 = "CYGWIN"
  val FreeBSD = "FREE_BSD"
  val SunOS = "SUN_OS"
}