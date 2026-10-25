# Configuration: three kinds of properties

Personal measurements, construction definitions and Norwegian wording are
different inputs. Sharing the Java properties format does not make their schemas
or responsibilities interchangeable.

| Kind | Source and consumer | Change policy |
| --- | --- | --- |
| Personal input | User-selected UTF-8 file; [PatternInput](../../src/no/tuva/knitproj/cli/PatternInput.java) | `--input` selects it; omitted keys use defaults; unknown keys fail. |
| Construction specification | Bundled [tshirt.properties](../../src/main/resources/patterns/tshirt.properties) / [oversized-raglan.properties](../../src/main/resources/patterns/oversized-raglan.properties), or complete external UTF-8 file via `--spec PATH`; `TeeSpec` / `SweaterSpec` | Rebuild for packaged changes only. External edits take effect next run without rebuilding. Never merged with bundled values. |
| Shared Norwegian text | Bundled [text-no.properties](../../src/main/resources/patterns/text-no.properties); [PatternText](../../src/no/tuva/knitproj/resources/PatternText.java) | Edit wording and named placeholders, not knitting arithmetic; rebuild. |

## Where defaults enter

`TeeOptions` and `SweaterOptions` each require `(double sleeveLengthCm)`.
Both validate finite, positive lengths without reading specifications. Scaling
has one owner: the selected spec, never options or measurement input.

Both specs expose `defaultOptions()` and `optionsWithSleeveLength(double)`,
returning their own option type. At this update the bundled T-shirt experiment
uses 10 cm sleeves, depth/shoulder gains 1.1/0.85 and NORMAL/OVERSIZED bust factors
1.1/1.3; the bundled sweater uses 45 cm sleeves. Resources remain editable;
these values are not hardcoded into domain options or help. Numerical defaults
are construction policy, not fit approval.

`PatternInput` supplies measurement defaults via `new Measurements()`, gauge
defaults of 22 stitches / 28 rows per 10 cm with a 5 mm needle, and `NORMAL` fit.
It obtains sleeve defaults from the selected spec. Only wearer measurements,
gauge, fit and desired lengths belong in measurement input. An explicit
`sleeveLengthCm` overrides only sleeve length, never scaling. Known construction
keys (including both gains, fit-specific factors, sweater factors/repeats and
finish lengths) fail with an actionable `--spec` migration message on **both**
patterns, including legacy input adapters. Unknown keys also fail.

## Runtime selection and migration

```sh
java -jar target/knitproj-1.0.0-SNAPSHOT.jar \
  --input examples/measurements.properties --spec examples/tshirt.properties \
  --output output/custom-pattern.txt --explain
```

The external file completely replaces the selected bundled spec, not an overlay.
Use the selected pattern's complete schema from the bundled files linked above.
T-shirt keys: `normal.neckFactor`, `oversized.neckFactor`, `normal.bustFactor`,
`oversized.bustFactor`, `armholeDepthGain`, `shoulderWidthGain`, `sleeveLengthCm`.
Sweater keys: `neckFactor`, `bustFactor`, `depthFactor`, `neckRepeats`,
`neckbandCm`, `hemCm`, `cuffCm`, `sleeveLengthCm`. All are required and positive
finite numbers; `neckRepeats` must also be a whole number within calculation
range. The sweater's default sleeve must exceed its cuff.

The example migration removed **only** the two gain entries and their obsolete
comment from the locally edited measurement example, without replacing or
publishing its measurements/gauge/lengths. New
[examples/tshirt.properties](../../examples/tshirt.properties) copies the
current bundled factors and sleeve default, but preserves the former input gains
exactly: depth 1.2 and shoulder 0.85. The command above continues that experiment;
omitting `--spec` instead uses the unchanged bundled depth gain 1.1.

For other inputs, copy the complete matching bundled spec to your own file, move
construction overrides there and remove those keys from measurement input.
No measurements belong in specs. External files are read on every invocation,
so edits need no rebuild or executable formula syntax. Formula/fit changes still
require the human review described in the [contract](../product-contract.txt).

`--explain` identifies bundled/external source and effective neck/bust/depth
scaling alongside stage counts, without dumping measurement input. It never
changes pattern bytes. `--help` performs no external input/spec I/O.

## Injecting a selected specification

Programmatic callers can build specs explicitly or use
`fromProperties(Properties)` with the complete specification schema:

```java
// reviewedSweaterProperties is a complete construction spec, not personal input.
var spec = SweaterSpec.fromProperties(reviewedSweaterProperties);
var generator = new RaglanPatternGenerator(spec);
GeneratedRaglanPattern result = generator.generate(request);
// generate(request) uses this spec's defaultOptions().
// generate(request, spec.optionsWithSleeveLength(lengthCm)) overrides the sleeve.
```

Use the same selected instance for parser defaults and generator composition:

```java
var teeSpec = TeeSpec.fromProperties(reviewedTeeProperties);
var selected = PatternInput.requestFromProperties(measurementProperties, teeSpec);
var generated = new PatternGenerator(teeSpec).generate(selected.request(), selected.options());
// The SweaterSpec overload returns GenerationRequest.Raglan and SweaterOptions.

// Optional programmatic multi-construction routing:
var service = new PatternGenerationService(
        new PatternGenerator(teeSpec), new RaglanPatternGenerator(spec));
var document = service.generate(
        new GenerationRequest.Raglan(request, spec.defaultOptions()));
```

`PatternInput.defaultRequest(type)` and the type-only parsing methods explicitly
choose bundled specs for convenience. Selected-spec overloads never load them.
Main loads exactly the selected spec once and bypasses the default
`PatternGenerationService()` constructor, which still eagerly creates both
bundled generators. That service remains available for programmatic typed routing;
use injected generators when selecting custom specs.

Source migration: replace `TeeOptions(length, depthGain, shoulderGain)` with
`TeeOptions(length)` and put gains in the selected `TeeSpec`. Replace
`TeeCalculator(EaseCalculator, StitchCalculator)` with
`TeeCalculator(TeeSpec, StitchCalculator)`. There is no options/parser gain
override channel. `PatternGenerator.generate(request)` uses injected spec
defaults; its explicit-options overload can override sleeve length only.
The sweater generator passes its spec to calculator and template, keeping
finish lengths and stated factors aligned.

`SweaterOptions` alone cannot validate a spec-dependent cuff. The
`SweaterSpec.optionsWithSleeveLength` factory checks sleeve length against that
spec's cuff, and `RaglanCalculator.calculate` checks again for explicitly
constructed options. This avoids silently applying the bundled cuff restriction
to an injected specification.

## Resource failures and limits

[BundledProperties](../../src/no/tuva/knitproj/resources/BundledProperties.java)
loads classpath resources as UTF-8; [Utf8Properties](../../src/no/tuva/knitproj/cli/Utf8Properties.java)
reads external files as UTF-8. Missing/unreadable external files exit 1.
Unknown/missing/wrong-pattern keys or invalid numbers exit 2 before output writes.
A missing value after `--spec` also exits 2. There is no bundled fallback after an
invalid external selection. `PatternText.render(key, values)` rejects missing
keys, missing or unused placeholder values, and unresolved braces; it does not
evaluate formulas. Text validation happens as rendering requests keys, not as a
complete up-front language-pack audit.

Not all prose has been extracted. Templates still contain construction-specific
instructions and instantiate bundled `PatternText`; injecting a spec is not
injecting a text catalogue. Do not claim arbitrary localization or a runtime
construction language.

**Source provenance:** [TeeOptions](../../src/no/tuva/knitproj/domain/TeeOptions.java),
[SweaterOptions](../../src/no/tuva/knitproj/domain/SweaterOptions.java),
[TeeSpec](../../src/no/tuva/knitproj/pattern/spec/TeeSpec.java),
[SweaterSpec](../../src/no/tuva/knitproj/pattern/spec/SweaterSpec.java),
[PatternGenerator](../../src/no/tuva/knitproj/application/PatternGenerator.java),
[RaglanPatternGenerator](../../src/no/tuva/knitproj/application/RaglanPatternGenerator.java),
[RaglanCalculator](../../src/no/tuva/knitproj/pattern/raglan/RaglanCalculator.java).

**Related:** [Architecture](architecture.md) · [Extending patterns](extending-patterns.md) ·
[Testing](testing.md) · [Open questions](open-questions.md) · [Updates](updates.md) ·
[Index](index.md)
