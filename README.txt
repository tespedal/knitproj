KNITPROJ — DETERMINISTIC NORWEGIAN T-SHIRT PATTERNS

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
  java -jar target/knitproj-1.0-SNAPSHOT.jar --help
  java -jar target/knitproj-1.0-SNAPSHOT.jar --explain
  java -jar target/knitproj-1.0-SNAPSHOT.jar \
    --input examples/measurements.properties --output output/custom-pattern.txt

Use mvnw.cmd on Windows. The Maven wrapper pins the build-tool version.
The default output is output/tshirt-pattern.txt, relative to the working directory.
--output is an explicit user-selected path; existing files are overwritten.
Do not select the input file as the output path if you want to retain it.
--explain prints stage counts for review; the pattern itself remains Norwegian.
Exit codes: 0 success/help, 1 file I/O error, 2 invalid request or arguments.

Input
-----
Input is a UTF-8 Java properties file, not executable configuration. Omitted
properties use the legacy defaults. Unknown property names are rejected.
Decimal values use a dot; gauge stitch/row counts must be whole numbers.
All lengths and body measurements use centimetres, needle size uses millimetres,
and gauge counts refer to 10 cm. See examples/measurements.properties for keys.
Input validation rejects invalid numbers and some incompatible geometry; passing
validation does not establish that a garment fits or is knit-ready.

Architecture
------------
Production sources remain under src/no/tuva/knitproj; tests are under src/test.
No framework, runtime AI service, or distributed infrastructure is required.

  domain          Validated measurements, gauge, fit, and sleeve options
  pattern/tee     Legacy deterministic calculations, stages, Norwegian template
  application     PatternGenerator: request + options -> counts + pattern
  cli             UTF-8 properties adapter
  service         Ease/conversion, plain-text formatter, filesystem adapter
  Main            CLI orchestration and process exit status

PatternGenerator has no filesystem dependency. TeeCalculationResults exposes
structured stage counts and a human-readable trace. Templates consume computed
counts instead of deciding shaping arithmetic. Formatting is separate from file
writing. Sleeve length is configurable and defaults to the original 10 cm.

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
