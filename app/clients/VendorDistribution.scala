package clients

// Canonical mapping between the Candidates Service's internal `vendor`
// shortcode (used everywhere internally and on public responses) and the
// State API's wire-side `distribution` enum name. This is the single source
// of truth for the translation defined in
// specs/vendor-distribution-translation.md.
//
// Orphan shortcodes (`adpt`, `albba`, `gln`, `trava`, `zulufx`) belong to
// defunct distributions that the State API does not host; they have no wire
// counterpart and `toDistribution` returns `None` for them.
object VendorDistribution {

  private val shortcodeToDistribution: Map[String, String] = Map(
    "tem"     -> "TEMURIN",
    "amzn"    -> "CORRETTO",
    "zulu"    -> "ZULU",
    "librca"  -> "LIBERICA",
    "nik"     -> "LIBERICA_NIK",
    "oracle"  -> "ORACLE",
    "open"    -> "OPENJDK",
    "graal"   -> "GRAALVM",
    "graalce" -> "GRAALCE",
    "mandrel" -> "MANDREL",
    "ms"      -> "MICROSOFT",
    "sapmchn" -> "SAP_MACHINE",
    "sem"     -> "SEMERU",
    "kona"    -> "KONA",
    "bisheng" -> "BISHENG",
    "jbr"     -> "JETBRAINS"
  )

  private val distributionToShortcode: Map[String, String] =
    shortcodeToDistribution.map(_.swap)

  def toDistribution(shortcode: String): Option[String] =
    shortcodeToDistribution.get(shortcode)

  def toVendor(distribution: String): Option[String] =
    distributionToShortcode.get(distribution)
}
