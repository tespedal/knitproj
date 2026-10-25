# Wiki update log

Entries record source changes and evidence, not automatic knitting approval.
Update the affected concept pages with the code; retain unknowns in
[open questions](open-questions.md).

## 2026-10-25 — initial wiki and architecture refactor

- Created the [index](index.md), [architecture](architecture.md),
  [configuration](configuration.md) and [extension checklist](extending-patterns.md),
  linked to the companion [calculations](calculations.md),
  [testing](testing.md) and [open questions](open-questions.md) pages.
- Documented the implemented sealed `GenerationRequest.Tee` / `.Raglan`,
  `GeneratedDocument` and `PatternGenerationService` seam. `Main` now delegates
  argument parsing/help and uses typed `PatternInput` request methods; the old
  T-shirt-shaped `PatternInput` record remains a compatibility wrapper.
- Documented explicit domain options, specification-owned default factories and
  injected-generator behavior. Sweater cuff compatibility is checked against the
  selected specification, including explicitly constructed options.
- Distinguished bundled specifications, bundled Norwegian text and personal
  properties. Recorded remaining resource-loading convenience paths, inline
  template prose and list-copying limitations rather than claiming complete
  dependency isolation.
- The [architecture plan](../architecture-plan.md) records a **129-test starting
  baseline**. The first integrated run passed **163 tests**. Both packaged
  constructions matched pre-refactor synthetic-default output byte-for-byte;
  all **157 relative file links** across the plan and wiki resolved.
- Final re-verification subsequently failed after the T-shirt specification was
  concurrently changed from bust factors NORMAL `1.0` / OVERSIZED `1.1` to
  `1.1` / `1.3`. Neither architecture implementation agent owned that resource.
  The existing arithmetic and snapshot expectations now disagree with these
  factors. No factors or golden files were overwritten to conceal the failure.
  Delivery is blocked pending human direction on this behavioral change; the
  earlier successful run is evidence for the earlier configuration only.
- Scope is a behavior-preserving refactor and source-grounded documentation,
  not formula/fit approval. No expert knitting approval or knit-through is
  established. Java 17 remains authoritative; the current untracked Python
  directory is unassessed. Inherited dirty files, personal input examples and
  generated outputs are not evidence for this delivery.

**Source provenance:** [architecture plan](../architecture-plan.md),
[product contract](../product-contract.txt),
[Main](../../src/no/tuva/knitproj/Main.java),
[generation service](../../src/no/tuva/knitproj/application/PatternGenerationService.java),
[TeeSpec](../../src/no/tuva/knitproj/pattern/spec/TeeSpec.java),
[SweaterSpec](../../src/no/tuva/knitproj/pattern/spec/SweaterSpec.java).

**Related:** [Index](index.md) · [Architecture](architecture.md) ·
[Configuration](configuration.md) · [Testing](testing.md) ·
[Open questions](open-questions.md)

## 2026-10-25 — approved T-shirt armhole budget

- User clarified and approved `armholeDepthCm * armholeDepthGain`, without
  halving, as the combined sleeve-only and raglan depth after shoulder shaping.
- Bust-driven raglan repeats are computed first; their rows are deducted before
  computing sleeve-only repeats. Insufficient depth fails before writing; zero
  sleeve-only repeats explicitly skip that stage.
- [Calculations](calculations.md) records the reviewed 24/28-gauge comparison:
  27 sleeve-only repeats, 9 raglan repeats, 72 combined rows, 244 body stitches
  and 92 stitches per sleeve after pickup.
- Editable T-shirt factors are preserved. Historical tests now pin historical
  factors explicitly; the original golden files remain unchanged. CLI tests
  follow selected configuration, while dedicated generation regressions assert
  approved numerical counts independently.
- `./mvnw verify` passes **169 tests**, zero failures/errors/skips. The rebuilt
  JAR with the current input reproduces the reviewed counts and Norwegian text.
  This supersedes the earlier blocked integration status; no sizing range,
  knitting-expert approval or knit-through is implied.

## 2026-10-25 — single-owner runtime configuration

- Measurement input now owns wearer values, gauge, fit and desired lengths only.
  Selected complete specs exclusively own scaling and construction. Removed
  gain-bearing `TeeOptions` and parser gain overrides, including legacy adapters;
  input construction keys now fail with a `--spec` migration diagnostic.
- `--spec PATH` reads a complete UTF-8 replacement at runtime. Main loads the
  selected spec once and passes it through defaults/calculation/wording, without
  loading unrelated construction resources. The programmatic routing service's
  eager bundled convenience remains documented; Main bypasses it.
- Migrated only the two gain entries and obsolete comment out of the edited
  measurement example. New `examples/tshirt.properties` retains those gains
  (1.2/0.85) with the current bundled factors and sleeve default. No wearer values
  were replaced or published. Bundled depth gain 1.1 and bust factors 1.1/1.3,
  original golden files, approved full-depth arithmetic and unrelated dirty work
  were preserved.
- Java 17 `./mvnw verify`: **194 tests**, zero failures/errors/skips.
  Runtime regressions cover editing a spec between Main calls without a rebuild,
  exact sleeve/bust accounting, selected defaults/wording, rejected overrides,
  invalid/wrong schemas, I/O statuses, help and pattern-byte-neutral explain.
  Isolated resource tests cover bundled and external selected-only composition.
- Packaged Java 17 smoke checks passed for bundled tee, bundled raglan, external
  tee, the migrated input/spec pair (without printing wearer values), and help
  with missing input/spec paths. All **165 relative documentation links** across
  the architecture plan and wiki resolved; `git diff --check` passed.
- This is configuration cleanup, not fit approval, a supported sizing range or
  a knit-through. Source/input migrations are documented in
  [configuration](configuration.md) and the [contract](../product-contract.txt).
