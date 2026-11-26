package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CliCandidatesSpec extends AnyWordSpec with Matchers {

  "CliCandidates.contains" should {

    "hold for the bash and native CLI candidates" in {
      CliCandidates.contains("sdkcli") shouldBe true
      CliCandidates.contains("sdkrs") shouldBe true
    }

    "not hold for any other name, matching case-sensitively and exactly" in {
      CliCandidates.contains("SDKCLI") shouldBe false
      CliCandidates.contains("Sdkrs") shouldBe false
      CliCandidates.contains("sdkclix") shouldBe false
      CliCandidates.contains("sdk") shouldBe false
      CliCandidates.contains("java") shouldBe false
    }
  }
}
