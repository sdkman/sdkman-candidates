# Java list view — reclaim column space and restore version qualifiers

`sdk list java` renders a table whose Version column silently drops version qualifiers, so Liberica shows three rows all reading `21.0.12`, and whose rows overflow the 80-character rule whenever an identifier exceeds 20 characters. This spec redefines the table's column layout: the `Dist` column is dropped (it is a 1:1 restatement of `Vendor`), the `Status` column is replaced by single-character markers, and the space reclaimed is given to `Version` and `Identifier` so that both fit whole.

Applies to `GET /candidates/java/:platformId/versions/list` only.

## Behaviour

A CLI user running `sdk list java` sees one row per installable java version, grouped under a vendor heading. Today two of the six columns earn no space:

- **`Dist`** repeats the vendor as a shortcode. It is derivable from `Vendor` on every row, and on rows whose vendor is unrecognised it is actively wrong — it prints `none` while the identifier reads `-xyz`. The shortcode also appears as the suffix of every identifier, so removing the column loses nothing.
- **`Status`** spends ten characters on a three-valued field (`installed`, `local only`, blank).

After this change the table has four columns — `Vendor`, `Use`, `Version`, `Identifier`. Installation state moves into the `Use` column as the `>` / `*` / `+` markers already used by the non-java list view, explained by a one-line legend in the footer.

The footer is rewritten at the same time. Today it spends nine lines alternating a line of prose with the command it describes, repeating `sdk install java` three times as though the three uses were unrelated, and closes by asserting that `Q` exits the list — something the service cannot know, since the CLI pages through `$PAGER`, or `less`, or not at all. It becomes seven lines: the marker legend, then the three `sdk install java` forms in an aligned column with their descriptions alongside.

The user-visible wins: a version qualifier such as `-fx+1.1` or `-crac+1.2` now appears in the `Version` column, so rows that differ are no longer displayed as identical; no row spills past the 80-character rule, so the table's right edge stays straight; and the footer no longer occupies more vertical space than a short listing.

## API Contract

The route, its parameters, its status codes and its content type are unchanged. Only the bytes of the `200` body change.

### Request

```
GET /candidates/java/{platformId}/versions/list?current={current}&installed={installed}
```

| Parameter | In | Required | Description |
|---|---|---|---|
| `platformId` | path | yes | CLI platform token, e.g. `linuxx64` |
| `current` | query | no | Identifier of the version currently in use |
| `installed` | query | yes | Comma-separated identifiers installed locally |

### Response

| Status | Body | When |
|---|---|---|
| `200 OK` | Rendered plain-text table (below) | Always, including when no versions match |

### Response Body

Fixed-width plain text, 80 columns. Column geometry, with the `|` separators at columns **17**, **23** and **44**:

| Column | Columns | Width | Content |
|---|---|---|---|
| Vendor | 2–16 | 15 | Vendor display label, on the group's first row only; blank on subsequent rows |
| Use | 19–21 | 3 | Two markers separated by a space (see *Business Rules*) |
| Version | 25–42 | 18 | Version including any qualifier |
| Identifier | 46–80 | 35 | Installable identifier; last column, not padded |

Reference rendering:

```
================================================================================
Available Java Versions for Linux 64bit
================================================================================
 Vendor         | Use | Version            | Identifier
--------------------------------------------------------------------------------
 Liberica       |     | 21.0.12+1.1        | 21.0.12+1.1-librca
                |     | 21.0.12-fx+1.1     | 21.0.12-fx+1.1-librca
                |   * | 21.0.12-crac+1.2   | 21.0.12-crac+1.2-librca
 Temurin        | > * | 25.0.4             | 25.0.4-tem
                |     | 21.0.12+1.1        | 21.0.12+1.1-tem
 Zulu           |     | 21.0.11.crac       | 21.0.11.crac-zulu
 Unclassified   |   + | 11.0.3             | 11.0.3-local
================================================================================
 > in use   * installed   + local only
--------------------------------------------------------------------------------
 $ sdk install java <Identifier>    install a specific version
 $ sdk install java                 install the default: 25.0.4-tem
 $ sdk install java [TAB]           complete an available identifier
================================================================================
```

The footer's command column occupies columns 2–35 and the description column begins at column 37.

## Business Rules

1. **The `Dist` column is removed.** No column restates the vendor shortcode. It remains visible as the identifier's trailing segment.

2. **The `Status` column is removed; its meaning moves into `Use`.** The `Use` cell is exactly three characters: an in-use marker, a space, then an installation marker.
   - In-use marker: `>` when the row's identifier equals the `current` query parameter, otherwise a space.
   - Installation marker: `*` when the version is installed, `+` when it is installed locally but not published, otherwise a space.
   - Both markers may show together (`> *`) for the version that is both installed and in use.

3. **The in-use marker is a single `>`, not `>>>`.** The three-character cell is shared with the installation marker.

4. **The `Version` column shows the version with its qualifier intact.** The value is the identifier with only the trailing `-<vendor shortcode>` removed. Qualifiers that are themselves hyphen-introduced — `-fx+1.1`, `-crac+1.2` — belong to the version and are retained. Two rows of the same vendor that differ at all differ visibly in this column.

5. **No rendered line exceeds 80 characters.** This holds for the header, the separator rules, every data row, the legend and the footer.

6. **A value wider than its column is truncated to the column width.** Truncation must not push the row past 80 characters. With current data no value truncates — `Version` needs at most 16 of its 18 columns and `Identifier` at most 23 of its 35 — so truncation is a width guarantee, not an expected outcome.

7. **The `Identifier` column is last and is not padded.** Rendered rows carry no trailing whitespace.

8. **The footer is seven lines** — an `=` rule, the marker legend, a `-` rule, three command lines, an `=` rule — replacing today's nine.
   - The marker legend is a **single line** listing all three markers with their meanings: `> in use`, `* installed`, `+ local only`. It is not the non-java view's three-line stacked form; only the marker vocabulary is shared.
   - The three command lines present the `sdk install java` forms in an aligned command column, each with its description alongside: install a specific version, install the default, complete an available identifier.
   - The default identifier appears at the **end** of its line, so a longer default cannot disturb the column alignment.
   - The `-` rule separating legend from commands is the same width and character as the table's header rule.

9. **The footer makes no claim about how to exit the list view.** Today's closing `Hit Q to exit this list view` is removed. Paging is a client-side concern: the CLI pipes through `$PAGER` when set, otherwise `less` when available, otherwise not at all, so the statement is false for some users and the service cannot tell which.

10. **Unrecognised vendor shortcodes group under `Unclassified`** and keep their real suffix in the `Identifier` column, as today. The removal of `Dist` eliminates the previous contradiction where such a row displayed `none`.

11. **Grouping, group ordering and within-group version ordering are unchanged.** Only the rendering of each row changes.

12. **The `Use` header label is retained.** It now heads a cell carrying both markers; the footer legend disambiguates.

13. **The heading block above the table is unchanged** — the `=` rule, `Available Java Versions for <platform>`, and the `=` rule keep their current text and position.

## Examples

The rendered output is the whole contract, so these exemplars are the specification of it. Each states the input condition, then the exact bytes it must produce. The full-page form is the reference rendering above; the fragments below isolate one rule each.

### The defect, and its fix

Three Liberica versions of the 21 line, none installed. Today the first hyphen is mistaken for the start of the vendor shortcode, so two distinct versions both display as `21.0.12`, and the reconstructed identifiers run past the rule — these two rows are 82 and 84 characters:

```
 Vendor        | Use | Version      | Dist    | Status     | Identifier
--------------------------------------------------------------------------------
               |     | 21.0.12+1.1  | librca  |            | 21.0.12+1.1-librca
               |     | 21.0.12      | librca  |            | 21.0.12-fx+1.1-librca
               |     | 21.0.12      | librca  |            | 21.0.12-crac+1.2-librca
```

Required:

```
 Vendor         | Use | Version            | Identifier
--------------------------------------------------------------------------------
                |     | 21.0.12+1.1        | 21.0.12+1.1-librca
                |     | 21.0.12-fx+1.1     | 21.0.12-fx+1.1-librca
                |     | 21.0.12-crac+1.2   | 21.0.12-crac+1.2-librca
```

### Marker states

`25.0.4-tem` in use and installed; `21.0.12+1.1-tem` installed; `17.0.20-tem` neither; `11.0.3-local` installed locally under an unrecognised shortcode:

```
 Vendor         | Use | Version            | Identifier
--------------------------------------------------------------------------------
 Temurin        | > * | 25.0.4             | 25.0.4-tem
                |   * | 21.0.12+1.1        | 21.0.12+1.1-tem
                |     | 17.0.20            | 17.0.20-tem
 Unclassified   |   + | 11.0.3             | 11.0.3-local
```

### Truncation guard

Synthetic — no live version reaches this width, and none is expected to. A version of 20 characters is cut to the column's 18; the row stays within 80 rather than overflowing:

```
 Vendor         | Use | Version            | Identifier
--------------------------------------------------------------------------------
 Liberica       |     | 21.0.12-crac+1.2.3 | 21.0.12-crac+1.2.3.4-librca
```

### Empty listing

A platform with no visible java versions (today: `sunos`, `freebsd`). The heading, the column header and the rule are still emitted; the trailing `=` rule below opens the footer, which takes the same seven-line form as in the reference rendering:

```
================================================================================
Available Java Versions for Solaris
================================================================================
 Vendor         | Use | Version            | Identifier
--------------------------------------------------------------------------------
No versions available for your platform at this time.
================================================================================
```

## Out of Scope

- The non-java list view (`GET /candidates/:candidate/:platformId/versions/list`). Its layout, its legend and its renderer are untouched.
- The State API — no endpoint, query-parameter or schema changes.
- Version ordering and vendor grouping. Rows appear in the same sequence as before.
- Any other endpoint: `/versions/all`, `/default/:candidate`, `/validate/…`, `/candidates/list`.
- Widening the table beyond 80 characters, or making the width terminal-dependent.
- The vendor shortcode → display-label mapping. No labels are added, removed or renamed.
- Reintroducing anything into the space reclaimed from `Dist` and `Status`. The surplus is deliberate headroom for qualifiers that continue to grow.
- The forthcoming Rust CLI list implementation. This is a fix to the current service, not a step towards it.

## Acceptance Criteria

- [ ] `GET /candidates/java/linuxx64/versions/list` returns a table with four columns — `Vendor`, `Use`, `Version`, `Identifier` — and separators at columns 17, 23 and 44.
- [ ] A Liberica group containing `21.0.12+1.1`, `21.0.12-fx+1.1` and `21.0.12-crac+1.2` renders three visibly distinct `Version` cells.
- [ ] Every line of the response is at most 80 characters, asserted by a test that does **not** strip trailing whitespace — the existing Cucumber body assertion trims line ends and therefore cannot prove column width on its own.
- [ ] A value exceeding its column width is truncated rather than overflowing the row.
- [ ] The version currently in use and also installed renders `> *`; installed-only renders `  *`; local-only renders `  +`; neither renders three spaces.
- [ ] The footer is seven lines: `=` rule, one-line marker legend, `-` rule, three aligned command lines, `=` rule.
- [ ] The footer's three command lines share a command column and a description column, and the default identifier appears at the end of its line.
- [ ] The response no longer contains `Hit Q to exit this list view`.
- [ ] No rendered row carries trailing whitespace.
- [ ] The non-java list view's output is byte-for-byte unchanged.
- [ ] Test fixtures cover at least one hyphen-qualified java version with a vendor (e.g. `21.0.12-crac+1.2` / `librca`) and one identifier at or beyond the previous 20-character ceiling — neither shape is present in the current suite, which is why this defect shipped.
- [ ] `sbt test` and `sbt scalafmtCheck Test/scalafmtCheck` pass.

## References

- Table layout — rejected alternatives: keeping `Status` as words, and keeping `>>>` with a one-character status column. Both fit 80 columns but leave 1 and 8 characters of headroom respectively against this option's 12.
- Footer layout — rejected alternatives: a label-column form (`Markers` / `Install` / `Exit` down the left edge), with and without an exit hint. Both were a line shorter but reintroduced a second vertical alignment to maintain.
- Marker vocabulary (`>`, `*`, `+`) originates in the non-java list view: `app/views/version_list.scala.txt`. This view borrows the symbols, not the three-line legend format.
- Pager behaviour that makes the removed exit hint unreliable: `__sdkman_echo_paged` in `../../cli/sdkman-cli/src/main/bash/sdkman-utils.sh`.
- Live data used to size the columns: `https://api.sdkman.io/2/candidates/java/linuxx64/versions/list` — 92 rows, longest identifier 23 (`21.0.12-crac+1.2-librca`), longest version 16 (`21.0.12-crac+1.2`), longest vendor label 14 (`GraalVM Oracle`).
