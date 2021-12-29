package utils

case class Platform(identifier: String, name: String)

object Platform {

  def apply(uname: String): Option[Platform] = uname.toLowerCase match {
    case "linuxx64"       => Some(LinuxX64)
    case "linuxx32"       => Some(LinuxX32)
    case "linuxarm64"     => Some(LinuxARM64)
    case "linuxarm32"     => Some(LinuxARM32)
    case "darwinx64"      => Some(MacX64)
    case "darwinarm64"    => Some(MacARM64)
    case "linux"          => Some(LinuxX64)
    case "linux64"        => Some(LinuxX64)
    case "linux32"        => Some(LinuxX32)
    case "darwin"         => Some(MacX64)
    case "freebsd"        => Some(FreeBSD)
    case "sunos"          => Some(SunOS)
    case CygwinPattern(c) => Some(Windows64)
    case _                => None
  }

  private val CygwinPattern = "(cygwin|mingw64|msys).*".r

  val LinuxX32   = Platform("LINUX_32", "Linux 32bit")
  val LinuxX64   = Platform("LINUX_64", "Linux 64bit")
  val LinuxARM32 = Platform("LINUX_ARM32", "Linux ARM 32bit")
  val LinuxARM64 = Platform("LINUX_ARM64", "Linux ARM 64bit")
  val MacX64     = Platform("MAC_OSX", "macOS 64bit")
  val MacARM64   = Platform("MAC_ARM64", "macOS ARM 64bit")
  val Windows64  = Platform("WINDOWS_64", "Cygwin")
  val FreeBSD    = Platform("FREE_BSD", "FreeBSD")
  val SunOS      = Platform("SUN_OS", "Solaris")
  val Universal  = Platform("UNIVERSAL", "Universal")
}
