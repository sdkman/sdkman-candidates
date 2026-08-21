# Spec — Candidates Service ⇄ State API integration

This spec describes the contract that must hold between the Candidates Service and the SDKMAN State API on the `state_api_integration` branch.

It defines **what** must be true. Implementation strategy is the planning phase's concern and is not prescribed here.

## Purpose

The Candidates Service consumes the SDKMAN State API (`https://state.sdkman.io`) for all version data. Three of its public endpoints rely on the State API; one of them — `GET /default/:candidate` — must be migrated off the MongoDB `versions` data (including the legacy denormalised `candidates.default` field) onto a tag-based State API lookup.

**Scope is the `versions` collection only.** Reads of the MongoDB `candidates` collection — which holds candidate metadata such as name, description, and platform classification — stay in place. Migrating the `candidates` collection is a separate, later piece of work.

The State API is consumed as-is. No State API endpoints or schemas change. All work happens in the Candidates Service.

## State API surface

The contract below is the source of truth, verified against <https://state.sdkman.io/swagger/documentation.yaml>.

### Endpoints

The Candidates Service consumes three GET endpoints:

| Path | Path parameters | Query parameters (all optional) | Success | Not found |
|---|---|---|---|---|
| `/versions/{candidate}` | `candidate` | `platform`, `distribution`, `visible` | `200` → `Version[]` | n/a |
| `/versions/{candidate}/{version}` | `candidate`, `version` | `platform`, `distribution` | `200` → `Version` | `404` |
| `/versions/{candidate}/tags/{tag}` | `candidate`, `tag` | `platform` (defaults to `UNIVERSAL`), `distribution` (defaults to the `NA` sentinel) | `200` → `Version` | `404` |

The query parameter that filters by vendor is named **`distribution`** on every endpoint. The State API has no `vendor` query parameter.

### `Version` response JSON

The fields the Candidates Service may receive on a `Version` object:

| Field | Type | Notes |
|---|---|---|
| `candidate` | string | |
| `version` | string | |
| `platform` | string | Drawn from the enum `LINUX_X32 \| LINUX_X64 \| LINUX_ARM32HF \| LINUX_ARM32SF \| LINUX_ARM64 \| MAC_X64 \| MAC_ARM64 \| WINDOWS_X64 \| UNIVERSAL`. |
| `url` | string | |
| `visible` | boolean | |
| `distribution` | string | The vendor concept. Drawn from a JDK-distribution enum (`TEMURIN`, `CORRETTO`, `ZULU`, …). |
| `md5sum`, `sha256sum`, `sha512sum` | string | |
| `tags` | string array | |

The vendor concept is named **`distribution`** in the response. The Candidates Service's internal model uses `vendor` (see *Decisions* below); the wire-level name on responses is `distribution`. Unknown fields may be ignored.

## Required behaviour

### Listing visible versions for a candidate scoped to a platform

The Candidates Service issues `GET /versions/{candidate}` to the State API with the platform supplied as a query parameter named `platform`. The response is a JSON array of `Version` objects.

### Fetching a single version, optionally filtered by vendor

The Candidates Service issues `GET /versions/{candidate}/{version}` to the State API with the platform supplied as a query parameter named `platform`. When a vendor filter is supplied internally, it is sent as a query parameter named `distribution`. The response is a single `Version` on `200`, or absent on `404`.

### Resolving the default version

When the Candidates Service receives `GET /default/:candidate`, it issues `GET /versions/{candidate}/tags/lts` to the State API. The query parameters are determined by the candidate's classification:

- If the candidate is `java`: `platform=LINUX_X64` and `distribution=TEMURIN`.
- If the candidate is otherwise platform-specific: `platform=LINUX_X64` only.
- If the candidate is universal: `platform=UNIVERSAL` only.

The State API's response is mapped to the public response as follows:

- `200` → `200 OK` with the response's `version` field as the body.
- Any non-`2xx` (including `404`) → `400 Bad Request` with an empty body.

Classification is sourced from the candidate's metadata (today: `candidates.distribution` in MongoDB, with values `PLATFORM_SPECIFIC` or `UNIVERSAL`). The Candidates Service must look this up per request — the set of platform-specific candidates grows over time.

For reference, the current set of `PLATFORM_SPECIFIC` candidates is: `java`, `cuba`, `connor`, `pierrot`, `tornadovm`, `ksrc`.

## Decisions

These design calls are locked. The planning phase does not revisit them.

- **The default tag is `"lts"`** for every candidate. Per-candidate tag configuration is out of scope.
- **The Candidates Service keeps `vendor` as its internal and public-facing term.** Routes, configuration, and Scala identifiers continue to say `vendor`. The string `"distribution"` appears only at the State API wire boundary — both on outgoing query parameters and on incoming response JSON. Translation is the client's responsibility.
- **`GET /default/:candidate` keeps its existing public contract.** The path parameter is the only input from the caller; the Candidates Service supplies the State API's `platform` and `distribution` parameters according to the classification rule above.
- **The platform default for platform-specific candidates is `LINUX_X64`.** The distribution default, used only for `java`, is `TEMURIN`.
- **Only the `versions` collection is in scope.** This change moves version-related reads (the existing list/single-version State API calls and the new default-version lookup) onto the State API. The MongoDB `candidates` collection stays in place — every controller that reads candidate metadata continues to do so from MongoDB. Migrating the `candidates` collection is a separate, later piece of work.

## Acceptance

The change is complete when **all** of the following hold:

- `sbt test` passes.
- `sbt scalafmtCheck Test/scalafmtCheck` passes.
- `sbt compile` produces no new warnings.
- The Candidates Service serves `GET /default/<candidate>` by issuing a tag lookup against the State API and not by reading from MongoDB.
- The State API request issued for `GET /default/java` carries `platform=LINUX_X64` and `distribution=TEMURIN` as query parameters.
- The State API request issued for `GET /default/<another platform-specific candidate>` (e.g. `cuba`) carries `platform=LINUX_X64` and no `distribution`.
- The State API request issued for `GET /default/<universal candidate>` carries `platform=UNIVERSAL` and no `distribution`.
- A `200` from the State API tag lookup yields `200 OK` from the Candidates Service with the version string as the response body.
- A `404` from the State API tag lookup yields `400 Bad Request` from the Candidates Service with an empty response body.
- The State API request for listing versions of a candidate uses the query-parameter form (the `platform` is sent as a query parameter, not as a path segment).
- The State API request for fetching a single version with a vendor filter uses the query-parameter name `distribution` (not `vendor`).
- The Candidates Service parses real State API `Version` responses, including those that carry the `distribution` JSON field.

## Out of scope

- The State API itself: no code in `../../do/sdkman-state`, no endpoint shapes, no schemas.
- The MongoDB `candidates` collection. Candidate metadata (name, description, platform classification) continues to be read from MongoDB by every controller. Migrating the `candidates` collection is out of scope.
- Per-candidate default-tag configuration. The literal `"lts"` is correct for this change.
- Vendor Release, dual-write, Foojay DISCO. Those are wider-migration steps 3 and 4.
- Public route shapes of the Candidates Service. CLI compatibility is preserved.

## References

- Migration overview: `../../docs/specs/end-game.md`
- Step-2 high-level analysis: `../../docs/specs/02-candidates-state-api-integration.md`
- Architecture diagram: `../../docs/architecture/end-game.svg`
- Live State API swagger: <https://state.sdkman.io/swagger/documentation.yaml>
