package utils

import org.scalatest.{Matchers, WordSpec}

class PlatformSpec extends WordSpec with Matchers {
  "platform" should {
    "return some platform identifier when presented with a valid unnormalised uname" in {
      Platform("Linux") shouldBe Some(Platform.Linux)
      Platform("Darwin") shouldBe Some(Platform.MacOSX)
      Platform("CYGWIN_NT-6.1") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-10.0") shouldBe Some(Platform.Windows64)
      Platform("MINGW64_NT-6.1") shouldBe Some(Platform.Windows64)
      Platform("MINGW32_NT-6.1") shouldBe None
      Platform("MINGW64_NT-10.0") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-6.1-WOW") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-6.3") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-10.0-WOW") shouldBe Some(Platform.Windows64)
      Platform("MINGW32_NT-6.2") shouldBe None
      Platform("SunOS") shouldBe Some(Platform.SunOS)
      Platform("MINGW64_NT-6.3") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-6.3-WOW") shouldBe Some(Platform.Windows64)
      Platform("MSYS_NT-6.1") shouldBe Some(Platform.Windows64)
      Platform("MSYS_NT-10.0") shouldBe Some(Platform.Windows64)
      Platform("CYGWIN_NT-6.1-WOW64") shouldBe Some(Platform.Windows64)
      Platform("FreeBSD") shouldBe Some(Platform.FreeBSD)
      Platform("MSYS_NT-6.3") shouldBe Some(Platform.Windows64)
      Platform("MINGW32_NT-6.1-WOW") shouldBe None
    }

    "return some platform identifier when presented with a valid normalised uname" in {
      Platform("linux") shouldBe Some(Platform.Linux)
      Platform("darwin") shouldBe Some(Platform.MacOSX)
      Platform("cygwin_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("mingw64_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("mingw32_nt-6.1") shouldBe None
      Platform("mingw64_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.1-wow") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-10.0-wow") shouldBe Some(Platform.Windows64)
      Platform("mingw32_nt-6.2") shouldBe None
      Platform("sunos") shouldBe Some(Platform.SunOS)
      Platform("mingw64_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.3-wow") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.1-wow64") shouldBe Some(Platform.Windows64)
      Platform("freebsd") shouldBe Some(Platform.FreeBSD)
      Platform("msys_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("mingw32_nt-6.1-wow") shouldBe None
    }

    "return none when presented with an unknown uname" in {
      Platform("zxspectrum") shouldBe None
      Platform("commodore64") shouldBe None
    }
  }
}
