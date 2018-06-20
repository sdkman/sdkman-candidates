package ordering

import io.sdkman.repos.Version

trait VersionOrdering {

  implicit object OrderedVersion extends Ordering[Version] with RegexStringComparison {
    def compare(v1: Version, v2: Version): Int = compareRegexGroups(v1.version, v2.version)
  }
}
