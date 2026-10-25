package no.tuva.knitproj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import no.tuva.knitproj.domain.PatternType;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.cli.Utf8Properties;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.service.StitchCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path directory;

    @Test
    void explicitGainsWriteCalculatedUtf8PatternAndExplainDoesNotChangePattern() throws IOException {
        Path input = directory.resolve("legacy.properties");
        Files.writeString(input, "");
        Path spec = historicalSpec();
        Path output = directory.resolve("nested/pattern.txt");
        var result = run("--input", input.toString(), "--spec", spec.toString(), "--output", output.toString(), "--explain");
        assertEquals(0, result.status());
        assertEquals("", result.err());
        assertTrue(result.out().contains("Cast on neck"));
        assertTrue(result.out().contains("104"));
        var selected = TeeSpec.fromProperties(Utf8Properties.read(spec));
        var configuration = PatternInput.requestFromProperties(Utf8Properties.read(input), selected);
        var generated = new no.tuva.knitproj.application.PatternGenerator(selected)
                .generate(configuration.request(), configuration.options());
        assertEquals(new no.tuva.knitproj.service.PatternTextFormatter().format(generated.pattern()),
                Files.readString(output));
        byte[] explained = Files.readAllBytes(output);
        assertEquals(0, run("--input", input.toString(), "--spec", spec.toString(), "--output", output.toString()).status());
        assertArrayEquals(explained, Files.readAllBytes(output));
        Files.writeString(output, "stale content");
        assertEquals(0, run("--output", output.toString()).status());
        assertTrue(Files.readString(output).startsWith("TILPASSET T-SKJORTEOPPSKRIFT\n"));
        assertFalse(Files.readString(output).contains("stale content"));
    }

    @Test
    void defaultGainsReachRenderedPatternAndExplain() throws IOException {
        Path output = directory.resolve("default.txt");
        var result = run("--output", output.toString(), "--explain");
        assertEquals(0, result.status(), result.err());
        var configuration = PatternInput.defaults();
        var counts = new no.tuva.knitproj.application.PatternGenerator()
                .generate(configuration.request(), configuration.options()).calculations();
        assertTrue(result.out().contains("after shoulder shaping)=" + counts.armholeShapingRows()));
        assertEquals(new StitchCalculator().calculateRows(
                configuration.request().getMeasurements().armholeDepthCm() * TeeSpec.bundled().armholeDepthGain(),
                configuration.request().getGauge().getStitchesPer10CmHeight()), counts.armholeShapingRows());
        String pattern = Files.readString(output);
        assertTrue(pattern.contains(counts.afterArmInc().nrIncreases() + " ganger = "
                + counts.afterArmInc().totalStitches() + " masker"));
        assertTrue(pattern.contains(counts.afterRaglan().nrIncreases() + " ganger = "
                + counts.afterRaglan().totalStitches() + " masker"));
        assertTrue(pattern.contains(counts.sleeveStitches() + " masker"));
    }

    @Test
    void propertiesCustomizeGaugeFitAndSleeveLength() throws IOException {
        Path input = directory.resolve("request.properties");
        Path output = directory.resolve("custom.txt");
        Files.writeString(input, "stitchesPer10Cm=18\nrowsPer10Cm=24\nneedleSizeMm=4.5\n"
                + "fit=oversized\nsleeveLengthCm=12.5\n");
        Path spec = historicalSpec();
        var result = run("--input", input.toString(), "--spec", spec.toString(), "--output", output.toString());
        assertEquals(0, result.status(), result.err());
        String pattern = Files.readString(output);
        assertTrue(pattern.contains("Legg opp 96 masker på rundpinne 4.5"));
        assertTrue(pattern.contains("12.5 cm"));
        var selected = TeeSpec.fromProperties(Utf8Properties.read(spec));
        var configuration = PatternInput.requestFromProperties(Utf8Properties.read(input), selected);
        var counts = new no.tuva.knitproj.application.PatternGenerator(selected)
                .generate(configuration.request(), configuration.options()).calculations();
        assertEquals(50, counts.armholeShapingRows());
        assertTrue(pattern.contains(counts.afterArmInc().nrIncreases() + " ganger = "
                + counts.afterArmInc().totalStitches() + " masker"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--unknown", "--input", "--output", "--spec"})
    void malformedArgumentsHaveNonzeroStatusAndActionableDiagnostics(String flag) {
        var result = run(flag);
        assertEquals(2, result.status());
        assertEquals("", result.out());
        assertFalse(result.err().isBlank());
    }

    @Test
    void invalidInputDoesNotCreateOutput() throws IOException {
        Path input = directory.resolve("invalid.properties");
        Path output = directory.resolve("should-not-exist.txt");
        Files.writeString(input, "neckCircumferenceCm=-1\n");
        var result = run("--input", input.toString(), "--output", output.toString());
        assertEquals(2, result.status());
        assertFalse(result.err().isBlank());
        assertFalse(Files.exists(output));
    }

    @ParameterizedTest
    @ValueSource(strings = {"armholeDepthGain=NaN", "shoulderWidthGain=0",
            "shoulderWidthGain=2", "armholeDepthGain=1.7976931348623157E308",
            "armholeDepthCm=0.01"})
    void invalidGainsDoNotOverwriteExistingOutput(String property) throws IOException {
        Path input = directory.resolve("invalid-gain.properties");
        Path output = directory.resolve("retained.txt");
        Files.writeString(input, property);
        Files.writeString(output, "existing pattern");
        var result = run("--input", input.toString(), "--output", output.toString());
        assertEquals(2, result.status());
        assertFalse(result.err().isBlank());
        assertEquals("existing pattern", Files.readString(output));
    }

    @Test
    void missingInputAndUnwritableOutputReturnIoStatus() {
        assertEquals(1, run("--input", directory.resolve("absent.properties").toString()).status());
        var result = run("--output", directory.toString());
        assertEquals(1, result.status());
        assertFalse(result.err().isBlank());
    }

    @Test
    void helpSucceedsWithoutGeneratingAFile() {
        var result = run("--help");
        assertEquals(0, result.status());
        assertTrue(result.out().contains("--input"));
        assertTrue(result.out().contains("--output"));
        assertTrue(result.out().contains("--explain"));
        assertTrue(result.out().contains("--pattern"));
        assertTrue(result.out().contains("overzided_raglan"));
        assertEquals("", result.err());
    }

    @Test
    void selectionDefaultsAndDedicatedFilenamesPreserveTee() {
        assertEquals(PatternType.OVERSIZED_RAGLAN, PatternType.parse("oversized_raglan"));
        assertEquals(PatternType.OVERSIZED_RAGLAN, PatternType.parse("overzided_raglan"));
        assertEquals(PatternType.TSHIRT, PatternType.parse("tshirt"));
        assertEquals("tshirt-pattern.txt", PatternType.TSHIRT.defaultFilename());
        assertEquals("oversized-raglan-pattern.txt", PatternType.OVERSIZED_RAGLAN.defaultFilename());
        assertEquals(TeeSpec.bundled().sleeveLengthCm(), PatternInput.defaults().options().sleeveLengthCm());
        assertEquals(SweaterSpec.bundled().sleeveLengthCm(),
                PatternInput.defaults(PatternType.OVERSIZED_RAGLAN).options().sleeveLengthCm());
    }

    @ParameterizedTest
    @ValueSource(strings = {"oversized_raglan", "overzided_raglan"})
    void sweaterSelectorsWriteCompleteNorwegianPatternAndExplain(String selector) throws IOException {
        Path output = directory.resolve(selector + ".txt");
        Path spec = directory.resolve("raglan.properties");
        Files.writeString(spec, "neckFactor=1.3\nbustFactor=1.5\ndepthFactor=1.4\n"
                + "neckRepeats=4\nneckbandCm=2\nhemCm=4\ncuffCm=4\nsleeveLengthCm=45\n");
        var result = run("--pattern", selector, "--spec", spec.toString(),
                "--output", output.toString(), "--explain");
        assertEquals(0, result.status(), result.err());
        assertEquals("", result.err());
        assertTrue(result.out().contains("raglan=41; raglan rows=82"));
        assertTrue(result.out().contains("front=138, back=138, sleeve each=92, body=276, total=460"));
        String pattern = Files.readString(output);
        assertTrue(pattern.startsWith("OVERSIZED RAGLANGENSER"));
        assertTrue(pattern.contains("Bolen har nå 296 masker"));
        assertTrue(pattern.contains("45.0 cm fra underarmen"));
        assertTrue(pattern.contains("MONTERING"));
        assertFalse(pattern.contains("TILPASSET T-SKJORTEOPPSKRIFT"));
    }

    @Test
    void explicitTshirtEqualsDefault() throws IOException {
        Path defaultOutput = directory.resolve("default.txt");
        Path selectedOutput = directory.resolve("selected.txt");
        assertEquals(0, run("--output", defaultOutput.toString()).status());
        assertEquals(0, run("--pattern", "tshirt", "--output", selectedOutput.toString()).status());
        assertArrayEquals(Files.readAllBytes(defaultOutput), Files.readAllBytes(selectedOutput));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sleeveLengthCm=4", "bustCircumferenceCm=60", "bustCircumferenceCm=1000",
            "unknownProperty=1", "armholeDepthGain=NaN"})
    void invalidSweaterInputDoesNotOverwriteOutput(String property) throws IOException {
        Path input = directory.resolve("invalid-sweater.properties");
        Path output = directory.resolve("existing-sweater.txt");
        Files.writeString(input, property);
        Files.writeString(output, "keep");
        var result = run("--pattern", "oversized_raglan", "--input", input.toString(), "--output", output.toString());
        assertEquals(2, result.status());
        assertFalse(result.err().isBlank());
        assertEquals("keep", Files.readString(output));
    }

    @Test
    void invalidOrMissingSelectorHasActionableErrorWithoutWriting() {
        var invalid = run("--pattern", "raglan");
        assertEquals(2, invalid.status());
        assertTrue(invalid.err().contains("Choose tshirt or oversized_raglan"));
        assertEquals(2, run("--pattern").status());
        assertEquals(2, run("--pattern", "--output").status());
    }

    @Test
    void nullArgumentsReturnInvalidRequestInsteadOfThrowing() {
        var result = run((String[]) null);
        assertEquals(2, result.status());
        assertTrue(result.err().contains("Arguments are required"));
        assertEquals("", result.out());
    }

    @Test
    void invalidArgumentsDoNotCreateOutputParents() {
        Path output = directory.resolve("not-created/pattern.txt");
        var result = run("--output", output.toString(), "--pattern", "unknown");
        assertEquals(2, result.status());
        assertFalse(Files.exists(output.getParent()));
    }

    @Test
    void helpDoesNotReadInputOrWriteOutput() {
        Path output = directory.resolve("not-created/pattern.txt");
        var result = run("--input", directory.resolve("missing.properties").toString(),
                "--spec", directory.resolve("missing-spec.properties").toString(),
                "--output", output.toString(), "--help", "--unknown");
        assertEquals(0, result.status());
        assertEquals("", result.err());
        assertFalse(Files.exists(output.getParent()));
    }

    @Test
    void actualDefaultOutputPathsAreSeparateForBothAliasesAndDefaultTee() throws Exception {
        launchInDirectory();
        Path tee = directory.resolve("output/tshirt-pattern.txt");
        byte[] teeBytes = Files.readAllBytes(tee);
        assertTrue(Files.readString(tee).startsWith("TILPASSET T-SKJORTEOPPSKRIFT"));
        launchInDirectory("--pattern", "oversized_raglan");
        Path sweater = directory.resolve("output/oversized-raglan-pattern.txt");
        byte[] sweaterBytes = Files.readAllBytes(sweater);
        assertTrue(Files.readString(sweater).startsWith("OVERSIZED RAGLANGENSER"));
        assertArrayEquals(teeBytes, Files.readAllBytes(tee));
        launchInDirectory("--pattern", "overzided_raglan");
        assertArrayEquals(sweaterBytes, Files.readAllBytes(sweater));
        assertArrayEquals(teeBytes, Files.readAllBytes(tee));
    }

    private void launchInDirectory(String... args) throws Exception {
        var command = new java.util.ArrayList<>(java.util.List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", Path.of("target/classes").toAbsolutePath().toString(), Main.class.getName()));
        command.addAll(java.util.List.of(args));
        Process process = new ProcessBuilder(command).directory(directory.toFile()).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "CLI subprocess timed out");
            assertEquals(0, process.exitValue(), new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor();
            }
        }
    }

    private Path historicalSpec() throws IOException {
        Path spec = directory.resolve("tshirt.properties");
        Files.writeString(spec, "normal.neckFactor=1.3\noversized.neckFactor=1.5\n"
                + "normal.bustFactor=1\noversized.bustFactor=1.1\n"
                + "armholeDepthGain=1\nshoulderWidthGain=0.8\nsleeveLengthCm=10\n");
        return spec;
    }

    private static Invocation run(String... args) {
        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        try (var out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
             var err = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            int status = Main.run(args, out, err);
            return new Invocation(status, stdout.toString(StandardCharsets.UTF_8),
                    stderr.toString(StandardCharsets.UTF_8));
        }
    }

    private record Invocation(int status, String out, String err) {}
}
