package utils

import org.scalatest.{Matchers, WordSpec}

class PlatformSpec extends WordSpec with Matchers {
  "platform" should {
    "return some platform identifier when presented with a valid normalised uname" in {
      Platform("darwin") shouldBe Some(Platform.MacOSX)
      Platform("linux") shouldBe Some(Platform.Linux)

      Platform("cygwin_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("mingw64_nt-6.1") shouldBe Some(Platform.Windows64)
      Platform("msys_nt-10.0") shouldBe Some(Platform.Windows64)

      Platform("freebsd") shouldBe Some(Platform.FreeBSD)
      Platform("sunos") shouldBe Some(Platform.SunOS)
    }

    "return none when presented with an unknown uname" in {
      Platform("commodore64") shouldBe None
    }
  }
}
