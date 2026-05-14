# Spec — State API integration: contract corrections + default-version lookup

> Describes **what** must be true about the Candidates Service ⇄ State API contract on the `state_api_integration` branch.
> Implementation strategy ("how") is left to the planning phase.

## Goal

Two things must hold on this branch:

1. Every Candidates Service interaction with the State API uses the **correct contract** as published in <https://state.sdkman.io/swagger/documentation.yaml>. Three of those interactions are currently wrong.
2. The Candidates Service `GET /default/:candidate` endpoint resolves the default version by **tag lookup against the State API**, not by reading from MongoDB.

The State API is consumed as-is. No State API endpoints or schemas change.

## State API contract (the source of truth)

All facts below are verified against the live production swagger.

### Endpoints consumed by the Candidates Service

| Method & path | Path params | Query params | Success body |
|---|---|---|---|
| `GET /versions/{candidate}` | `candidate` | `platform?`, `distribution?`, `visible?` | `Version[]` |
| `GET /versions/{candidate}/{version}` | `candidate`, `version` | `platform?`, `distribution?` | `Version` (200) or empty (404) |
| `GET /versions/{candidate}/tags/{tag}` | `candidate`, `tag` | `platform?` (defaults to `UNIVERSAL`), `distribution?` (defaults to `NA`) | `Version` (200) or empty (404) |

The query parameter for vendor is named **`distribution`** on every endpoint. There is no `vendor` query parameter.

### `Version` response JSON

The production `Version` object has these fields:

- `candidate: string`
- `version: string`
- `platform: string` — drawn from the enum `LINUX_X32 | LINUX_X64 | LINUX_ARM32HF | LINUX_ARM32SF | LINUX_ARM64 | MAC_X64 | MAC_ARM64 | WINDOWS_X64 | UNIVERSAL`
- `url: string`
- `visible: boolean`
- **`distribution: string`** — drawn from an enum of JDK distributions (`TEMURIN`, `CORRETTO`, `ZULU`, …). The vendor concept is named `distribution` in the response.
- `md5sum: string`, `sha256sum: string`, `sha512sum: string`
- `tags: string[]`

The Candidates Service does not need to consume every field; unknown fields can be ignored. But the **wire field for vendor is `distribution`**, and `platform` values come back in SCREAMING_SNAKE_CASE.

## Decisions (fixed for this change — do not deliberate)

- **Default tag is `"lts"` for every candidate.** When `GET /default/:candidate` is called, the State API call uses `tag = "lts"`, regardless of which candidate. Per-candidate tag configuration is a later concern.
- **The Candidates Service keeps `vendor` as its internal/public-facing term.** Public route params, configuration, and internal Scala identifiers continue to say `vendor`. The string `"distribution"` appears only at the State API wire boundary — both on outgoing query parameters *and* on incoming response JSON. Translation is the client's responsibility.
- **`GET /default/:candidate` keeps its existing public contract.** It takes only the `candidate` path parameter; no `platform` or `vendor` is added. The State API call therefore omits both query parameters and relies on the State API's defaults.
- **Behaviour on missing default.** `404` from the State API tag lookup translates to `400 Bad Request` with an empty body, matching the legacy Mongo behaviour when `Candidate.default` is unset.

## Contract changes required

Each item below describes the **correct shape** of an interaction. The current branch state is described only enough to make the gap visible. None of this prescribes which files to modify or how to structure the code.

### A. Listing visible versions for a candidate + platform

- **Currently issues:** a request whose path includes both `candidate` and `platform` as path segments. This does not match any production State API route and effectively returns 404 (or routes incorrectly into the single-version endpoint).
- **Must issue:** a request against `GET /versions/{candidate}` with `platform` carried as a **query parameter**.

### B. Fetching a single version with an optional vendor filter

- **Currently issues:** `GET /versions/{candidate}/{version}` with the vendor filter carried as a query parameter named `vendor`.
- **Must issue:** the same path, but the vendor filter must be carried as a query parameter named **`distribution`**. The Scala caller continues to receive a `vendor: Option[String]` argument; only the wire name changes.

### C. Resolving the default version by tag

- **Currently exists:** no interaction. `GET /default/:candidate` reads `Candidate.default` from MongoDB.
- **Must issue:** when the public route `GET /default/:candidate` is invoked, the Candidates Service must call `GET /versions/{candidate}/tags/lts` against the State API with no `platform` or `distribution` query parameters. On `200`, the response's `version` field is returned to the caller with HTTP `200 OK`. On `404` (or any non-2xx), the caller receives `400 Bad Request` with an empty body.
- **Side effect:** the public-facing default-version path no longer depends on MongoDB. Other controllers' Mongo dependencies are out of scope.

### D. Response parsing for `Version`

- **Currently parses:** the JSON field `vendor` into the Scala model's `vendor: Option[String]`. This works against the WireMock stubs (which produce `vendor`) but will fail against the real State API (which produces `distribution`).
- **Must parse:** the JSON field `distribution` into the Scala model's `vendor` field. Other unknown fields (`md5sum`, `sha256sum`, `sha512sum`, `tags`) are not required and may be ignored.

### E. WireMock stubs

- **Currently model:** the stale wire contract — path-style `/versions/{candidate}/{platform}`, query parameter `vendor`, and JSON output keyed `vendor`.
- **Must model:** the production contract — `/versions/{candidate}` with `platform` as a query parameter; `distribution` as the vendor-filter query parameter; `Version` JSON keyed `distribution`. The tag endpoint `/versions/{candidate}/tags/{tag}` must also be stubbable with optional `platform` and `distribution` query parameters and configurable 200/404 outcomes.

## Acceptance criteria

The change is complete when **all** of these hold:

- `./sbt test` passes — every existing test plus new coverage for the default-version-by-tag interaction (both the success case and the missing-tag case).
- `./sbt scalafmtCheck Test/scalafmtCheck` passes.
- `./sbt compile` produces no new warnings.
- A Candidates Service test that issues `GET /default/<candidate>` against a WireMock stub which **returns 200 for `GET /versions/<candidate>/tags/lts`** receives `200 OK` from the controller, with the version string in the body.
- A Candidates Service test that issues `GET /default/<candidate>` against a WireMock stub which **returns 404 for `GET /versions/<candidate>/tags/lts`** receives `400 Bad Request` from the controller, with an empty body.
- The `GET /default/<candidate>` controller path has no remaining dependency on MongoDB. (Other controllers' Mongo dependencies remain — they are out of scope.)

## Out of scope (do not touch)

- **The State API.** No code, endpoints, or schemas in `../../do/sdkman-state` change.
- **Other controllers' MongoDB dependencies.** `VersionsController`, `VersionsListController`, `JavaListController`, `CandidatesController`, `CandidatesListController`, `ValidationController` continue to read `Candidate` metadata from MongoDB. Migrating those is a separate, later change.
- **Per-candidate default-tag configuration.** The hard-coded `"lts"` decision stands.
- **Vendor Release / dual-write / Foojay DISCO.** Steps 3 and 4 of the wider migration.
- **The 1500 ms request timeout** currently configured on State API calls. Carry it forward unchanged.
- **The public-facing route shape** of any Candidates Service endpoint. CLI compatibility is preserved.

## Watch-outs

- The `platform` query parameter on State API requests accepts free-form strings, but `platform` **in response bodies** is an enum (`LINUX_X64`, `MAC_ARM64`, `UNIVERSAL`, …). If response objects flow into Candidates Service code that compares platform values, the existing internal representation may differ from the API's wire form.
- `tags` is now a real field on `Version` responses. The Candidates Service does not need to surface it but should not break if it appears.

## References

- High-level gap analysis (control-plane meta-repo): `../../docs/specs/02-candidates-state-api-integration.md`
- End-game plan: `../../docs/specs/end-game.md`
- Diagram: `../../docs/architecture/end-game.svg`
- Live State API swagger: <https://state.sdkman.io/swagger/documentation.yaml>
