# Spec — Vendor ↔ Distribution translation at the State API boundary

This spec describes a contract that must hold between the Candidates Service's internal model and the SDKMAN State API. It defines **what** must be true. Implementation strategy is the planning phase's concern and is not prescribed here.

## Purpose

The Candidates Service uses **vendor shortcodes** (`tem`, `open`, `zulu`, …) as its internal and public-facing vendor identifier — they appear in CLI-facing version identifiers (`21.0.3-tem`), in installed-version suffixes, and in the Java version-list grouping. The State API uses **distribution enum names** (`TEMURIN`, `OPENJDK`, `ZULU`, …) on the wire — on both query parameters and response JSON.

The existing State API client translates only the **field name** (`distribution` ⇄ `vendor`); it does **not** translate the **value**. As a result the two dialects leak across the boundary in both directions:

- **Inbound:** a `Version` response carries `distribution: "TEMURIN"`, which becomes `vendor = "TEMURIN"` internally. The rest of the service expects a shortcode, so the Java version list groups every version under "Unclassified" and installed-version correlation (matching a `-tem` suffix) fails.
- **Outbound:** the validation path sends the shortcode (`open`, split from `8u111-open`) as the `distribution` query parameter. The State API does not recognise `open`; today it silently drops the filter, and — once the State API tightens its query-parameter validation (see [References](#references)) — it will reject the value with `400`.

This spec requires the client boundary to translate the **value** in both directions, so the service speaks shortcodes internally and enum names on the wire. It is the completion of the locked decision in [`state-api-default-version.md`](./state-api-default-version.md) that *"translation is the client's responsibility"* — which today is only half-done (field name, not value).

**Scope is the vendor/distribution value translation only.** It does not change the public route contract, the CLI-facing vendor vocabulary, or the default-version classification rules.

## Vocabularies

Two vocabularies for the same concept:

- **Internal / public-facing (Candidates Service + CLI):** vendor **shortcode**.
- **Wire (State API):** distribution **enum name**.

The canonical mapping between them (the 16 distributions the State API knows):

| Vendor shortcode | State API distribution |
|---|---|
| `tem` | `TEMURIN` |
| `amzn` | `CORRETTO` |
| `zulu` | `ZULU` |
| `librca` | `LIBERICA` |
| `nik` | `LIBERICA_NIK` |
| `oracle` | `ORACLE` |
| `open` | `OPENJDK` |
| `graal` | `GRAALVM` |
| `graalce` | `GRAALCE` |
| `mandrel` | `MANDREL` |
| `ms` | `MICROSOFT` |
| `sapmchn` | `SAP_MACHINE` |
| `sem` | `SEMERU` |
| `kona` | `KONA` |
| `bisheng` | `BISHENG` |
| `jbr` | `JETBRAINS` |

**Orphan shortcodes** that have no State API distribution — `adpt` (AdoptOpenJDK, superseded by Temurin), `albba` (Dragonwell), `gln` (Gluon), `trava`, `zulufx` — are **defunct**: no versions are hosted for them. They have no wire counterpart.

Absence of a vendor (non-Java candidates such as `groovy`, `gradle`) maps to absence of a distribution on the wire, and vice versa.

## Required behaviour

### Outbound translation (requests)

When the State API client sends a `distribution` query parameter, the value it puts on the wire is the **distribution enum name** corresponding to the internal vendor shortcode. A caller that holds the shortcode `open` causes `distribution=OPENJDK` to be sent; `tem` causes `distribution=TEMURIN`.

This applies wherever a vendor is sent today — the single-version lookup (validation) and the tag lookup (default version). Callers continue to express the vendor as a shortcode (or as "no vendor"); they do not construct enum names themselves.

### Inbound translation (responses)

When the State API client parses a `Version` response, the `distribution` enum name is translated to the internal vendor **shortcode** before it populates `vendor`. A response carrying `distribution: "TEMURIN"` surfaces internally as `vendor = "tem"`. A response with no `distribution` surfaces as no vendor.

### Internal invariant

Everywhere except the State API wire boundary, the vendor is a **shortcode** (or absent). No distribution enum name appears in controllers, rendering, ordering, installed-version correlation, or any public response. With this invariant restored, the existing shortcode-keyed logic (the Java version-list vendor grouping, the `-<shortcode>` installed-version matching) works without change.

### Unmappable values

- An internal vendor **shortcode** with no corresponding distribution (an orphan/defunct shortcode) has no wire counterpart: the client treats it as "no distribution". A lookup for such a vendor is best-effort and is expected to yield no match, since no versions are hosted for defunct distributions. This mirrors the State API's stance on defunct platforms.
- An inbound `distribution` enum name is always mappable (every State API distribution has a shortcode), so no orphan case arises on responses.

## Decisions

These design calls are locked. The planning phase does not revisit them.

- **The Candidates Service keeps `vendor` (shortcode) as its internal and public-facing term.** The string `distribution` and the enum-name vocabulary appear **only** at the State API wire boundary. This extends the locked decision in `state-api-default-version.md` from the field name to the field value.
- **Translation lives solely in the State API client boundary** (the request builder, the response parsing) — an anti-corruption layer. Controllers, rendering, and ordering remain shortcode-only.
- **The canonical shortcode ↔ distribution mapping is the table above** (the 16 State API distributions). It is authoritative; orphan shortcodes are defunct and have no wire counterpart.
- **`DefaultController` passes the internal shortcode** (e.g. `tem`) and lets the boundary translate, rather than hardcoding the wire value `TEMURIN`.

## Acceptance

The change is complete when **all** of the following hold:

- `sbt test` passes.
- `sbt scalafmtCheck Test/scalafmtCheck` passes.
- A State API `Version` response carrying `distribution: "TEMURIN"` is parsed so that `vendor` is `tem` (and absent `distribution` yields no vendor).
- The Java version list groups versions under their correct vendor labels (Temurin, Zulu, …) and never under "Unclassified" solely because of an untranslated enum name; installed vendored versions (e.g. `21.0.3-tem`) correlate correctly.
- The State API request for validating a vendored Java version (e.g. `8u111-open`) carries `distribution=OPENJDK` (the enum name), not `open`.
- The State API request issued for `GET /default/java` carries `distribution=TEMURIN`, derived from the `tem` shortcode rather than a hardcoded literal.
- No distribution enum name (`TEMURIN`, `OPENJDK`, …) appears in any Candidates Service public response or in any code path outside the State API client boundary.
- A vendor shortcode with no State API distribution (`adpt`, `albba`, `gln`, `trava`, `zulufx`) is handled without error, sending no distribution on the wire.

## Out of scope

- The State API itself: no code in `../../do/sdkman-state`, no endpoint shapes, no schemas. (The State API's own query-parameter strictness is specified separately — see References.)
- The `platform` query parameter — the Candidates Service already sends canonical platform enum names; that concern is the State API's to tighten.
- The default-version classification rules (platform/distribution selection per candidate) — owned by `state-api-default-version.md`.
- The public route contract and CLI-facing vendor vocabulary — these remain shortcodes and are unchanged.
- Migrating the MongoDB `candidates` collection.

## References

- Default-version migration spec: [`state-api-default-version.md`](./state-api-default-version.md)
- State API query-parameter strictness (the change that makes outbound shortcodes a hard `400`): `../../do/sdkman-state/specs/version-read-query-parameters.md`
- Migration overview: `../../docs/specs/end-game.md`
- Step-2 high-level analysis: `../../docs/specs/02-candidates-state-api-integration.md`
- Live State API swagger: <https://state.sdkman.io/swagger/documentation.yaml>
