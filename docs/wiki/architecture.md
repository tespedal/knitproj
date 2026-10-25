# Architecture: typed selection, separate constructions

The application seam selects a construction without pretending its options or
intermediate counts are interchangeable. A shared request contains measurements,
gauge and fit; a sealed selection pairs it with the appropriate option type.

## Actual runtime path

```text
arguments
   -> CliArguments.parse                 selectors, paths, explain/help
   -> Main.run
        -> CliHelp.text                  help exits without generation
        -> load selected TeeSpec | SweaterSpec once
             --spec complete external file OR selected bundled resource
        -> PatternInput.requestFromProperties(properties, selectedSpec)
             UTF-8 wearer properties + selected sleeve default -> validated values
             -> GenerationRequest.Tee | GenerationRequest.Raglan
        -> selected generator (same spec instance)
             PatternGenerator           -> TeeCalculator -> TeeTemplate
             OR RaglanPatternGenerator  -> RaglanCalculator -> RaglanTemplate
             <- GeneratedDocument(PatternResult, trace)
        -> PatternTextFormatter.format
        -> PatternFileWriter.writeToFile
        -> optional source + effective scaling + trace on stdout
```

Calculation precedes template rendering, formatting and writing. `Main.run`
returns 0 for success/help, 2 for `IllegalArgumentException`, and 1 for
`IOException`; `main` converts that return value to the process exit code.
The optional programmatic routing service does not format, print or write files.
Main deliberately bypasses its eager default constructor rather than loading
unselected construction resources. Even an external selected spec does not
require either bundled construction file. Shared Norwegian text remains bundled.

## Responsibility map

| Owner | Responsibility and boundary |
| --- | --- |
| [Domain](../../src/no/tuva/knitproj/domain/PatternRequest.java), [options](../../src/no/tuva/knitproj/domain/SweaterOptions.java) | Validate values; option constructors require explicit values, not resource reads. |
| [Specifications](../../src/no/tuva/knitproj/pattern/spec/SweaterSpec.java) | Sole owners of scaling/construction and sleeve defaults; validate complete schemas and manufacture sleeve-only options. |
| [Calculators](../../src/no/tuva/knitproj/pattern/raglan/RaglanCalculator.java) | Construction geometry and stage accounting; share conversions, not one universal shaping algorithm. |
| [Templates](../../src/no/tuva/knitproj/pattern/raglan/RaglanTemplate.java) | Norwegian sections from calculated values, with some construction-specific prose still inline. |
| [Application service](../../src/no/tuva/knitproj/application/PatternGenerationService.java) | Route the sealed request and expose pattern plus explanation. |
| [CLI input](../../src/no/tuva/knitproj/cli/PatternInput.java) | Read personal properties, reject unknown keys, select defaults and typed options. |
| [Main](../../src/no/tuva/knitproj/Main.java) | Coordinate adapters, writing, console messages and exit categories. |

## Two useful application interfaces

Given an already validated `PatternRequest request`, the common path is:

```java
var spec = SweaterSpec.bundled();
GenerationRequest selection =
        new GenerationRequest.Raglan(request, spec.defaultOptions());
GeneratedDocument document = new PatternGenerationService().generate(selection);
// document.pattern() is structured output; document.trace() is explanatory text.
```

`GenerationRequest.Tee` accepts `TeeOptions`; `GenerationRequest.Raglan` accepts
`SweaterOptions`. `GeneratedDocument` deliberately does not carry typed counts.
For inspection or numerical integration retain the individual APIs:

```java
GeneratedPattern tee = new PatternGenerator().generate(request);
GeneratedRaglanPattern raglan = new RaglanPatternGenerator().generate(request);
// tee.calculations() / raglan.calculations() retain construction-specific results.
```

The types above live in `application`; specifications live in `pattern.spec`.
See [configuration](configuration.md) before supplying a custom specification.

## Compatibility is not the runtime model

`PatternInput` remains a record of `(PatternRequest request, TeeOptions options)`.
Its `defaults`, `read` and `fromProperties` overloads are compatibility wrappers,
including overloads taking `PatternType`. They still return T-shirt-shaped
options even for raglan selection. New callers should use `defaultRequest`,
`readRequest` or `requestFromProperties`, which return `GenerationRequest`.
`Main` uses selected-spec typed overloads and never reconstructs sweater options
from `PatternInput.options()`. All adapters reject construction/scaling keys
in measurements with a `--spec` migration diagnostic.

The boundaries are pragmatic, not a claim of strict clean architecture:
default application/calculator/template constructors can load bundled resources;
both templates instantiate `PatternText`. `PatternGenerationService()` eagerly
creates both bundled generators for programmatic convenience; it is not the
selected-only CLI composition root. Use its injected constructor or an individual
typed generator for external specs. `TeeCalculator` takes a `TeeSpec` directly,
not a potentially mismatched ease calculator and gain-bearing options.
`PatternResult` and `PatternSection` retain caller lists without defensive
copies. These are actual limitations, not guarantees of purity or immutability.

**Source provenance:** [GenerationRequest](../../src/no/tuva/knitproj/application/GenerationRequest.java),
[GeneratedDocument](../../src/no/tuva/knitproj/application/GeneratedDocument.java),
[PatternGenerator](../../src/no/tuva/knitproj/application/PatternGenerator.java),
[RaglanPatternGenerator](../../src/no/tuva/knitproj/application/RaglanPatternGenerator.java),
[CliArguments](../../src/no/tuva/knitproj/cli/CliArguments.java),
[PatternResult](../../src/no/tuva/knitproj/domain/PatternResult.java),
[PatternSection](../../src/no/tuva/knitproj/domain/PatternSection.java).

**Related:** [Configuration](configuration.md) · [Calculations](calculations.md) ·
[Extending patterns](extending-patterns.md) · [Open questions](open-questions.md) ·
[Updates](updates.md) · [Index](index.md)
