# Java list view — harden the Unclassified path and the footer width

Adversarial review of PR #99 (<https://github.com/sdkman/sdkman-candidates/pull/99#issuecomment-5375293757>) showed that the guarantees of `specs/java-list-view-layout.md` hold for the table body but not for the footer, and that three mechanisms route locally installed versions *away* from the `Unclassified` group that Business Rule 10 promises as the catch-all. One finding is a regression introduced by the layout PR (the `Unclassified` map-key collision); the rest are pre-existing defects now in scope because the layout spec claims them fixed.

This spec amends `specs/java-list-view-layout.md`. Where the two conflict, this spec wins. Applies to `GET /candidates/java/:platformId/versions/list` only.

## Context

Per <https://sdkman.io/usage/#install-local-versions>, the only constraint on a local install label is uniqueness — no character set, no length limit, no required `<version>-<shortcode>` shape. Non-conforming labels are therefore the norm. Every locally installed version passed via the `installed` query parameter must render exactly once, under some group, with its full label visible in the `Identifier` column. From the CLI, an absent row is indistinguishable from an uninstalled JDK.

## Business Rules

1. **A local install joins a vendor group only when both conditions hold**: its label ends with `-<shortcode>` (hyphen required — a bare `endsWith` match on the shortcode letters alone, e.g. `system` matching `tem`, does not count), **and** that shortcode's vendor has at least one published version on the requested platform. Every other local install renders in the `Unclassified` group with the `+` marker. In particular: labels ending in never-published shortcodes (`adpt`, `albba`, `gln`, `trava`, `zulufx`), labels ending in a shortcode whose vendor is unpublished on this platform (e.g. `17-zulu` where Zulu is absent), bare shortcode labels (`tem`), and hyphenless labels (`system`, `mybuild`) all render under `Unclassified`. No local install is ever dropped.

2. **Groups that share a display label merge into one group.** Published versions with an unmapped shortcode, published versions with no vendor, and local-only installs all carry the `Unclassified` label; they render as a single `Unclassified` group, published rows first, local-only rows after. A group never silently overwrites another group with the same label. The merged `Unclassified` group renders after every vendor group, matching the layout spec's reference rendering (`Zulu` above `Unclassified`); all other groups keep their alphabetical order.

3. **The footer's default-version line never exceeds 80 characters.** The interpolated default identifier is truncated to 23 characters — the width remaining after the fixed prefix ` $ sdk install java                 install the default: ` — using the truncation marker of rule 5. The current longest live identifier is exactly 23 characters, so no live default truncates.

4. **The version-cell fallback drops the trailing hyphen segment only when it has shortcode shape.** When the identifier does not end with `-<the row's own vendor shortcode>`, the segment after the last hyphen is removed only if it consists entirely of lowercase letters `a`–`z` (regex `[a-z]+`). Otherwise the whole identifier is shown in the `Version` column. Thus `11.0.3-local` renders version `11.0.3`, while `21.0.12-crac+1.2`, `21.0.12-fx+1.1` and `UPPER-CASE` render unchanged, keeping Business Rule 4 of the layout spec ("rows that differ at all differ visibly") true for local installs.

5. **A truncated value ends with a visible `>` marker.** When a value exceeds its column width, the rendered cell is the first `width − 1` characters followed by `>`. This applies to the `Version` cell, the `Identifier` cell and the footer default. A truncated identifier is not installable as displayed; the marker makes the elision self-evident instead of silent.

6. **Blank elements in the `installed` parameter are ignored.** Splitting `installed` on commas discards empty and whitespace-only elements before any further processing. A leading, trailing or doubled comma neither renders a blank row nor suppresses the `Unclassified` group.

7. **No `installed` value causes a non-200 response.** An all-hyphen label such as `-` sorts as an empty version string instead of throwing. Degenerate labels may render degenerate rows; they must not produce an HTTP 500.

8. **A label whose `-<shortcode>` suffix matches a published vendor is grouped under that vendor** (e.g. `mine-zulu` under Zulu when Zulu is published), with the suffix stripped from the `Version` cell per rule 4. This is accepted behaviour, not a defect: the user chose the vendor-shaped suffix, and the `Identifier` column stays honest.

## Out of Scope

- Display-width handling for double-width (CJK) or astral characters in labels. `take`/`padTo` count UTF-16 code units; terminal drift for such labels is accepted (review finding 9).
- Any change to grouping or ordering of published versions with mapped, published vendors.
- The non-java list view, the State API contract, and all other endpoints.

## Acceptance Criteria

- [ ] `installed=17-gln,system,17-zulu` with only Temurin published renders all three labels under `Unclassified` with `+` markers.
- [ ] A published version with an unmapped shortcode and a local-only install render together in one `Unclassified` group; neither row is lost.
- [ ] A candidate default longer than 23 characters renders a footer line of exactly 80 characters ending in `>`.
- [ ] `installed=21.0.12-crac+1.2,21.0.12-fx+1.1` renders two visibly distinct `Version` cells under `Unclassified`.
- [ ] A `Version` or `Identifier` value wider than its column renders `width − 1` characters plus `>`.
- [ ] `installed=,a-local,,b-local` renders exactly two `Unclassified` rows and no blank row.
- [ ] `installed=mybuild-,-` returns `200 OK`.
- [ ] The raw-width Cucumber step rejects trailing tabs and carriage returns, and runs in the java list scenarios of `features/version_list_by_platform.feature` and `features/version_list_by_visibility.feature`.
- [ ] `sbt test` and `sbt scalafmtCheck Test/scalafmtCheck` pass.

## References

- Review source: <https://github.com/sdkman/sdkman-candidates/pull/99#issuecomment-5375293757> — findings 1–9; findings 6 and 9 are resolved here as accepted behaviour and out of scope respectively.
- Local install contract: <https://sdkman.io/usage/#install-local-versions>.
- Decision record: rule 1 keys vendor-group membership on *published* vendors rather than the full shortcode map, so a label is either grouped or `Unclassified`, never invisible. Rule 4 chooses shortcode-shape (`[a-z]+`) over an `available`-lookup because it is deterministic per row and needs no extra data flow. Rule 5 chooses `>` over `…` to keep the body ASCII. Rule 2 places the merged `Unclassified` group last rather than in alphabetical position because the layout spec's exemplar and every existing fixture render it after `Zulu`; alphabetical placement would violate layout rule 11 ("group ordering unchanged").
