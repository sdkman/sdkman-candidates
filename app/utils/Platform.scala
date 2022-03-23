package utils

case class Platform(distribution: String, name: String)

object Platform {

  def apply(platformId: String): Platform = platformId.toLowerCase match {
    case "linuxx64"       => LinuxX64
    case "linuxx32"       => LinuxX32
    case "linuxarm64"     => LinuxARM64
    case "linuxarm32sf"   => LinuxARM32SF
    case "linuxarm32hf"   => LinuxARM32HF
    case "darwinx64"      => MacX64
    case "darwinarm64"    => MacARM64
    case "linux"          => LinuxX64
    case "linux64"        => LinuxX64
    case "linux32"        => LinuxX32
    case "darwin"         => MacX64
    case "freebsd"        => FreeBSD
    case "sunos"          => SunOS
    case CygwinPattern(c) => Windows64
    case _                => Exotic
  }

  private val CygwinPattern = "(cygwin|mingw64|msys).*".r

  val LinuxX32     = Platform("LINUX_32", "Linux 32bit")
  val LinuxX64     = Platform("LINUX_64", "Linux 64bit")
  val LinuxARM32SF = Platform("LINUX_ARM32SF", "Linux ARM 32bit Soft Float")
  val LinuxARM32HF = Platform("LINUX_ARM32HF", "Linux ARM 32bit Hard Float")
  val LinuxARM64   = Platform("LINUX_ARM64", "Linux ARM 64bit")
  val MacX64       = Platform("MAC_OSX", "macOS 64bit")
  val MacARM64     = Platform("MAC_ARM64", "macOS ARM 64bit")
  val Windows64    = Platform("WINDOWS_64", "Cygwin")
  val FreeBSD      = Platform("FREE_BSD", "FreeBSD")
  val SunOS        = Platform("SUN_OS", "Solaris")

  //fallback to UNIVERSAL for exotic (unsupported) platforms
  //allows exotic platforms to install only UNIVERSAL candidates
  val Exotic = Platform("UNIVERSAL", "Universal")
}
