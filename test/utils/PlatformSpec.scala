package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlatformSpec extends AnyWordSpec with Matchers {
  "platform" should {
    "return some identifier when presented with a valid lowercase platform" in {
      Platform("darwinx64") shouldBe Platform.MacX64
      Platform("darwinarm64") shouldBe Platform.MacARM64
      Platform("linuxx32") shouldBe Platform.LinuxX32
      Platform("linuxx64") shouldBe Platform.LinuxX64
      Platform("linuxarm32sf") shouldBe Platform.LinuxARM32SF
      Platform("linuxarm32hf") shouldBe Platform.LinuxARM32HF
      Platform("linuxarm64") shouldBe Platform.LinuxARM64
      Platform("linuxriscv64") shouldBe Platform.LinuxRISCV64

      Platform("windowsx64") shouldBe Platform.Windows64
      Platform("cygwin_nt-6.1") shouldBe Platform.Windows64
      Platform("cygwin_nt-6.1-wow") shouldBe Platform.Windows64
      Platform("cygwin_nt-6.1-wow64") shouldBe Platform.Windows64
      Platform("cygwin_nt-6.3") shouldBe Platform.Windows64
      Platform("cygwin_nt-6.3-wow") shouldBe Platform.Windows64
      Platform("cygwin_nt-10.0") shouldBe Platform.Windows64
      Platform("cygwin_nt-10.0-wow") shouldBe Platform.Windows64
      Platform("darwin") shouldBe Platform.MacX64
      Platform("freebsd") shouldBe Platform.FreeBSD
      Platform("linux") shouldBe Platform.LinuxX64
      Platform("linux32") shouldBe Platform.LinuxX32
      Platform("linux64") shouldBe Platform.LinuxX64
      Platform("mingw32_nt-6.1") shouldBe Platform.Exotic
      Platform("mingw32_nt-6.1-wow") shouldBe Platform.Exotic
      Platform("mingw32_nt-6.2") shouldBe Platform.Exotic
      Platform("mingw64_nt-6.1") shouldBe Platform.Windows64
      Platform("mingw64_nt-6.3") shouldBe Platform.Windows64
      Platform("mingw64_nt-10.0") shouldBe Platform.Windows64
      Platform("msys_nt-6.1") shouldBe Platform.Windows64
      Platform("msys_nt-6.3") shouldBe Platform.Windows64
      Platform("msys_nt-10.0") shouldBe Platform.Windows64
      Platform("sunos") shouldBe Platform.SunOS
    }

    "return none when presented with an unknown uname" in {
      Platform("zxspectrum") shouldBe Platform.Exotic
      Platform("commodore64") shouldBe Platform.Exotic
    }
  }
}
