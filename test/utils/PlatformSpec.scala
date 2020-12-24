package utils

import org.scalatest.{Matchers, WordSpec}

class PlatformSpec extends WordSpec with Matchers {
  "platform" should {
    "return some identifier when presented with a valid lowercase platform" in {
      Platform("darwinx64") shouldBe Some(Platform.MacX64)
      Platform("darwinarm64") shouldBe Some(Platform.MacARM64)
      Platform("linuxx32") shouldBe Some(Platform.LinuxX32)
      Platform("linuxx64") shouldBe Some(Platform.LinuxX64)
      Platform("linuxarm32") shouldBe Some(Platform.LinuxARM32)
      Platform("linuxarm64") shouldBe Some(Platform.LinuxARM64)

      Platform("cygwin_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.1-wow") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.1-wow64") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-6.3-wow") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("cygwin_nt-10.0-wow") shouldBe Some(Platform.Windows64)
      Platform("darwin") shouldBe Some(Platform.MacX64)
      Platform("freebsd") shouldBe Some(Platform.FreeBSD)
      Platform("linux") shouldBe Some(Platform.LinuxX64)
      Platform("linux32") shouldBe Some(Platform.LinuxX32)
      Platform("linux64") shouldBe Some(Platform.LinuxX64)
      Platform("mingw32_nt-6.1") shouldBe None
      Platform("mingw32_nt-6.1-wow") shouldBe None
      Platform("mingw32_nt-6.2") shouldBe None
      Platform("mingw64_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("mingw64_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("mingw64_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-6.3") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-10.0") shouldBe Some(Platform.Windows64)
      Platform("sunos") shouldBe Some(Platform.SunOS)
    }

    "return none when presented with an unknown uname" in {
      Platform("zxspectrum") shouldBe None
      Platform("commodore64") shouldBe None
    }
  }
}
