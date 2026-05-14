# Spec — State API integration: default version + contract fixes

> **Source of truth** for the work on the `state_api_integration` branch.
> Self-contained — everything a loop needs to perform the implementation is here.

## Goal

Make the Candidates Service consume the SDKMAN State API (`https://state.sdkman.io`) correctly for the version operations on this branch, and switch `DefaultController` off MongoDB onto a tag-based State API lookup.

Two distinct things must land together:

1. **Fix two pre-existing contract bugs** in the State API client (the branch was authored against a stale API shape).
2. **Migrate the default-version path** off Mongo onto `GET /versions/{candidate}/tags/lts`.

The State API itself is **not changed**. All work happens in this repo.

## State API contract (consumed as-is)

The endpoints the client must call. Verified against `https://state.sdkman.io/swagger/documentation.yaml`.

### `GET /versions/{candidate}`

- Path param: `candidate`.
- Query params (all optional): `platform`, `distribution`, `visible`.
- Returns: JSON array of `Version` objects.

### `GET /versions/{candidate}/{version}`

- Path params: `candidate`, `version`.
- Query params (all optional): `platform`, `distribution`.
- Returns: `200` with a single `Version` JSON object, or `404` if not found.

### `GET /versions/{candidate}/tags/{tag}`  ← new for this work

- Path params: `candidate`, `tag`.
- Query params (all optional): `platform` (defaults to `UNIVERSAL`), `distribution` (defaults to `NA` sentinel).
- Returns: `200` with a single `Version` JSON object, or `404` if no version carries that tag in the scope.

**Important:** the State API uses the parameter name **`distribution`**. The Candidates Service uses **`vendor`** in its public-facing routes/configs. These refer to the same concept — the client must translate `vendor` → `distribution` when constructing query strings on the wire. Internal Scala signatures keep saying `vendor`.

## Decisions (do not deliberate, just apply)

- **Default tag** for `DefaultController` is the literal string `"lts"`, hard-coded for every candidate. Do not introduce per-candidate config in this change.
- **Vendor / distribution naming.** Public routes and Scala signatures stay `vendor`. The wire-level query param is `distribution`. Translation happens inside `RequestBuilder`.
- **Behaviour on missing default.** If the State API returns `404` for `/versions/{candidate}/tags/lts`, `DefaultController` returns `400 Bad Request` with an empty body — matching the current behaviour when Mongo's `c.default` is unset.
- **No changes to the public Candidates Service route shape.** `GET /default/:candidate` continues to take only `candidate` (no platform, no vendor); the State API call uses query-param defaults (platform `UNIVERSAL`, distribution `NA`).

## Required changes (in order)

### 1. Fix `RequestBuilder.versionsByCandidatePlatformRequest`

File: `app/clients/RequestBuilder.scala`.

Current (wrong — `/versions/{candidate}/{platform}` does not exist on the State API):

```scala
ws.url(s"$stateApi/versions/$candidate/$platform")
  .addHttpHeaders("Accept" -> "application/json")
  .withRequestTimeout(1500.millis)
```

Change to a query-param call against `GET /versions/{candidate}`:

```scala
ws.url(s"$stateApi/versions/$candidate")
  .withQueryStringParameters("platform" -> platform)
  .addHttpHeaders("Accept" -> "application/json")
  .withRequestTimeout(1500.millis)
```

### 2. Fix `RequestBuilder.versionByCandidatePlatformRequest`

File: `app/clients/RequestBuilder.scala`.

The `vendor` query param must become `distribution` on the wire. Scala parameter stays `vendor: Option[String]`.

Current:

```scala
val queryParams = List(
  Some("platform" -> platform),
  vendor.map("vendor" -> _)
).flatten
```

Change to:

```scala
val queryParams = List(
  Some("platform" -> platform),
  vendor.map("distribution" -> _)
).flatten
```

### 3. Add a tag-based lookup to `RequestBuilder`

File: `app/clients/RequestBuilder.scala`.

Add a new method:

```scala
def versionByTagRequest(
    candidate: String,
    tag: String,
    platform: Option[String],
    vendor: Option[String]
): WSRequest = {
  val queryParams = List(
    platform.map("platform" -> _),
    vendor.map("distribution" -> _)
  ).flatten
  ws.url(s"$stateApi/versions/$candidate/tags/$tag")
    .withQueryStringParameters(queryParams: _*)
    .addHttpHeaders("Accept" -> "application/json")
    .withRequestTimeout(1500.millis)
}
```

Both query params optional: the State API supplies sensible defaults.

### 4. Add a tag-based method to `StateApi` / `StateApiImpl`

File: `app/clients/StateApiImpl.scala`.

Extend the `StateApi` trait:

```scala
def findVersionByTag(
    candidate: String,
    tag: String,
    platform: Option[String],
    vendor: Option[String]
): Future[Option[Version]]
```

Implement in `StateApiImpl` analogously to `findVersionByCandidateAndPlatform`: call `requestBuilder.versionByTagRequest(...)`, treat `200` as `Some(Version)` (parse via existing `JsonConverters`), and `404` (or any non-200) as `None`. Keep the existing `// TODO: improve error handling` comment shape — error handling can be tightened later.

### 5. Rewire `DefaultController`

File: `app/controllers/DefaultController.scala`.

Replace the `CandidatesRepository` injection and Mongo lookup with a `StateApi` injection and a tag-based call:

```scala
class DefaultController @Inject() (stateApi: StateApi, cc: ControllerComponents)
    extends AbstractController(cc) {
  def find(candidate: String): Action[AnyContent] = Action.async(parse.anyContent) { _ =>
    stateApi.findVersionByTag(candidate, tag = "lts", platform = None, vendor = None).map {
      case Some(v) => Ok(v.version)
      case None    => BadRequest("")
    }
  }
}
```

Drop the `repos.CandidatesRepository` import. **This controller no longer touches Mongo.**

### 6. Update WireMock stubs

File: `test/support/StateApiStubs.scala`.

Three changes:

(a) Fix `stubVersionsForCandidateAndPlatform` to match the corrected list URL — query-param style:

```scala
stubFor(
  get(urlPathEqualTo(s"/versions/$candidate"))
    .withQueryParam("platform", equalTo(platform))
    .willReturn(aResponse().withBody(Json.toJson(versions).toString).withStatus(200))
)
```

(b) In `stubVersionForCandidateAndPlatform` and `stubNoVersionForCandidateAndPlatform`, change the `vendor` WireMock query-param matcher key to `distribution`. The function signature stays `vendor: Option[String]` — only the wire name changes.

(c) Add two new stub helpers for the tag endpoint:

```scala
def stubVersionByTag(
    candidate: String,
    tag: String,
    platform: Option[String],
    vendor: Option[String],
    version: Version
): Unit = {
  val queryParams = List(
    platform.map(p => "platform" -> equalTo(p)),
    vendor.map(v => "distribution" -> equalTo(v))
  ).flatten
  stubFor(
    get(urlPathEqualTo(s"/versions/$candidate/tags/$tag"))
      .withQueryParams(queryParams.toMap.asJava)
      .willReturn(aResponse().withBody(Json.toJson(version).toString).withStatus(200))
  )
}

def stubNoVersionByTag(
    candidate: String,
    tag: String,
    platform: Option[String],
    vendor: Option[String]
): Unit = {
  val queryParams = List(
    platform.map(p => "platform" -> equalTo(p)),
    vendor.map(v => "distribution" -> equalTo(v))
  ).flatten
  stubFor(
    get(urlPathEqualTo(s"/versions/$candidate/tags/$tag"))
      .withQueryParams(queryParams.toMap.asJava)
      .willReturn(aResponse().withStatus(404))
  )
}
```

### 7. Update / add tests

- **`DefaultController` test(s)**: switch from a `CandidatesRepository` mock to `StateApi` (or to the WireMock stub if the existing test uses a real Play test server). Cover two cases:
  - lts present: stub `GET /versions/<candidate>/tags/lts` → `200`, expect `200 OK` with the version string body.
  - lts absent: stub `GET /versions/<candidate>/tags/lts` → `404`, expect `400 Bad Request` with empty body.
- **Existing controller tests** (`VersionsController`, `VersionsListController`, `JavaListController`, `ValidationController`): the stub URLs/params changed in step 6, so re-run and adjust any test fixtures that referenced the old shapes.
- **Cucumber features**: search `features/` for any step definitions that pre-populate Mongo with `c.default` for the `/default/:candidate` flow and replace them with State API stubbing.

## Acceptance criteria

The loop is done when **all** of the following hold:

1. `./sbt test` exits 0 (all ScalaTest + Cucumber tests pass).
2. `./sbt scalafmtCheck Test/scalafmtCheck` exits 0.
3. `./sbt compile` exits 0 with no warnings introduced by the new code.
4. `git grep -nE '"vendor"' app/clients/ test/support/'` returns no matches (the literal `"vendor"` query-param key must not appear anywhere in client/stub wire code; the Scala-side `vendor: Option[String]` parameter names are fine and expected).
5. `git grep -n '/versions/.*/\$platform' app/clients/` returns no matches (the wrong path-style URL is gone).
6. `DefaultController` does not import `repos.CandidatesRepository` or anything from `io.sdkman.repos`.

## Out of scope (do **not** touch in this change)

- **Any code in `../../do/sdkman-state` or the State API contract.** Endpoint shapes, schemas, OpenAPI — all stay.
- **Other controllers' Mongo dependency.** `VersionsController`, `VersionsListController`, `JavaListController`, `CandidatesController`, `CandidatesListController`, `ValidationController` still need `Candidate` metadata from Mongo via `CandidatesRepository`. Moving them off Mongo is a later, separate piece of work.
- **Per-candidate default-tag configuration.** Hard-coded `"lts"` is correct for this change.
- **Vendor Release / dual-write / Java-via-Foojay-DISCO.** Those are steps 3 and 4 of the wider migration.
- **Error-handling cleanup** in the existing `// TODO: improve error handling` blocks. Mirror the existing pattern in the new method; do not rewrite.

## Watch-outs

- `RequestBuilder` has a hard-coded **1500 ms** timeout. Keep it — don't tune in this change.
- WireMock's `urlPathEqualTo` matches the path only; query params are matched separately. If a test fails with an unmatched stub, double-check both halves.
- The `Version` JSON shape in the test stubs already carries `vendor: Option[String]` in the Scala model. The State API's JSON also keys this as `vendor` on the response (the `distribution` wire name only appears in **request** query params for these endpoints). Don't rename the JSON field on the response side.
- Cucumber test server runs on a different port to `./sbt run` — features rely on that. If you touch test wiring, leave the port handling alone.

## References

- High-level gap analysis (control-plane meta-repo): `../../docs/specs/02-candidates-state-api-integration.md`
- End-game plan: `../../docs/specs/end-game.md`
- Diagram: `../../docs/architecture/end-game.svg`
- Live State API swagger: <https://state.sdkman.io/swagger/documentation.yaml>
