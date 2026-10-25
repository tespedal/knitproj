# Calculation model

This page describes the Java 17 calculation path and its current numeric
characterization. “Preserved” means preserved by the implementation and tests;
it is **not** knitting-expert approval, a fit guarantee, or a claim of a
successful knit-through. Formula changes require human review and new numerical
cases. See [open questions](open-questions.md) for the approval gates.

## Shared conversions

The two constructions share [`StitchCalculator`](../../src/no/tuva/knitproj/service/StitchCalculator.java).
For a length \(L\) cm and gauge \(G\) stitches or rows per 10 cm, it computes
`Math.round(L * G / 10)`, then increments the result if it is odd. This is
round-to-integer-then-raise-odd, **not** nearest-even rounding. Width conversions
produce stitch counts; height conversions produce row counts. The vertical
gauge is called `rowsPer10Cm` in input, despite a legacy Java accessor name.
Two-row repeats are integer division by two; a leftover row is not redistributed.

The T-shirt converts circumference directly. The sweater uses two independently
rounded even half-circumferences (`2 * E(circumference / 2, gauge)`) for neck and
chest, so those targets are multiples of four. They are separate policies, not
interchangeable approximations. Conversion limits are arithmetic safety bounds,
not sizing limits. See the [conversion tests](../../src/test/java/no/tuva/knitproj/service/CalculationServicesTest.java)
and [product contract](../product-contract.txt).

## T-shirt: cumulative shaping

[`TeeCalculator`](../../src/no/tuva/knitproj/pattern/tee/TeeCalculator.java)
and [`TeeStage`](../../src/no/tuva/knitproj/pattern/tee/TeeStage.java) calculate
the legacy top-down T-shirt independently of the sweater.

- Bundled factors are editable; historical NORMAL bust/neck factors were
  1.0/1.3 and OVERSIZED 1.1/1.5. At the depth-budget update, the edited bust
  factors are NORMAL 1.1 and OVERSIZED 1.3. The bundled experiment currently uses
  `armholeDepthGain=1.1` and `shoulderWidthGain=0.85`; the reviewed comparison below
  pins depth gain 1.2 explicitly. These gains
  apply only to the T-shirt; shoulder gain replaces the former 0.8 factor.
  The bundled T-shirt parameters are in
  [`tshirt.properties`](../../src/main/resources/patterns/tshirt.properties).
  Scaling lives exclusively in the selected complete spec (`--spec` replaces the
  bundle); measurement input cannot override gains. See [configuration](configuration.md).
- The ten neck-shaping rounds are followed by shoulder shaping calculated
  cumulatively from cast-on. Calculate bust-driven raglan repeats first using
  the existing chest formula. Armhole rows come from the **full** gained
  armhole depth, without halving. Deduct two rows per raglan repeat, then
  allocate remaining rows to two-row sleeve-only increase repeats. Negative
  remaining rows are rejected; zero sleeve-only repeats explicitly skip the
  stage. Knitting order remains sleeve-only then raglan. The combined depth
  starts after shoulder shaping, not at the neck.
- Stage values distinguish body stitches from shoulder/underarm stitches per
  side. At separation the shoulder/underarm count is exchanged for the initial
  underarm allocation; it is not an extra body circumference. The stage result
  and `explain()` trace are structured calculation output, not pattern approval.

### Historical T-shirt reference cases (before the depth-budget change)

Cases below use synthetic repository defaults, not user measurements:
neck 35 cm, shoulder width 46 cm, shoulder across 4 cm, bust 89 cm, and
armhole depth 21 cm; gauge is 22 stitches / 28 rows per 10 cm. The T-shirt
back-length default is 41 cm, fit is NORMAL, and its sleeve default is 10 cm.
See [`Measurements`](../../src/no/tuva/knitproj/domain/Measurements.java),
[`Gauge`](../../src/no/tuva/knitproj/domain/Gauge.java), and the
[contract](../product-contract.txt). They are regression fixtures, not
recommended sizes.

| Stage | Historical gains 1.0 / 0.8: body | shoulder each | total | Former defaults 1.2 / 0.85: body | shoulder each | total |
|---|---:|---:|---:|---:|---:|---:|
| Cast on | 84 | 10 | 104 | 84 | 10 | 104 |
| After neck | 124 | 10 | 144 | 124 | 10 | 144 |
| After shoulder | 164 | 10 | 184 | 172 | 10 | 192 |
| After armhole increases | 164 | 40 | 244 | 172 | 46 | 264 |
| After raglan increases | 184 | 50 | 284 | 184 | 52 | 288 |
| After armhole separation | 184 | 10 | 204 | 184 | 10 | 204 |

The historical case has 15 armhole repeats, 5 raglan repeats, and 60 stitches
per completed sleeve; the former default case has 18, 3, and 62 respectively.
The original golden file remains unchanged, tested with explicit historical
scaling and a 40-row combined budget (gain `40 / (21 * 2.8)`).
These numbers are historical, not the current depth formula. Evidence:
[`TeeCalculatorTest`](../../src/test/java/no/tuva/knitproj/pattern/tee/TeeCalculatorTest.java)
and the [contract's reference arithmetic](../product-contract.txt).

### Approved full-depth comparison

For neck 35, shoulder width 46, shoulder patch 4, bust 89 and depth 21 cm;
gauge 24 stitches / 28 rows per 10 cm; NORMAL neck/bust factors 1.3/1.1;
armhole/shoulder gains 1.2/0.85:

| Quantity | Previous calculation | Full combined budget |
| --- | ---: | ---: |
| Raglan repeats | 9 | 9 |
| Sleeve-only repeats | 18 | 27 |
| Combined shaping rows | 54 | 72 |
| Actual combined depth, cm | 19.3 | 25.7 |
| Total stitches before separation | 352 | 388 |
| Body after separation | 244 | 244 |
| Held stitches per sleeve | 64 | 82 |
| Sleeve after pickup | 74 | 92 |

`21 * 1.2 * 2.8 = 70.56` rounds to 71, then is raised to 72.
The 18 raglan rows leave 54 sleeve-only rows. The user approved this formula;
it still does not establish fit. Exact counts, row-gauge independence, zero
remaining rounds and insufficient-depth rejection are tested through generation
in [TeeArmholeBudgetTest](../../src/test/java/no/tuva/knitproj/pattern/tee/TeeArmholeBudgetTest.java).

## Oversized raglan sweater: depth stage and residual shaping

The sweater uses separate [`RaglanCalculator`](../../src/no/tuva/knitproj/pattern/raglan/RaglanCalculator.java),
[`RaglanStage`](../../src/no/tuva/knitproj/pattern/raglan/RaglanStage.java), and
[`RaglanCalculationResults`](../../src/no/tuva/knitproj/pattern/raglan/RaglanCalculationResults.java).
Its current fixed factors are neck 1.3, chest 1.5, and **full armhole depth**
1.4. Fit and T-shirt gain settings do not stack with these factors. The bundled
values and finish defaults are in
[`oversized-raglan.properties`](../../src/main/resources/patterns/oversized-raglan.properties).

The order is cast-on, neck increases, residual shoulder increases, then
depth-driven raglan increases. The depth conversion gives raglan-stage rows;
two-row repeats are `rows / 2`. Thus raglan depth is one shaping stage, **not
the entire yoke depth**: neck and shoulder rounds also contribute to yoke length.
The implementation derives the remaining shoulder repeats from the chest target
after neck and depth shaping; a negative residual is rejected instead of being
silently truncated. Each repeat adds four body stitches; a raglan repeat also
adds two stitches to each sleeve. The underarm cast-on reuses `shoulderAcrossCm`
as each sleeve patch and each underarm allocation.

### Synthetic sweater reference

At the synthetic defaults listed above, with sweater body length 58 cm and
22 stitches / 28 rows per 10 cm (NORMAL is the input default; the sweater's
sleeve default is 45 cm):

| Stage | Front | Back | Sleeve each | Body | Total |
|---|---:|---:|---:|---:|---:|
| Cast on | 40 | 40 | 10 | 80 | 100 |
| After neck | 48 | 48 | 10 | 96 | 116 |
| After shoulder | 56 | 56 | 10 | 112 | 132 |
| After raglan | 138 | 138 | 92 | 276 | 460 |
| Body after separation | — | — | — | 296 | — |

This case has 4 neck repeats, 4 shoulder repeats, 82 raglan rows / 41 raglan
repeats, and 102 stitches in each sleeve after underarm pickup. Its calculated
yoke is 35 cm excluding the 2 cm neckband; 17 cm of straight body plus a 4 cm
hem gives 21 cm below the underarm, and the default 45 cm sleeve consists of
41 cm straight knitting plus a 4 cm cuff. These are regression values and
construction assumptions, not fit approval. See
[`RaglanCalculatorTest`](../../src/test/java/no/tuva/knitproj/pattern/raglan/RaglanCalculatorTest.java)
and the [contract's sweater calculations](../product-contract.txt).

## Source of truth and updates

The formulas and stage accounting live in the construction calculators and
shared converter, not in templates or CLI code. The templates render computed
counts; do not copy arithmetic into them. For a formula or rounding change,
first obtain the knitting approval described in the [open questions](open-questions.md),
then update the source, focused numerical tests, and this page together.
Snapshots characterize generated text; they do not establish mathematical or
knitting correctness. The [wiki index](index.md) and related
[architecture](architecture.md), [configuration](configuration.md), and
[extension](extending-patterns.md) pages are planned crosslinks and may not yet
exist.
