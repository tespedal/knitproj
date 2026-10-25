# Testing and verification

Use Java 17 and the Maven wrapper from the repository root. The authoritative
whole-project check is [`./mvnw verify`](../../README.txt); it compiles,
executes tests, and packages the CLI. On Windows use `mvnw.cmd verify`.

## Focused calculations and CLI checks

Run the calculation/conversion regressions and CLI tests together:

```sh
./mvnw -Dtest=CalculationServicesTest,TeeCalculatorTest,TeeArmholeBudgetTest,RaglanCalculatorTest,MainTest,RuntimeSpecificationTest test
```

These tests cover shared rounding, historical and current T-shirt numerical
cases, the sweater's synthetic reference case, validation boundaries, typed
selection, and CLI output behavior. The source tests are
[`CalculationServicesTest`](../../src/test/java/no/tuva/knitproj/service/CalculationServicesTest.java),
[`TeeCalculatorTest`](../../src/test/java/no/tuva/knitproj/pattern/tee/TeeCalculatorTest.java),
[`RaglanCalculatorTest`](../../src/test/java/no/tuva/knitproj/pattern/raglan/RaglanCalculatorTest.java),
[`MainTest`](../../src/test/java/no/tuva/knitproj/MainTest.java), and
[`RuntimeSpecificationTest`](../../src/test/java/no/tuva/knitproj/RuntimeSpecificationTest.java).

Runtime-spec regressions edit the same external UTF-8 file between two Main calls
without rebuilding; pin exact gained-depth/sleeve counts while preserving bust;
check selected defaults, sweater factor/finish wording and sleeve-only overrides;
reject input scaling through typed and legacy adapters; exercise missing,
unreadable, partial, invalid and wrong-pattern specs before output writes;
verify help ignores missing files and explain is pattern-byte neutral.
An isolated classloader rejects any unselected/replaced construction resource
read, covering both bundled and external CLI selection paths.
Numerical cases and both historical snapshots use explicit specs; general bundled
default tests compare against the selected spec rather than pinning editable gains.

After `./mvnw verify` has produced the executable JAR, the following packaged
CLI smoke checks use the program's built-in synthetic defaults. They do not read
or create a measurement input file; each explicitly selected output is under
`target/`, not the user's output directory:

```sh
java -jar target/knitproj-1.0.0-SNAPSHOT.jar \
  --output target/wiki-default-tee.txt --explain
java -jar target/knitproj-1.0.0-SNAPSHOT.jar \
  --pattern oversized_raglan --output target/wiki-default-raglan.txt --explain
java -jar target/knitproj-1.0.0-SNAPSHOT.jar \
  --spec examples/tshirt.properties --output target/wiki-external-tee.txt --explain
```

`--explain` prints spec source, effective scaling and stage counts while the Norwegian pattern
is written to the selected file. The T-shirt is the default selection; the
sweater command is explicit. These commands overwrite those exact `target/`
files if already present. CLI behavior and exit statuses are implemented in
[`Main`](../../src/no/tuva/knitproj/Main.java), argument parsing in
[`CliArguments`](../../src/no/tuva/knitproj/cli/CliArguments.java), and defaults
in [`PatternInput`](../../src/no/tuva/knitproj/cli/PatternInput.java).

## Interpreting evidence

- Exit code `0`: success or `--help`.
- Exit code `1`: input/spec/output file I/O error.
- Exit code `2`: invalid request, spec schema/values or command-line arguments
  (including a missing path after `--spec`).

An invalid calculation is expected to fail before the pattern file is written;
tests cover that boundary. A golden/snapshot comparison detects an output
change, not a wrong or right knitting formula. The retained T-shirt and sweater
snapshots are
[`default-pattern.txt`](../../src/test/resources/default-pattern.txt) and
[`default-raglan-pattern.txt`](../../src/test/resources/default-raglan-pattern.txt);
the historical T-shirt snapshot uses a complete explicit historical spec,
including combined-depth gain `40 / (21 * 2.8)`, rather than input/option overrides.
Never regenerate a golden file just to silence a failure. Formula, fit,
rounding, or supported-size changes need separate human knitting approval and
reviewed numerical examples, as detailed in
[calculations](calculations.md) and [open questions](open-questions.md).

## Fixture boundaries

The numerical references in these pages are synthetic defaults, not personal
measurements. Do not turn locally edited examples or generated output into wiki
fixtures. Keep new regression fixtures synthetic and source their expected
values from reviewed calculation cases. For the repository's setup and
limitations see [`README.txt`](../../README.txt); the wiki's
[index](index.md), [architecture](architecture.md),
[configuration](configuration.md), and [extension guide](extending-patterns.md)
are planned crosslinks.
