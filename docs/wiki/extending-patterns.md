# Extending patterns: add one explicit construction

The two existing constructions share conversions and output structure, not a
plugin framework. Add a construction by following their explicit seams. Do not
make a generic template decide shaping arithmetic or copy the engine into another
language.

## Checklist

1. **Agree construction policy first.** Record measurement meanings, stage
   ordering, fit factors, rounding and length origins in the
   [product contract](../product-contract.txt). Obtain explicit human knitting
   approval for changed mathematics, sizing or allowances. Existing snapshots
   are characterization, not approval.
2. **Define validated values and defaults.** Reuse `PatternRequest` where its
   meanings fit; introduce explicit options for genuinely different inputs.
   Keep resource loading outside domain constructors. A construction spec owns
   its validated schema, `defaultOptions()` and `optionsWithSleeveLength(double)`.
   Specs exclusively own scaling and construction settings; measurement input
   contains wearer values, gauge, fit and desired lengths, never factor overrides.
   Validate cross-values such as cuff/sleeve compatibility against the selected
   spec, including when callers construct options directly.
3. **Implement stage accounting in a calculator.** Reuse
   [StitchCalculator](../../src/no/tuva/knitproj/service/StitchCalculator.java)
   for compatible conversions and two-row repeats; preserve the distinction
   between horizontal stitches and vertical rows. Pass the selected spec directly
   to the calculator; avoid a separate factor/default source. Expose named stage/results
   types and a trace rather than hiding residual stitches in prose.
4. **Render from results.** Produce `PatternSection` / `PatternParagraph`
   values in a construction template. Reuse checked text keys only for genuinely
   compatible wording. Preserve Norwegian output and keep calculations out of
   properties and prose. Compose calculator and template in an I/O-free generator
   that returns typed calculations alongside `PatternResult`.
5. **Extend explicit selection.** Add a `PatternType` value, deliberate selector
   spelling/aliases and a distinct default filename. Add the appropriate
   `GenerationRequest` record and permitted subtype, then update
   Main's selected-only composition and `PatternGenerationService.generate`
   programmatic routing. Its current final cast assumes
   the only non-T-shirt case is raglan; simply adding a permitted subtype is not
   sufficient.
6. **Adapt input and help.** Update the typed `PatternInput` methods,
   property-key validation and selected defaults; never smuggle new options
   through its legacy `TeeOptions` wrapper. Update `CliHelp` and relevant command
   documentation. `--spec PATH` is a complete external UTF-8 replacement for the
   selected construction, not an overlay. Load it once and pass the same instance
   through parser defaults, calculation and spec-dependent wording. Do not load
   unselected construction resources. Do not introduce a registry or formula DSL.
7. **Verify before integration.** Add synthetic numerical cases, invalid geometry,
   stage conservation/symmetry checks, option/default injection tests, typed
   routing tests and intentional Norwegian snapshots. Exercise selectors,
   output paths, help and exit categories. Run focused tests, then `./mvnw verify`
   with numerical cases pinned to explicit specs (not editable bundled factors).
   Check external edits between invocations without rebuilding, wrong-pattern and
   incomplete schemas, rejected input scaling, selected-only resource loading,
   and the packaged CLI. Do not regenerate old golden files merely to pass.
8. **Maintain the explanation.** Link the new sources from this wiki, update
   [calculations](calculations.md), [testing](testing.md) and
   [open questions](open-questions.md), and record evidence in [updates](updates.md).
   Keep approval status and unassessed behavior explicit.

## Existing interface reference

`PatternGenerator.generate(PatternRequest[, TeeOptions])` returns
`GeneratedPattern`; `RaglanPatternGenerator.generate(PatternRequest[,
SweaterOptions])` returns `GeneratedRaglanPattern`. These are overload summaries,
not Java optional-parameter syntax. Each exposes `calculations()` and `pattern()`.
The common `PatternGenerationService.generate(GenerationRequest)` returns
`GeneratedDocument(pattern, trace)` for adapters that do not need typed counts.

Avoid extending the legacy input wrapper as the primary public model. Preserve
its compatibility deliberately, or document a separately reviewed migration.
There is no need for reflection, dependency injection infrastructure, dynamic
plugin discovery or a universal construction DSL to add one explicit pattern.

**Source provenance:** [GenerationRequest](../../src/no/tuva/knitproj/application/GenerationRequest.java),
[routing service](../../src/no/tuva/knitproj/application/PatternGenerationService.java),
[PatternType](../../src/no/tuva/knitproj/domain/PatternType.java),
[PatternInput](../../src/no/tuva/knitproj/cli/PatternInput.java),
[CliHelp](../../src/no/tuva/knitproj/cli/CliHelp.java),
[RaglanPatternGenerator](../../src/no/tuva/knitproj/application/RaglanPatternGenerator.java),
[architecture plan](../architecture-plan.md).

**Related:** [Architecture](architecture.md) · [Configuration](configuration.md) ·
[Calculations](calculations.md) · [Testing](testing.md) · [Index](index.md)
