# Spec — State API integration hardening (PR #71 pre-merge)

Consolidates the three blocking findings from the [adversarial review of PR #71](https://github.com/sdkman/sdkman-candidates/pull/71#pullrequestreview-4878315974) into one imperative spec. It amends [`state-api-default-version.md`](./state-api-default-version.md), which specifies the happy-path contract and is silent on all three concerns.

This spec defines **what** must be true. The **how** — sequencing, recovery mechanics, config layout — is the planning phase's concern and is not prescribed here. Where this spec and `state-api-default-version.md` disagree, **this spec wins**; every other decision in that spec is unchanged.

The review raised six findings plus nits. The three below are the ones the reviewer flagged for resolution or conscious acceptance **before this fronts the live CLI**. The remaining three (`/validate` vendor split, `/versions/all` existence + sort, a visibility-default contract test) and the nits are deferred — see [Out of scope](#out-of-scope).

---

## 1. Degraded upstream must not become a `5xx`

**Symptom.** The version-**listing** read (`GET /versions/{candidate}?platform=…`, backing `sdk list` — the most-hit path) parses the response body regardless of HTTP status, unlike the single-version and tag-lookup reads which guard on `200`. Any non-2xx (State API deploy, `502`, `503`, timeout, empty body) fails the `Future`; no controller recovers it and no custom `HttpErrorHandler` is installed, so Play returns **`500` to the CLI**. This is a strict availability regression against the co-located MongoDB reads it replaced.

**Reachable today with no outage.** `Platform.scala` maps `freebsd`/`sunos` onto `FREE_BSD`/`SUN_OS`, which are not in the State API's platform enum. The API rejects them with a `400` whose JSON body is valid but is not a `Version` array → `JsError` → `500`. So `GET /candidates/:c/freebsd/versions/all` (and `sunos`) return `500` in production right now. `/validate` sends the same value but is protected by its status guard, so it degrades correctly — confirming the diagnosis.

**Required behaviour.**

- A **listing** read that fails — by **non-2xx response** (any status, any body) *or* by **transport failure** (connection refused, DNS, or exceeding the request timeout, which fails the `Future` before any status exists) — must resolve to an **empty version sequence**. The endpoint then renders its normal empty-list output; each of the three list-backed endpoints returns `2xx`, never `5xx`.
- A `200` whose body **does not validate** as a `Version` array is **not** softened. It stays a failed `Future` → `500`. This is a genuine contract break between two services that must agree; collapsing it to an empty list would be indistinguishable from a legitimately empty candidate (the API returns `200 []`, not `404`, for unknown candidates) and would hide schema drift indefinitely.
- Every softened failure is **logged** at a level that reaches the root logger, identifying candidate, platform, and cause — so a degraded upstream is diagnosable, not silently indistinguishable from an empty candidate.

**Locked decisions.**
- Soft-fail applies to **listing reads only**. Single-version and tag lookups already map non-`200` → absent, and their callers translate that correctly (`invalid` for `/validate`, `400` for `/default`). Softening those would report a real version as missing — worse than today. Unchanged.
- Soft-fail is keyed on **status and transport failure, never on the parse result of a `200`**.
- The `FREE_BSD`/`SUN_OS` values **stay** in `Platform.scala`. Removing them would also stop the `500` but changes how the service answers those platforms — a separate product decision. This spec only requires the rejection degrade rather than crash.

---

## 2. `/default` platform classification must tolerate a mislabelled candidate

**Symptom.** `GET /default/:candidate` picks the tag-lookup `platform` from a binary read of `candidates.distribution`: exactly `"UNIVERSAL"` → `platform=UNIVERSAL`, anything else → `platform=LINUX_X64`. The State API tag lookup filters `platform` **exactly** with no UNIVERSAL fallback. So a candidate labelled `UNIVERSAL` (or `MIXED`/`MULTI_PLATFORM`) whose `lts` artefact is hosted only per-platform resolves `404` → **`400 Bad Request`**, where the superseded platform-agnostic `candidates.default` read returned a version.

**Not hypothetical.** Probing live on 2026-08-07, **14 candidates** are broken today: `gcn`, `grails`, `jextract`, `jikkou`, `jmc`, `kcctl`, `kuml`, `mcs`, `micronaut`, `mvnd`, `neo4jmigrations`, `scala`, `scalacli`, `toolkit`. (`scala` is the clearest: labelled `UNIVERSAL`, `lts` = `3.8.4` hosted at `LINUX_X64` only.) The label is inherited by default in `../../misc/sdkman-db-migrations` and only four candidates override it — **the label is a hint, not a guarantee.** The service already acknowledges this shape: `VersionsController`/`VersionsListController` query **both** `UNIVERSAL` and the request platform and union the results (`features/versions.feature`, *"…registered as UNIVERSAL but hosting platform-specific Versions"*). `/default` is the only version-backed endpoint still trusting the label as the single source of truth.

**Required behaviour.** The default-version lookup considers **two** platforms:

| Candidate | Preferred | Alternate | Distribution |
|---|---|---|---|
| `java` | `LINUX_X64` | **none** | `TEMURIN`, from the `tem` shortcode |
| labelled `UNIVERSAL` | `UNIVERSAL` | `LINUX_X64` | none |
| labelled anything else | `LINUX_X64` | `UNIVERSAL` | none |

- The **preferred** result is used when present; otherwise the **alternate** result; when neither yields a version, `400 Bad Request` with an empty body, exactly as today.
- The preferred platform always wins when both return a version — so the fallback **can never change the answer for a correctly-labelled candidate**, only turn a `400` into a `200`.
- The tag stays the literal `"lts"` on both lookups; the vendor/distribution treatment is identical on both — the fallback varies **only** the platform.

**Locked decisions.**
- The `distribution` label **stays the primary signal** (it picks the preferred platform). This adds tolerance; it does not remove the classification.
- The fallback set is exactly **`{UNIVERSAL, LINUX_X64}`** — no other platform. `/default` takes no platform input.
- **`java` is excluded** from the fallback: its `lts` resolves only with `distribution=TEMURIN`, at neither platform without it, so a fallback would only add a wasted request. Exactly **one** lookup (`platform=LINUX_X64`).
- The `200`/`400` public contract is unchanged — only the set of inputs that produce `200` widens.
- An unknown candidate (absent from MongoDB `candidates`) still yields `400` with **no** State API call.

---

## 3. Version reads must not be served from a stacked in-process cache

**Symptom.** This branch enabled `play.ws.cache.enabled` and added the `ehcache` dependency (commit `6c3fc73`), plus a commented-out WS-cache TRACE logger suggesting the work was left mid-investigation. Play's WS cache is RFC 7234 and caches on explicit freshness — which the live State API sends (`cache-control: max-age=600` on both the `/versions/*` **and** the tag endpoints, verified 2026-08-07). So this is not a no-op: list **and** `/default` responses are cached in-process for ten minutes, on top of the Cloudflare edge cache the API already sits behind. That **stacks a second staleness window** — a newly published version can be invisible to `sdk list` (and reported `invalid` by `/validate`) for up to twenty minutes. The cache is per-JVM, so instances go stale independently (the "it worked for my colleague" failure mode); it is also unsized and has no release-time invalidation hook.

**Required behaviour.** Version data read from the State API must not be served from an in-process HTTP response cache layered on the upstream's edge caching. Freshness of published versions takes precedence over the per-request saving — a vendor publishing a release expects `sdk list`/`sdk install` to see it promptly, and the edge cache already provides that saving.

**Locked decisions.**
- Caching is **removed**, not retained-and-justified — it was introduced exploratorily, is unsized, and duplicates the CDN. Removing it restores pre-branch freshness.
- If in-process caching is reintroduced later it must be a deliberate change that scopes caching to the requests that benefit, sets an explicit cache size, and records the resulting staleness budget in a spec.
- The exploratory artefacts that accompanied the flag (the `ehcache` dependency, the commented-out WS-cache TRACE logger) are removed with it.

---

## Acceptance

The change is complete when **all** hold:

- `./sbt test` and `./sbt scalafmtCheck Test/scalafmtCheck` pass.

*Resilience (#1)*
- A listing response of `500`/`503`/`502` — or a `400` carrying the live `{"error":"Bad Request","message":"Invalid platform …"}` body — yields an empty version sequence, not a failed `Future`.
- A listing request that fails at the transport layer or exceeds the request timeout yields an empty version sequence.
- A listing `200` whose body does not validate as a `Version` array still yields a failed `Future` (deliberately **not** softened).
- `GET /candidates/:c/freebsd/versions/all` and the `sunos` equivalent return `2xx` with an empty list rather than today's `500`; each of the three list-backed endpoints returns `2xx` on a failed listing read.
- Every softened failure emits a log entry identifying candidate, platform, and cause.

*Default-version fallback (#2)*
- A candidate labelled `UNIVERSAL` whose `lts` exists only at `LINUX_X64` resolves `200` with that version (the `scala`/`jmc`/`micronaut` shape).
- A candidate labelled non-`UNIVERSAL` whose `lts` exists only at `UNIVERSAL` resolves `200` with that version.
- A candidate whose `lts` exists at **both** platforms resolves to its **preferred** platform's version.
- A candidate with no `lts` at either platform yields `400` with an empty body.
- `GET /default/java` still carries `distribution=TEMURIN` (derived from the `tem` shortcode, not a literal) and issues exactly **one** lookup (`platform=LINUX_X64`).
- An unknown candidate yields `400` and issues no State API request.
- `features/default.feature:35-39` no longer passes coincidentally: it currently stubs only `micronaut`/`UNIVERSAL` as `404` and leans on WireMock's unmatched-request default to `404` the fallback. Retitle it and stub `404` explicitly at **both** platforms so the `400` is asserted deliberately.

*Caching (#3)*
- `play.ws.cache.enabled` is absent from `conf/application.conf`, the `ehcache` dependency is absent from `build.sbt`, and the commented-out `play.api.libs.ws.ahc.cache` logger is absent from `conf/logback.xml`.

---

## Out of scope

**Deferred review findings** (real, but not blocking the live-CLI cutover; track separately):
- **#4 `/validate` vendor parsing** splits on the *first* `-` (`.split("-").lift(1)`) while the rest of the service uses the last-`-` suffix (`endsWith(s"-$ven")`). Agrees for today's single-dash java identifiers; mis-parses any identifier with an internal `-`.
- **#5 `/versions/all`** (a) no longer checks candidate existence (unknown → `200 ""`) while `VersionsListController` returns `404` for the same input — two contracts across siblings; and (b) concatenates `universal ++ platform` without a global re-sort.
- **#6 Visibility-default contract test.** The client sends no `visible` param and relies on the State API's implicit `visible=true` default; the Cucumber stubs pre-filter visibility, so no test pins this dependency (e.g. `withQueryParam("visible", absent())`).
- **Nits:** the three `// TODO: improve error handling` mean one malformed row `500`s a whole list; `ExecutionContext.Implicits.global` in the new client rather than an injected EC; a `withClue` heredoc margin typo in `RestSteps`.

**Confirmed NOT problems** by the reviewer (do not re-investigate): no visibility regression (State API defaults `visible=true`; old repo alias also unioned `UNIVERSAL`); no hidden-version validation regression (single-version `findUnique` has no `visible` predicate); the `+`→space fold correctly does not touch `/validate` (path param, no query-string fold).

**Structurally out of scope:**
- The State API itself — no code in `../../do/sdkman-state`, no endpoint shapes, no schemas, no change to its cache headers. In particular, teaching the tag lookup its own UNIVERSAL fallback is **not** this change.
- Correcting `candidates.distribution` labels in MongoDB — the label is not owned by this service and mislabelling will recur; the fallback is the durable fix. A data-correction pass may happen independently. (Note a fixture contradiction to reconcile alongside: `../../seed/mongo-candidates.js` labels `micronaut` `PLATFORM_SPECIFIC`, while the Cucumber fixtures and authoritative migrations label it `UNIVERSAL`.)
- Candidates with **no** `lts` at either platform (`coursier`, `cuba`, `ksrc`, `ktx`, `test` at time of writing) — `400` under both old and new behaviour; a State API data gap, tracked separately.
- Retries, circuit breaking, or a stale-while-revalidate fallback cache — legitimate follow-ups; this spec requires only that failure not become `5xx`.
- The `1500 ms` request timeout value; MongoDB `candidates` reads and their failure behaviour.

---

## References

- Amended happy-path contract: [`state-api-default-version.md`](./state-api-default-version.md)
- Vendor↔distribution translation: [`vendor-distribution-translation.md`](./vendor-distribution-translation.md)
- Review: <https://github.com/sdkman/sdkman-candidates/pull/71#pullrequestreview-4878315974>
- Client under discussion: `app/clients/StateApiImpl.scala`, `app/clients/RequestBuilder.scala`
- Callers: `app/controllers/DefaultController.scala`, `VersionsController.scala`, `VersionsListController.scala`, `JavaListController.scala`
- Union-of-platforms precedent: `features/versions.feature`
- Caching commit: `6c3fc73` — "Introduce http caching for remote calls"
- Live State API swagger: <https://state.sdkman.io/swagger/documentation.yaml>
