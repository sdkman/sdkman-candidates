package utils

case class Platform(identifier: String, name: String)

object Platform {

  def apply(uname: String): Option[Platform] = uname.toLowerCase match {
    case "linux" => Some(Linux64)
    case "linux64" => Some(Linux64)
    case "linux32" => Some(Linux32)
    case "linuxarm64" => Some(LinuxARM64)
    case "linuxarm32" => Some(LinuxARM32)
    case "darwin" => Some(MacOSX)
    case "freebsd" => Some(FreeBSD)
    case "sunos" => Some(SunOS)
    case CygwinPattern(c) => Some(Windows64)
    case _ => None
  }

  private val CygwinPattern = "(cygwin|mingw64|msys).*".r

  val Linux32 = Platform("LINUX_32", "Linux 32bit")
  val Linux64 = Platform("LINUX_64", "Linux 64bit")
  val LinuxARM32 = Platform("LINUX_ARM32", "Linux ARM 32bit")
  val LinuxARM64 = Platform("LINUX_ARM64", "Linux ARM 64bit")
  val MacOSX = Platform("MAC_OSX", "Mac OSX")
  val Windows64 = Platform("WINDOWS_64", "Cygwin")
  val FreeBSD = Platform("FREE_BSD", "FreeBSD")
  val SunOS = Platform("SUN_OS", "Solaris")
  val Universal = Platform("UNIVERSAL", "Universal")
}