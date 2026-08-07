# State API integration hardening

Three defects found reviewing [PR #71](https://github.com/sdkman/sdkman-candidates/pull/71#pullrequestreview-4878315974), to fix before this fronts the live CLI. Amends [`state-api-default-version.md`](./state-api-default-version.md).

## 1. A State API blip returns `500` to the CLI

The version-listing read (`GET /versions/{candidate}`, behind `sdk list`) parses the body regardless of status, so any non-2xx, timeout, or transport failure becomes an unrecovered `500`. Reachable today: `freebsd`/`sunos` map to platforms the State API rejects with `400`, so `/candidates/:c/freebsd/versions/all` returns `500` now.

A failed listing read degrades to an empty list, logged with candidate, platform, and cause. A `200` that doesn't parse as `Version[]` still fails — that is contract drift, not "no versions". Single-version and tag reads already handle this; listing is the gap.

## 2. `/default` returns `400` when `lts` isn't at the labelled platform

`GET /default/:candidate` chooses the tag-lookup platform from `candidates.distribution` (`UNIVERSAL` → `platform=UNIVERSAL`, else `LINUX_X64`), and the State API filters platform exactly. The label is unreliable: 14 live candidates (`scala`, `jmc`, `micronaut`, …) are labelled `UNIVERSAL` but host `lts` only at `LINUX_X64`, so they resolve `400` where the old read returned a version.

On a miss, the lookup falls back to the other platform:

| Candidate | Preferred | Fallback |
|---|---|---|
| `java` | `LINUX_X64` (`distribution=TEMURIN`) | none |
| labelled `UNIVERSAL` | `UNIVERSAL` | `LINUX_X64` |
| anything else | `LINUX_X64` | `UNIVERSAL` |

The preferred result wins when both hit, so correctly-labelled candidates are unchanged — the fallback only turns `400` into `200`. `java` is excluded (its `lts` needs `distribution=TEMURIN` and exists at neither platform without it). Unknown candidate → `400`, no State API call.

## 3. `/validate` rejects real dashed versions

`GET /validate/:candidate/:versionVendor/:platform` splits the identifier on the *first* `-` and treats the tail as a vendor. That holds only for java; elsewhere the dash is part of the version. Real versions that exist but validate `invalid`, blocking install: `groovy 6.0.0-alpha-1`, `sbt 2.0.0-RC13`, `kotlin 1.0.5-2`.

The vendor is the suffix after the *last* `-`, meaningful only for java. A dashed non-java version resolves as itself.

## Out of scope

- The State API — no endpoint or schema changes.
- Correcting `candidates.distribution` labels in Mongo — the #2 fallback is the durable fix.
- `/versions/all` returning `200 ""` for unknown candidates and lacking a global re-sort.
- A contract test pinning the client's reliance on the State API `visible=true` default.

## Done when

- `./sbt test` and `./sbt scalafmtCheck Test/scalafmtCheck` pass.
- A `500`/`503`/`400`/timeout listing read yields an empty list; `/candidates/:c/freebsd/versions/all` returns `2xx`; a malformed `200` body still fails.
- A `UNIVERSAL`-labelled candidate with `lts` only at `LINUX_X64` resolves `200`; both-platform resolves to preferred; `/default/java` issues one lookup with `distribution=TEMURIN`.
- `/validate/groovy/6.0.0-alpha-1/<platform>` resolves `valid`.
