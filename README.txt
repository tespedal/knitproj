KNITPROJ — DETERMINISTIC NORWEGIAN T-SHIRT AND RAGLAN PATTERNS

Status
------
Java 17 is the authoritative implementation in this checkout. No second-language
implementation was found. Consolidation across languages remains blocked until
that implementation is supplied and compared with identical inputs.

The default output is regression-tested, NOT independently approved knitting
advice. No supported sizing range or successful knit-through is claimed.
See docs/product-contract.txt before changing the knitting mathematics.

Build and run
-------------
From the repository root, with JDK 17 and network access for the first build:

  ./mvnw verify
  java -jar target/knitproj-1.0.0-SNAPSHOT.jar --help
  java -jar target/knitproj-1.0.0-SNAPSHOT.jar --explain
  java -jar target/knitproj-1.0.0-SNAPSHOT.jar \
    --input examples/measurements.properties --spec examples/tshirt.properties \
    --output output/custom-pattern.txt
  java -jar target/knitproj-1.0.0-SNAPSHOT.jar --pattern oversized_raglan \
    --input examples/oversized-raglan.properties --explain

Use mvnw.cmd on Windows. The Maven wrapper pins the build-tool version.
The default output is output/tshirt-pattern.txt, relative to the working directory.
--pattern tshirt explicitly selects the unchanged default.
--pattern oversized_raglan selects the new top-down oversized long-sleeved sweater;
the requested spelling overzided_raglan is an exact alias. Its dedicated default
output is output/oversized-raglan-pattern.txt, so selecting it does not overwrite
the T-shirt. Unknown selectors are errors. --output overrides either default.
--output is an explicit user-selected path; existing files are overwritten.
Do not select the input file as the output path if you want to retain it.
--spec PATH selects a complete UTF-8 construction specification for the selected
pattern, replacing its bundled specification. Edit the external file and rerun:
no rebuild, overlay, executable formulas or new dependencies are involved.
Without --spec the selected bundled specification is used.
--explain identifies bundled/external source, effective scaling and stage counts;
it does not dump measurement input or change the Norwegian pattern file.
Exit codes: 0 success/help, 1 file I/O error, 2 invalid request or arguments.

Input
-----
Input is a UTF-8 Java properties file, not executable configuration. Omitted
properties use the documented defaults. Unknown property names are rejected.
Decimal values use a dot; gauge stitch/row counts must be whole numbers.
All lengths and body measurements use centimetres, needle size uses millimetres,
and gauge counts refer to 10 cm. See examples/measurements.properties for keys.
Input validation rejects invalid numbers and some incompatible geometry; passing
validation does not establish that a garment fits or is knit-ready.

Measurement input owns wearer measurements, gauge, fit and desired lengths only.
Scaling, construction and sleeve defaults belong exclusively to the selected
complete pattern specification. Input gain/factor/finish keys are errors with a
--spec migration message, including when selecting the sweater.

T-shirt specification gains (dimensionless, finite and greater than zero):
  armholeDepthGain        multiplies armholeDepthCm for the full shaping depth
  shoulderWidthGain       multiplies the full shoulderWidthCm measurement
Choose these in --spec independently of fit. The current bundled experiment has
depth gain 1.1, shoulder gain 0.85 and NORMAL/OVERSIZED bust factors 1.1/1.3.
These editable parameters are not approved sizing advice.
The shoulder gain replaces the former 0.8 factor; it is not multiplied by it.
The depth budget covers sleeve-only plus raglan rounds after shoulder shaping.
Raglan repeats are calculated from the bust target first; their two-round repeats
are deducted from the depth budget. Remaining two-round repeats increase sleeves
only. No halving is applied. Zero sleeve-only repeats skip that stage; insufficient
depth for the raglan rounds is rejected before writing output.
The older gains 1.0/0.8 alone no longer reproduce historical sleeve shaping.
Large shoulder gains can conflict with the bust target and are rejected rather
than silently changing chest sizing. Rounding can mask small gain adjustments.
Armhole depth gain adds vertical shaping room; it is not upper-arm circumference
ease. Defaults were changed following reported armhole tightness; fit still needs
a new knit-through. The tracked output/tshirt-pattern.txt is a historical example,
not the current default output.

Example migration
-----------------
Only armholeDepthGain=1.2, shoulderWidthGain=0.85 and their obsolete comment were
removed from examples/measurements.properties; all edited measurements, gauge
and lengths were retained. NEW examples/tshirt.properties is a complete copy of
the current bundled factors and sleeve default, with those two gains preserved.
To continue that experiment, use the --input / --spec command above. Running
without --spec intentionally selects the bundled depth gain 1.1 instead.
For other existing inputs, copy the selected bundled spec as a complete external
file, move construction overrides there, and remove them from measurement input.
Do not use a partial gains file: missing specification keys fail rather than
silently borrowing bundled defaults.

Oversized raglan (new, unapproved construction)
----------------------------------------------
Bundled factors are neck 1.3, bust 1.5 and full raglan-stage armhole depth 1.4.
The selected sweater spec owns these factors, neck repeats and finishing lengths.
They never stack with fit or T-shirt gains; gain keys in input are rejected.
shoulderWidthCm, underbust,
waist, backLengthCm and upperArmCircumferenceCm are also validated but unused.
Use shoulderAcrossCm for each initial sleeve patch AND each underarm cast-on.
The order is neckband, neck increases, residual shoulder increases, depth-driven
raglan, separation, straight body and two straight sleeves, ribbing and finishing.
There are no short rows, taper, waist shaping or independently fitted upper arms.

sleeveLengthCm defaults to 45 cm for the sweater (10 cm for the T-shirt), measured
from the underarm including a 4 cm cuff; it must exceed 4 cm. bodyLengthCm is the
sweater's total length from cast-on edge including 2 cm neckband and 4 cm hem.
Neck/shoulder/raglan rows count towards body length; insufficient room is an error.
Shoulder width is the residual chest target, not a separately fitted shoulder
measurement. Incompatible small or large chest/depth/length combinations are
rejected with suggestions, never silently changed to fit.

Chest and neck are rounded as two even half-circumferences using the shared
converter, giving multiples of four. The chest target includes both underarms;
no residual stitches are dropped. See docs/product-contract.txt for complete
formulas, exact reference counts, rounding bounds, and outstanding fit decisions.
These are pragmatic assumptions, NOT expert-approved sizing or a knit-through.

Architecture
------------
Start at docs/wiki/index.md for the linked architecture, configuration,
calculation and testing wiki. docs/architecture-plan.md records the evaluation,
improvement priorities, ownership and acceptance gates for this refactor.

Bundled pattern specifications live in src/main/resources/patterns:
  tshirt.properties             Fit-dependent scaling and T-shirt option defaults
  oversized-raglan.properties    Fixed sweater scaling, shaping and finish defaults
  text-no.properties            Shared Norwegian text with named {placeholders}
These are garment definitions, not personal measurement input files. Rebuild with
./mvnw verify after editing packaged resources. To experiment without rebuilding,
copy the selected construction file and pass the complete copy via --spec PATH.
External edits take effect on the next run, including between calls to Main.run.
Unknown/missing/wrong-pattern specification keys and invalid numbers exit 2
before output writing; missing/unreadable external files exit 1. Missing --spec
paths are argument errors (2); --help reads neither external file. Missing text
and unresolved placeholders fail explicitly. Java retains construction order
and mathematics; the spec is data, not a construction language.
The templates share only compatible wording; different shaping instructions are
not made interchangeable. Stitch/row conversion and two-row repeats are shared,
while each construction keeps its own accounting and rounding policy.

Production sources remain under src/no/tuva/knitproj; tests are under src/test.
No framework, runtime AI service, or distributed infrastructure is required.

  domain          Validated measurements, gauge, fit, and sleeve options
  pattern/tee     Legacy deterministic calculations, stages, Norwegian template
  pattern/raglan  Separate sweater counts, depth-driven shaping, Norwegian template
  application     PatternGenerator: request + options -> counts + pattern
                  PatternGenerationService: typed selection -> document + trace
  cli             Argument/help and UTF-8 properties adapters
  service         Ease/conversion, plain-text formatter, filesystem adapter
  Main            CLI orchestration and process exit status

PatternGenerator has no filesystem dependency. TeeCalculationResults exposes
structured stage counts and a human-readable trace. Templates consume computed
counts instead of deciding shaping arithmetic. Formatting is separate from file
writing. Sleeve length is configurable and defaults to the original 10 cm.
PatternGenerator/GeneratedPattern retain their typed construction results.
RaglanPatternGenerator.generate(PatternRequest[, SweaterOptions]) is the separate
I/O-free sweater API with explicit RaglanCalculationResults; SweaterOptions
defaults come from SweaterSpec (45 cm sleeves in the bundled specification).
PatternType parses the CLI selectors and aliases. The command-line path loads
only the selected construction specification once,
then passes the same instance to typed input parsing and its generator. The
sweater generator passes it to its calculator and template. No unrelated
construction resource is loaded. Legacy PatternInput methods remain sleeve-only
compatibility adapters; selected-spec overloads never reload bundled defaults.

Options are explicit validated domain values; they do not load bundled files.
Use TeeSpec.bundled().defaultOptions() or SweaterSpec.bundled().defaultOptions()
in place of removed no-argument option constructors. For a sleeve override use
the selected specification's optionsWithSleeveLength(double) factory. The
TeeOptions(sleeveLengthCm) and SweaterOptions(sleeveLengthCm) constructors are
the explicit domain API. The former three-argument TeeOptions constructor is
removed: migrate its gains to a complete TeeSpec supplied to PatternGenerator
or TeeCalculator(TeeSpec, StitchCalculator), not to options. Cuff compatibility
is checked against the selected sweater specification, not a hidden bundled one.
The programmatic PatternGenerationService() convenience still eagerly creates
both bundled generators. Main deliberately bypasses it to compose selected-only
dependencies; callers needing custom specs should use the typed generators or
inject generators into that service and parse options with the same selected spec.

Development and review
----------------------
Read AGENTS.md for bounded multi-agent task ownership and acceptance gates.
CI runs the Maven verification build, dependency review, and secret checks.
Tests characterize the legacy output, exercise calculations and invalid inputs,
and cover the application/CLI. A snapshot detects change; it cannot prove a
knitting formula is correct. Never silently regenerate a snapshot to hide a
failure. Approve numerical behavior changes separately from cleanup.

Remaining human gates
---------------------
1. Supply the other implementation and identify unique behavior before removal.
2. Have an experienced knitter approve fixtures and complete a knit-through.
3. Agree supported proportions, construction variants, and sizing tolerances.
4. Approve formula/rounding corrections before altering existing arithmetic.
5. Choose the agent platform and privileges; this repo does not provision one.
6. Review a real feature delivery before granting any additional autonomy.
