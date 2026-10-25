# Open questions and approval gates

This page records unsettled work, not recommendations to change formulas.
Current implementation facts and their reference cases are in
[calculations](calculations.md); do not treat passing tests as knitting approval.

## Human knitting decisions — gates for behavior changes

No supported size range, expert-approved numerical fixture, or complete
knit-through is claimed. An experienced knitting reviewer must review numerical
cases across intended gauges, sizes, and fits, check the written instructions
against those counts, and complete a practical knit-through before the project
claims garment correctness. Until then, the factors and construction rules
below are implementation assumptions, not approved sizing advice.

For the T-shirt, unresolved review items include cumulative shoulder accounting
and short-row descriptions; independent horizontal-stitch and vertical-row
rounding; redistribution and shoulder-patch suitability; raglan denominator,
underarm additions and chest tolerance; and neck/armhole fit. The measurement
origins for back length and sleeve length also need confirmation.

For the oversized raglan, the fixed neck/chest/depth factors, symmetric
rounding, residual shoulder allocation, underarm accounting, and use of the
full armhole depth for the raglan stage remain unapproved assumptions.
Clarify its body-length origin and sleeve-length origin, and confirm whether
the current yoke-length calculation matches the intended construction. Raglan
depth is currently only one stage of the yoke; do not describe it as the whole
yoke. The sweater has no short rows, taper, waist shaping, or independently
fitted upper arm in the current implementation.

Decide separately whether unused measurements should drive new shaping, what
proportions and tolerances the product supports, and what construction variants
are in scope. Each approved behavior change needs a written rationale, old and
new numerical cases, reviewer approval, focused regression tests, and a review
of the resulting Norwegian instructions. Do not infer an answer from a
snapshot.

## Deferred engineering work — not formula approval

The [architecture plan](../architecture-plan.md) identifies lower-priority
maintenance work: defensive copies for result/section lists and an explicit
vertical-row gauge accessor while preserving the legacy API. It also defers
consolidating the Python directory: that directory is unassessed and outside
this documentation task, so this page makes no claim about its contents or
behavior. Java 17 is the authoritative engine documented for this delivery;
no cross-language equivalence or consolidation conclusion is made here.

Other deliberate deferrals include avoiding a universal template/calculation
framework for two distinct constructions and deciding whether currently
repeated wording merits further extraction. These are architecture questions,
not reasons to alter calculations. The current locally edited example inputs
and generated `output/my-*` files are outside scope and must not be copied into
wiki fixtures; use synthetic cases only.

## Provenance and maintenance

For calculation behavior, use the implementation and tests linked from
[calculations](calculations.md), then check the [product contract](../product-contract.txt)
for historical decisions and human gates. For test commands and the meaning of
snapshots, use [testing](testing.md). Update this page when a decision is
actually made and point to its reviewed evidence; do not fill open items with
inferred expert judgment.

The [wiki index](index.md), [architecture](architecture.md),
[configuration](configuration.md), and [extension guide](extending-patterns.md)
are planned crosslinks; their pages are not part of this documentation set.
