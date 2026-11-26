package domain

// The two CLIs are registered in the State API but never served here: specs/cli-candidates-excluded.md
object CliCandidates {

  private val Identifiers: Set[String] = Set("sdkcli", "sdkrs")

  def contains(candidate: String): Boolean = Identifiers.contains(candidate)
}
