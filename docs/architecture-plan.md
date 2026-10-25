# Architecture evaluation and improvement plan

## Scope and evidence

This is a behavior-preserving Java 17 refactor of the two existing constructions,
not a redesign of knitting formulas. The starting verification passed 129 tests.
Existing uncommitted work, personal example inputs, generated output, and the
untracked Python directory are outside this change.

Primary evidence:

- [Main](../src/no/tuva/knitproj/Main.java) mixes argument parsing, selection,
  help formatting, application routing and filesystem coordination.
- [PatternInput](../src/no/tuva/knitproj/cli/PatternInput.java) represents sweater
  configuration as T-shirt options, then the entry point reconstructs sweater
  options. This loses the meaning of the selected construction.
- [TeeOptions](../src/no/tuva/knitproj/domain/TeeOptions.java) and
  [SweaterOptions](../src/no/tuva/knitproj/domain/SweaterOptions.java) load bundled
  resources in constructors. The domain depends on specifications that themselves
  construct domain values, and custom sweater specifications can inherit the
  bundled cuff restriction unintentionally.
- [PatternGenerator](../src/no/tuva/knitproj/application/PatternGenerator.java)
  and [RaglanPatternGenerator](../src/no/tuva/knitproj/application/RaglanPatternGenerator.java)
  already provide useful I/O-free generation modules.
- Shared conversions, typed calculation results, separate formatting, explicit
  input errors, and characterization snapshots are strengths to retain.
- Templates still contain construction-specific prose. Do not force distinct
  short-row and round-based instructions into one generic algorithm.
- [PatternResult](../src/no/tuva/knitproj/domain/PatternResult.java) and
  [PatternSection](../src/no/tuva/knitproj/domain/PatternSection.java) accept caller
  lists without defensive copies. Their immutability is conventional, not
  enforced. This is lower priority than selection and default correctness.
- [Gauge](../src/no/tuva/knitproj/domain/Gauge.java) exposes a vertical row count
  under the legacy name `getStitchesPer10CmHeight`. Preserve compatibility now;
  a future explicit row accessor can improve readability without changing math.
- [PatternTextFormatter](../src/no/tuva/knitproj/service/PatternTextFormatter.java)
  and [PatternFileWriter](../src/no/tuva/knitproj/service/PatternFileWriter.java)
  already isolate presentation and writes. Moving them solely to rename packages
  would add churn without meaningful depth.

## Priority and expected leverage

| Priority | Improvement | Expected result |
| --- | --- | --- |
| P0 | Validate sweater options against the selected specification | Remove bundled-default contamination from custom specifications |
| P1 | Typed generation request and thin CLI adapter | Add or call a construction without transporting the wrong option type |
| P1 | Explicit default factories outside domain | Make resource loading and selected defaults visible to maintainers and tests |
| P1 | Source-grounded wiki and independent regression checks | Make entry points, invariants and extension workflow discoverable |
| Later | Defensive copying and row naming migration | Strengthen value semantics with separately reviewed compatibility tests |
| Later | Further text extraction where wording genuinely repeats | Improve editorial locality without merging distinct constructions |

## Target structure

```text
CLI arguments + UTF-8 measurements
              |
        typed selection
              |
     application generation
       /               \
 T-shirt module    raglan module
       \               /
       pattern + calculation trace
              |
      formatter -> file writer
```

Domain values validate themselves without loading resources. Specifications own
pattern-specific defaults and create options explicitly. A small typed
application seam hides routing from the CLI without erasing the individual
construction results. No reflection, plugin framework, formula language, runtime
AI, new dependencies, or source-tree migration is needed for two patterns.

## Delivery and ownership

1. **Typed selection and orchestration.** One implementation agent owns Main,
   CLI files, new application routing files and CLI tests. Preserve argument
   handling, aliases, output filenames, exit codes and legacy input entry points
   where practical. The actual execution path must carry sweater options as
   sweater options.
2. **Explicit defaults and dependency direction.** A second implementation agent
   owns option records, specification factories, existing construction generators,
   calculator/template default overloads and their tests. Remove hidden resource
   loading from domain constructors; enforce cuff compatibility against the
   selected specification in the construction calculation.
3. **Independent verification.** Separate agents review and test each improvement
   after implementation. They do not approve knitting formulas or merge changes.
4. **Linked LLM wiki.** A third set of agents writes complementary source-grounded
   pages: architecture/data flow, and specifications/calculation/testing guides.
   Include an index, links between concepts, evidence links, limitations and an
   update log. This is a maintained repository wiki, not a runtime agent system.
5. **Integration.** The orchestrator resolves findings, verifies the whole build
   and packaged CLI, checks documentation links, and summarizes remaining debt.

Shared interface agreement: specification factories expose `defaultOptions()`
and `optionsWithSleeveLength(double)` for their own option type. CLI execution
uses explicit option constructors/factories, never removed convenience
constructors. Domain options retain complete explicit value constructors.
Any source-level migration away from implicit constructors must be documented.
Implementation agents have disjoint file ownership and do not modify examples,
golden files, other agents' files, instruction files, or documentation.

## Acceptance gates

- All existing numerical cases, rounding rules and Norwegian snapshot output
  remain unchanged; never regenerate snapshots to hide a regression.
- Invalid requests fail before file writing, with existing exit-code categories.
- Both selectors and the requested spelling alias work in the packaged JAR.
- Defaults are defined by the selected specification; injected specifications
  control both calculations and generated wording.
- Domain production files have no resource/specification/application dependency.
- Focused tests cover new typed routing, defaults, incompatible options and
  application use without filesystem writes.
- Full `./mvnw verify`, whitespace checks and wiki link checks pass.

## Deferred deliberately

- Formula corrections, new sizing guarantees and knitting approval.
- Rewriting all templates into one universal construction engine.
- Relocating the entire Java tree or adopting a framework merely for convention.
- Consolidating the unrelated Python directory without a separate behavior audit.
- Automated wiki ingestion or publication: documentation remains local and
  reviewable, with explicit provenance rather than invented authoritative claims.

## Integration evidence and blocker

The first complete integration passed 163 tests and both packaged constructions
matched the captured pre-refactor synthetic-default output byte-for-byte.
All 157 relative file links across this plan and the wiki resolved.
Both independent reviewers found no production defect in their assigned changes.

A later re-verification failed after concurrent edits changed T-shirt bust
factors from NORMAL 1.0 / OVERSIZED 1.1 to 1.1 / 1.3. The architecture agents did
not own or edit that specification resource. Those values conflict with the
preserved numerical cases and snapshots. They have not been reverted, nor have
tests or golden files been rewritten. Final completion is blocked until the
owner chooses whether to restore the previous behavior or separately approve
and characterize the changed scaling. The earlier passing evidence does not
establish that the currently edited configuration passes.

Follow-up: the user subsequently clarified that the scaling edits were an
intentional experiment and approved the T-shirt full armhole-budget formula.
The factors remain unchanged. Historical arithmetic tests now inject their
historical scaling explicitly and use an explicit combined-depth budget to
retain the original golden file. Current behavior has dedicated numerical
generation tests. Verification now passes 169 tests and the rebuilt JAR
reproduces the approved 24/28-gauge example; the earlier blocker is resolved.
See [wiki updates](wiki/updates.md) for the follow-up evidence.
