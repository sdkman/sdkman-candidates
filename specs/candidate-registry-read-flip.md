# Spec — Candidate metadata read flip onto the State API

This spec describes the contract that must hold between the Candidates Service and the SDKMAN State API once candidate *metadata* moves off MongoDB.

It defines **what** must be true. Implementation strategy is the planning phase's concern and is not prescribed here.

## Purpose

[`state-api-default-version.md`](state-api-default-version.md) moved version data onto the State API and explicitly left the MongoDB `candidates` collection in place: *"Migrating the `candidates` collection is a separate, later piece of work."* This is that work.

Every read of the `candidates` collection moves to a new State API resource, `GET /candidates`. Five of this service's public endpoints depend on candidate metadata today, and all five change source. Two fields disappear on the way across, and the second of them takes a piece of this service's logic with it.

**The MongoDB connection stays.** `HealthController` reads the `application` collection for `GET /alive`, and the `application` collection is not in scope. `ApplicationRepository`, `MongoConn`, and the Mongo configuration all remain. Only `CandidatesRepository` and its callers go.

The State API is consumed as-is; the new resource is specified in [`../../../do/sdkman-state/specs/candidate-registry.md`](../../../do/sdkman-state/specs/candidate-registry.md) and ships before this change.

## State API surface

One new endpoint is consumed, alongside the three already in use.

| Path | Parameters | Success | Notes |
|---|---|---|---|
| `/candidates` | none | `200` → `Candidate[]` | public, unauthenticated, ascending by `candidate` |

### `Candidate` response JSON

| Field | Type | Notes |
|---|---|---|
| `candidate` | string | the identifier |
| `name` | string | display name |
| `description` | string | free text, may contain non-ASCII |
| `website_url` | string | always `https` |
| `default` | string | **optional.** The `lts`-tagged version, already resolved to a platform by the State API. Absent when unresolved, and **always absent for `java`** |

There is no `distribution` field and no stored `default` field. The Mongo collection had both; neither survives.

## What is lost, and what replaces it

| Mongo field | Replacement |
|---|---|
| `candidates.default` | the derived `default` on `GET /candidates`, for every candidate except `java` |
| `candidates.distribution` (`UNIVERSAL` / `PLATFORM_SPECIFIC`) | **nothing in this service.** The State API resolves the platform when deriving `default`; this service no longer chooses one and no longer probes |

The second row is the larger change. Today `DefaultController` classifies a candidate, picks a platform, and retries at the other one on a miss ([`state-api-hardening.md`](state-api-hardening.md) §2). All of that is **deleted**. The `UNIVERSAL`-then-`LINUX_X64` preference still exists, but it lives once, in the State API's query, and this service never expresses it. `java` is the sole exception and keeps its own lookup.

## Required behaviour

### Sourcing the candidate set

The Candidates Service holds the full candidate set in memory and refreshes it from `GET /candidates` on a TTL. It never issues a per-request lookup for a single candidate: the registry is order-of-100 rows and is fetched whole.

- The set is warmed on startup. A **failed** warm must not prevent the service from starting.
- While the cached set is fresh, requests are served from it with no State API call.
- When the cached set is stale, it is refreshed. The stale copy is served while that refresh is in flight; a request never blocks on a refresh.
- When there is **no** cached set at all, a request **waits** for the initial fetch and is served if it succeeds. It fails only if that fetch fails. A restart against a healthy State API must not produce errors.
- When a refresh fails, the previous set is retained and continues to be served. A failed refresh is logged and retried on the next opportunity.
- A `200` carrying an **empty** array is treated as contract drift, not as a valid empty registry: the previous set is retained and the condition is logged. The live registry is never legitimately empty, and an empty candidate set is more damaging than a stale one. Note that the State API legitimately returns `[]` between its own two releases, before the backfill runs — if this service is ever deployed ahead of that backfill it will refuse to serve, which is intended but easy to misdiagnose.
- A `200` whose body does not parse as `Candidate[]` is contract drift and is handled the same way.

**Staleness is the sum of two caches, and that is intended.** Everything the State API serves is static in nature, and every call to it should be cached. `play.ws.cache.enabled=true` (`conf/application.conf`) with `ehcache` on the classpath means Play WS already honours the `Cache-Control: max-age` the State API sets on its read routes. The in-process set sits on top of that. Effective staleness for candidate metadata is therefore **this service's TTL plus the State API's `max-age`**, not the TTL alone, and the in-process set earns its keep only past the point where the HTTP cache has expired — which is exactly where the never-empty and serve-stale guarantees matter. Any TTL chosen here must be read as an addition to the State API's, not as the total.

**When no set can be obtained.** Every endpoint below that needs candidate metadata responds `503 Service Unavailable` **with an empty body**.

The empty body is required, not stylistic, and the reason is broader than it first appears. Three separate consumers write the response of `/candidates/all` into `$SDKMAN_DIR/var/candidates`:

| Writer | Guard |
|---|---|
| `cli/sdkman-cli/src/main/bash/sdkman-update.sh:28` | skips on an empty or HTML-looking body |
| `sdkman-hooks/app/views/install_stable.scala.txt:64-65` | **none** — `curl -s`, then write |
| `sdkman-hooks/app/views/selfupdate_stable.scala.txt:61-63` | **none** — `curl -s`, then write |

An empty body protects only the first. A `curl get.sdkman.io | bash` during an outage still writes a zero-byte cache, after which `sdkman-cache.sh` reports `WARNING: Cache is corrupt. SDKMAN cannot be used until updated.` for every command except `update`, and the native binaries panic rather than warn. An empty body is still strictly the least-bad choice — any *non*-empty body would be written by all three and corrupt the cache with its own contents — but it does not make a fresh install during an outage safe, and this spec does not claim it does. Hardening those two bootstrap scripts belongs with `sdkman-hooks`, in phase 3.

Note also that `sdk update` against an empty `503` is completely silent: `sdkman-update.sh:28` has no `else` branch, so the user sees no output and no error. That is accepted.

This posture is deliberately **not** the one `StateApiImpl` applies to version listings, which degrade a failure to an empty list. An empty candidate set makes `sdk install` reject every candidate and `sdk list` render nothing.

### `GET /candidates/all`

Returns the comma-joined candidate identifiers from the cached set, in the order received. The service does not re-sort.

The sort authority moves from MongoDB (`sort(ascending("candidate"))`) to a Postgres `ORDER BY`, so ordering is now subject to the database's collation. Every live identifier matches `^[a-z][a-z0-9]*$`, for which the two agree, but `features/candidates.feature` and `features/candidate_list.feature` pin exact orderings and are the regression net for this.

### `GET /candidates/list`

Renders the plain-text candidate listing from the cached set, in the order received.

Each section header shows the candidate's display name and its default version. The default is taken from the `default` field of the cached record, and renders as `Coming soon!` when that field is absent — the existing fallback, unchanged.

`java` is the exception. Its record never carries a `default`, so the service resolves it with a tag lookup: `GET /versions/java/tags/lts` with `platform=LINUX_X64` and the Temurin distribution, rendered as the public **identifier** (`25.0.4-tem`), never the bare version. That result is cached alongside the candidate set and refreshed with it, so rendering the listing does not issue a State API call per request. A failed or absent java lookup renders `Coming soon!` like any other candidate, and never fails the whole listing.

The `test` candidate is no longer filtered out by name. It is not registered in the State API, so it does not arrive.

### `GET /default/:candidate`

The public contract is unchanged: `200` with the candidate's **identifier**, or `400` with an empty body. For every candidate except `java` the identifier and the stored version are the same string; for `java` they are not, and conflating them caused two production rollbacks ([`../../../docs/glossary.md`](../../../docs/glossary.md), *identifier / version*).

Resolution is:

- The candidate must exist in the cached set. An unknown candidate is `400`.
- If the candidate is `java`: the Temurin tag lookup described above, rendered as an identifier.
- Otherwise: the `default` field of the cached record. Absent means `400`.

**This service issues no tag lookup for a non-java candidate and chooses no platform.** The platform preference lives in the State API's derived-`default` query. There is no fallback, no retry, and no classification.

A State API transport failure, timeout, or unparseable body during the java lookup yields `400` with an empty body, the same as a miss. `state-api-hardening.md` §1 deliberately left this path un-recovered for version *listings*; for a default resolution the CLI cannot use a `500`, and a miss and a failure are indistinguishable to it.

### `GET /candidates/:candidate/:platformId/versions/list`

Unchanged except for its source of candidate metadata. The candidate must exist in the cached set, and its display name is used as the listing title. An unknown candidate is `404`, as today.

### `GET /candidates/java/:platformId/versions/list`

Unchanged except that the footer's default version can no longer come from a stored `default`. It uses the same cached java tag result described under `GET /candidates/list`. When that is absent, the existing hardcoded `17.0.0-tem` fallback applies, still truncated to the existing footer width.

That the listing header falls back to `Coming soon!` while this footer falls back to `17.0.0-tem` is deliberate: each preserves the behaviour its own view has today.

### `GET /alive` and `GET /ping`

Unchanged. `/alive` continues to read the `application` collection from MongoDB.

Two consequences to hold onto rather than fix here:

- `/alive` now reports on a dependency this service barely uses while ignoring the one it depends on. An instance with no candidate set, serving `503` on everything, still reports `OK` and stays in rotation. That is the accepted cost of leaving `application` to phase 3.
- **`/ping` must not join the `503` set.** It backs the CLI's own availability probe: `sdkman-availability.sh:28` curls `${SDKMAN_CANDIDATES_API}/healthcheck`, which the live front maps onto this endpoint, on every shell startup and every `sdk` command. An empty or failing response there sets `SDKMAN_AVAILABLE=false` and prints the full `INTERNET NOT REACHABLE!` banner in every shell.

## Decisions

These design calls are locked. The planning phase does not revisit them.

- **The candidate set is fetched whole and cached, never per-candidate.** The State API offers no single-candidate route and none is needed.
- **Every State API read is cached, at both layers, by design.** The data is static in nature. The stated TTL is additive to the State API's `max-age`, not a total.
- **Stale is better than empty.** A failed refresh serves the last good set. Only a first fetch that fails causes a request to fail.
- **A cold start blocks rather than failing.** A restart against a healthy State API must not produce a burst of errors, which matters because this service deploys by manual ansible.
- **A `503` from these endpoints carries an empty body.** See above; this is a CLI-safety requirement, and it is a mitigation rather than a cure.
- **`GET /default/:candidate` is served from the cached derived value.** An earlier draft kept it as a live lookup on freshness grounds; that rationale does not survive the fact that the tag lookup is itself HTTP-cached for the State API's `max-age`. Reading the cached record instead removes a mechanism rather than adding one.
- **The platform preference is not implemented in this service.** `UNIVERSAL` then `LINUX_X64` lives once, in the State API. This service must not reconstruct it, and must not rebuild the `UNIVERSAL` / `PLATFORM_SPECIFIC` classification from version data or a hardcoded list.
- **The retry-the-other-platform behaviour from [`state-api-hardening.md`](state-api-hardening.md) §2 is deleted, not repurposed.** Note that its outcome can differ from the new rule: for a candidate carrying `lts` at both platforms, today's `LINUX_X64`-first order for a `PLATFORM_SPECIFIC`-labelled candidate resolves differently from `UNIVERSAL`-first. No live candidate is in that state, but the change is not behaviour-preserving in general. See [`../../../docs/decisions/0007-platform-classification-deleted.md`](../../../docs/decisions/0007-platform-classification-deleted.md).
- **`java` keeps its distribution-qualified default and its special case.** The rule that java's default is Temurin at `LINUX_X64` lives in this service, not in the State API.
- **This service owns its candidate model.** `io.sdkman.repos.Candidate` comes from the published `sdkman-mongodb-persistence` artefact and carries a non-optional `distribution: String` and an `Option[String] default`, neither of which exists any more. A service-owned type replaces it, following the precedent of `domain.Version` and `domain.Platform`. `CandidateListSection` and its spec are constructed from that type, not from the library's.
- **The `vendor` vocabulary is unaffected.** [`vendor-distribution-translation.md`](vendor-distribution-translation.md) continues to govern the wire boundary; the new resource carries no vendor concept.
- **MongoDB is not removed from this service.** Only `CandidatesRepository` goes. `ApplicationRepository` and the Mongo wiring stay until the `application` collection moves.
- **The cache TTL is configuration, not a constant**, and defaults to five minutes. The request timeout for `GET /candidates` matches the existing State API calls.

## Consumer-visible changes

Three candidates disappear from `/candidates/all` and `/candidates/list`: `cuba`, `ktx` and `test`. `cuba` and `ktx` were retired during phase 1 by removing them from the State API's allow-list; `test` is a fixture. See [`../../../docs/specs/candidates-end-game.md`](../../../docs/specs/candidates-end-game.md) §6.

This is not cosmetic for anyone who has one installed. Both CLIs gate on the cached candidate list: `sdkman-init.sh:116-123` only exports `<NAME>_HOME` and extends `PATH` for listed candidates, `sdkman-main.sh:129` rejects unlisted qualifiers, and `cli/sdkman-cli-native/src/lib.rs:68-75` exits with "not a valid candidate" for `home`, `default`, `uninstall` and `current`. After their next `sdk update`, an affected user loses the binary from `PATH` and cannot run `sdk uninstall` on it. This is accepted; it is recorded here so it is not rediscovered as a bug.

## Acceptance

The change is complete when **all** of the following hold:

- `sbt test` passes.
- `sbt scalafmtCheck Test/scalafmtCheck` passes.
- `sbt compile` produces no new warnings.
- No controller reads the MongoDB `candidates` collection; `CandidatesRepository` and the `CandidatesRepo` mixin are gone from this service.
- No type from `io.sdkman.repos` representing a candidate appears in the rendering layer or its tests.
- `ApplicationRepository` remains and `GET /alive` still reads MongoDB.
- `GET /candidates/all` returns the identifiers from the cached State API set, comma-joined, in the order received.
- `GET /candidates/list` renders every candidate in the order received, with no name-based filtering of `test`.
- A candidate whose State API record has no `default` renders `Coming soon!` in the listing header.
- The listing header for `java` shows the Temurin `lts` **identifier**, and renders `Coming soon!` if that lookup fails.
- The `sdk list java` footer shows the same java value, falls back to `17.0.0-tem` when it is absent, and remains truncated to the existing footer width.
- Rendering either listing issues no State API call while the cache is fresh.
- `GET /default/<non-java candidate>` is answered from the cached record and issues **no** tag lookup.
- `GET /default/<non-java candidate>` whose record has no `default` returns `400` with an empty body.
- `GET /default/java` issues one tag lookup, at `platform=LINUX_X64` with the Temurin distribution, and returns the suffixed identifier.
- `GET /default/<unknown candidate>` returns `400` with an empty body and issues no tag lookup.
- A transport failure, timeout, or unparseable body on the java lookup returns `400`, not `500`.
- `GET /candidates/<unknown candidate>/<platform>/versions/list` returns `404`; a known candidate's display name is used as the listing title.
- A State API failure with a warm cache serves the previous candidate set on every metadata-backed endpoint.
- A State API `200` with an empty array, or an unparseable body, retains the previous set and is logged.
- A cold start against a healthy State API serves successfully; the first request waits for the initial fetch rather than failing.
- A cold start whose initial fetch fails returns `503` with an **empty** body on every metadata-backed endpoint.
- A failed warm at startup does not prevent the service from starting.
- `GET /ping` answers successfully regardless of State API or cache state.
- The refresh interval is configurable and defaults to five minutes; the `GET /candidates` request timeout matches the existing State API calls.
- No code path reconstructs a `UNIVERSAL` / `PLATFORM_SPECIFIC` classification, and no code path chooses a platform for a non-java default.
- The service starts and serves successfully against a State API whose candidate records carry no `default` field at all.
- `test/support/StateApiStubs.scala` gains a `/candidates` stub, and `test/support/Mongo.scala`'s `insertCandidate` / `insertCandidates` are no longer used by any step or spec.
- `test/rendering/PlainTextRenderingSpec.scala` constructs its fixture from the service-owned candidate type, not `io.sdkman.repos.Candidate`.
- The Cucumber features that pin candidate listings and defaults (`candidates.feature`, `candidate_list.feature`, `default.feature`, `java_version_list_footer.feature`) pass against State API stubs rather than Mongo fixtures.

## Test fixtures

Candidate metadata moves out of Mongo, so the fixture layer moves with it. This is the largest mechanical part of the change and is in scope.

| Today | After |
|---|---|
| `test/support/Mongo.scala` — `insertCandidate`, `insertCandidates`, `candidatesCollection` | unused by candidate-facing steps; Mongo support stays only for `application` |
| `test/support/StateApiStubs.scala` — stubs for `/versions/*` only | gains a `/candidates` stub returning a `Candidate[]` body, including the `default` field and its absence |
| `test/rendering/PlainTextRenderingSpec.scala` — builds `io.sdkman.repos.Candidate` | builds the service-owned type |
| Cucumber steps seeding candidates into Mongo | seed the State API stub instead |

The features that pin rendered output (`candidate_list.feature`, `candidates.feature`, `default.feature`, `java_version_list_footer.feature`) are the regression net for the flip and should keep their existing expectations wherever behaviour is unchanged.

## Out of scope

- The State API itself: the `candidates` table, routes and migrations are [`../../../do/sdkman-state/specs/candidate-registry.md`](../../../do/sdkman-state/specs/candidate-registry.md).
- The MongoDB `application` collection, `GET /alive`, and removal of the Mongo driver. Phase 3.
- Hardening the unconditional candidate-cache writes in the `sdkman-hooks` install and self-update scripts. Phase 3, with the rest of that repo.
- Any change to either CLI, including the candidate-validity gates that strand `cuba` and `ktx`.
- Backfilling the candidate data into Postgres. That is `candidates-migration/`, in the workspace root.
- Public route shapes. Every path, status code and response body keeps its existing contract, apart from the new `503` condition and the three candidates no longer present.
- Per-candidate default-tag configuration. The literal `"lts"` remains correct.
- Vendor Release, dual-write, Foojay DISCO.
- Any change to version listing, single-version lookup, or `/validate`.

## References

- State API side of this change: [`../../../do/sdkman-state/specs/candidate-registry.md`](../../../do/sdkman-state/specs/candidate-registry.md)
- Phase 2 scope and measurements: [`../../../docs/specs/candidates-end-game.md`](../../../docs/specs/candidates-end-game.md)
- Why the classification is deleted rather than renamed: [`../../../docs/decisions/0007-platform-classification-deleted.md`](../../../docs/decisions/0007-platform-classification-deleted.md)
- Why the registry is a foreign key: [`../../../docs/decisions/0006-candidate-foreign-key.md`](../../../docs/decisions/0006-candidate-foreign-key.md)
- Preceding version read flip: [`state-api-default-version.md`](state-api-default-version.md). Note two stale claims in it: the tag route's `platform` is **required**, not defaulted to `UNIVERSAL`, and the `NA` distribution sentinel was removed by State API migration `V16`.
- The platform fallback this deletes: [`state-api-hardening.md`](state-api-hardening.md) §2
- Vendor ⇄ distribution vocabulary: [`vendor-distribution-translation.md`](vendor-distribution-translation.md)
- Terminology, and the identifier/version distinction: [`../../../docs/glossary.md`](../../../docs/glossary.md)
