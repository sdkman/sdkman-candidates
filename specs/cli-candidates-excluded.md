# Spec — The CLI candidates do not exist here

This spec describes what the Candidates Service must do once the two SDKMAN CLIs are registered as State API candidates. It defines **what** must be true. Implementation strategy is the planning phase's concern and is not prescribed here.

## Purpose

Phase 3 models the two CLIs as ordinary State API candidates: `sdkcli` (the bash CLI, one `UNIVERSAL` row per release) and `sdkrs` (the native CLI, one row per release per native platform). The State API has no notion of a hidden candidate, so once they are registered `GET /candidates` lists them like any other ([`../../../docs/specs/application-end-game.md`](../../../docs/specs/application-end-game.md) §4.1).

They are not installable SDKs. The legacy CLI must never offer them: `sdk list` must not show them, and `sdk install sdkcli` must fail exactly as it does for any candidate that does not exist. Keeping them out is this service's job, not the State API's (end-game §7).

This must be live before the two are registered (phase 3 execution plan, steps 4 and 5), and must stay live while they are registered. No Candidates image without the rule may serve once State lists them, so rolling this change back means unregistering the two first. Until registration the rule has no visible effect on Live, and the deployed image tag is the proof that it is live.

## Required behaviour

### The rule

`sdkcli` and `sdkrs` are excluded by this service. The two names are a fixed rule in code, not configuration: there is no setting that adds to, removes from, or disables the list. Matching is on the exact, case-sensitive candidate name.

### Registry-backed routes

The cached candidate set never contains the two. They are dropped from the State API's answer when it is taken into the cache, so every route that reads the set sees them as unregistered:

| Route | For `sdkcli` or `sdkrs` |
|---|---|
| `GET /candidates/all` | absent from the list |
| `GET /candidates/list` | no section |
| `GET /default/:candidate` | `400`, empty body |
| `GET /candidates/:candidate/:platformId/versions/list` | `404` |

`GET /candidates/:candidate/:platformId/versions/list` issues no State API request for either candidate.

Every other candidate is served exactly as today, in the order received.

The existing guards against contract drift ([`candidate-registry-read-flip.md`](candidate-registry-read-flip.md), *Sourcing the candidate set*) judge the State API's answer as received, not the set after the two are dropped. A `200` with an empty array is still drift; a `200` listing the two among others is a normal answer.

### Routes that do not read the registry

Two routes read version rows straight from the State API and never consult the candidate set. They apply the same rule by name, and issue **no** State API call for either candidate:

| Route | For `sdkcli` or `sdkrs` |
|---|---|
| `GET /validate/:candidate/:version/:platformId` | `200` `invalid` |
| `GET /candidates/:candidate/:platformId/versions/all` | `200`, empty body |

Both answers are what each route gives an unknown candidate today. Neither route gains a dependency on the candidate set: an install must not start failing because the registry cannot be read.

### What the legacy CLI sees

Because `/candidates/all` never lists them, the CLI's cached candidate list never holds them, and its own gate (`cli/sdkman-cli/src/main/bash/sdkman-main.sh:143`) rejects them for `sdk install` and `sdk list` before any call reaches this service: `Stop! sdkcli is not a valid candidate.` The gate skips `sdk env`, so `sdk env install` with `sdkcli=5.24.0` in `.sdkmanrc` calls `/validate/sdkcli/5.24.0/<platform>`. The by-name `invalid` is what stops it, as for any unknown candidate.

## Decisions

These design calls are locked. The planning phase does not revisit them.

- **The State API stays unaware.** It gains no hidden flag and no filter. Each client keeps the CLI candidates out of its own views; the native CLI does the same when its list views are built.
- **Fixed in code.** The two identifiers are not configuration. A new non-installable candidate is a code change and a spec, not a setting.
- **The two non-registry routes check by name, not against the registry.** Gating them on the set would make installs depend on it, `503` when no set is held, and delay every newly registered candidate by the registry's staleness. Retired candidates such as `infrastructor` keep validating by exact version identifier, as today.

## Done when

- `sbt test` and `sbt scalafmtCheck Test/scalafmtCheck` pass.
- With a State API registry that lists `sdkcli` and `sdkrs` among other candidates:
  - `GET /candidates/all` and `GET /candidates/list` omit both and serve every other candidate unchanged, in the order received.
  - `GET /default/sdkcli` and `GET /default/sdkrs` return `400` with an empty body.
  - `GET /candidates/sdkcli/linuxx64/versions/list` and the same for `sdkrs` return `404` and issue no State API request.
- With the State API holding version rows for both, `GET /validate/sdkcli/5.24.0/linuxx64` and `GET /validate/sdkrs/0.8.0/linuxx64` return `200` `invalid`, `GET /candidates/sdkcli/linuxx64/versions/all` and `GET /candidates/sdkrs/linuxx64/versions/all` return `200` with an empty body, and none issues a State API request.
- With `infrastructor` absent from the registry and holding a `0.3.1` `UNIVERSAL` row, `GET /validate/infrastructor/0.3.1/linuxx64` answers `valid` and `GET /candidates/infrastructor/linuxx64/versions/all` lists `0.3.1`.
- A registry answer that lists only other candidates is served unchanged.
- A `200` with an empty array is still treated as drift and retains the previous set.
- No configuration key names either candidate.

## Out of scope

- The State API: registering the two, their rows and tags, and the vendor login that writes them. Phase 3 step 5, in the workspace root. The same change makes Insomnia folder `11 candidate registry` (R3, R4) compare against State's registry minus `sdkcli` and `sdkrs`, and assert that neither appears in `/candidates/all` or `/candidates/list`.
- The native CLI's own list views.
- Broker 2 download and version routes for the two, which serve them by design.
- `sdkman-hooks`.
- `/alive` against the State API and Mongo removal from this service: a separate spec.

## References

- End-game: [`../../../docs/specs/application-end-game.md`](../../../docs/specs/application-end-game.md) §4.1, §7
- Execution plan: [`../../../docs/specs/application-execution-plan.md`](../../../docs/specs/application-execution-plan.md) steps 4 and 5
- The candidate set and its caching: [`candidate-registry-read-flip.md`](candidate-registry-read-flip.md)
- Why a failed version listing answers empty: [`state-api-hardening.md`](state-api-hardening.md) §1
