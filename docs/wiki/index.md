# Knitproj wiki

Java 17 is the authoritative engine for deterministic Norwegian T-shirt and
oversized raglan patterns. This wiki explains the current implementation through
linked concepts and source evidence; it is not a runtime LLM service or an
automatically ingested knowledge base.

## Start with the question

| Question | Page |
| --- | --- |
| Where does a CLI request become a pattern? | [Architecture and typed routing](architecture.md) |
| Which defaults, properties and text are used? | [Configuration boundaries](configuration.md) |
| Which counts and rounding rules must survive edits? | [Calculations](calculations.md) |
| How do I add a construction without copying mathematics? | [Extending patterns](extending-patterns.md) |
| What does verification establish? | [Testing](testing.md) |
| What still needs a human decision? | [Open questions](open-questions.md) |
| When and why was this explanation updated? | [Update log](updates.md) |

## Reading the evidence

Implementation links identify current symbols, not a promise that every layer
has perfect isolation. The [product contract](../product-contract.txt) records
preserved arithmetic and unresolved knitting decisions. The
[architecture plan](../architecture-plan.md) records the refactor's starting
problems and acceptance gates; its descriptions of former constructors and CLI
routing are historical, not the current API.

The delivery date is **2026-10-25**. No expert knitting approval, supported sizing
range or successful knit-through is established. Snapshots characterize output;
they do not approve fit. The checkout also contains an untracked Python directory
that this work has not assessed: “Java is authoritative” does not mean “no Python
exists.” Personal input files and generated output are not wiki evidence.

For build/run commands use the [README](../../README.txt); for repository change
rules use [AGENTS.md](../../AGENTS.md). When changing a concept, check its linked
sources and related pages, record the change in [updates](updates.md), and leave
unresolved decisions explicit rather than upgrading assumptions into guarantees.

**Source provenance:** [Main](../../src/no/tuva/knitproj/Main.java),
[generation service](../../src/no/tuva/knitproj/application/PatternGenerationService.java),
[product contract](../product-contract.txt), [architecture plan](../architecture-plan.md).
