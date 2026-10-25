package no.tuva.knitproj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.application.RaglanPatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeSpecificationTest {
    @TempDir
    Path directory;

    private static final String TEE = """
            # Komplett konstruksjon – ikke personmål.
            normal.neckFactor=1.3
            oversized.neckFactor=1.5
            normal.bustFactor=1.1
            oversized.bustFactor=1.3
            armholeDepthGain=1.2
            shoulderWidthGain=0.85
            sleeveLengthCm=14
            """;
    private static final String RAGLAN = """
            neckFactor=1.3
            bustFactor=1.6
            depthFactor=1.2
            neckRepeats=4
            neckbandCm=3
            hemCm=5
            cuffCm=2
            sleeveLengthCm=3
            """;

    @Test
    void editingExternalSpecBetweenMainCallsChangesOnlyDepthDrivenCountsWithoutRebuild() throws IOException {
        var specPath = write("tee.properties", TEE);
        var output = directory.resolve("tee.txt");
        var first = invoke("--spec", specPath.toString(), "--output", output.toString(), "--explain");
        assertEquals(0, first.status(), first.err());
        var spec = teeSpec(1.2);
        var input = PatternInput.requestFromProperties(new Properties(), spec);
        var generated = new PatternGenerator(spec).generate(input.request(), input.options());
        var counts = generated.calculations();
        assertEquals(72, counts.armholeShapingRows());
        assertEquals(28, counts.afterArmInc().nrIncreases());
        assertEquals(224, counts.afterArmHole().totalStitches());
        assertEquals(92, counts.sleeveStitches());
        assertEquals(new PatternTextFormatter().format(generated.pattern()), Files.readString(output));
        assertTrue(first.out().contains("external " + specPath));
        assertTrue(first.out().contains("armholeDepthGain=1.2"));
        assertTrue(Files.readString(output).contains("ermet måler 14 cm"));
        byte[] original = Files.readAllBytes(output);
        assertEquals(0, invoke("--spec", specPath.toString(), "--output", output.toString()).status());
        assertArrayEquals(original, Files.readAllBytes(output), "--explain must be byte neutral");

        Files.writeString(specPath, TEE.replace("armholeDepthGain=1.2", "armholeDepthGain=1.4"));
        var second = invoke("--spec", specPath.toString(), "--output", output.toString(), "--explain");
        assertEquals(0, second.status(), second.err());
        var deeper = new PatternGenerator(teeSpec(1.4)).generate(input.request()).calculations();
        // E(21 * 1.4, 28) = 82; 82/2 - 8 raglan repeats = 33 sleeve-only repeats.
        assertEquals(82, deeper.armholeShapingRows());
        assertEquals(33, deeper.afterArmInc().nrIncreases());
        assertEquals(counts.afterRaglan().nrIncreases(), deeper.afterRaglan().nrIncreases());
        assertEquals(counts.afterArmHole(), deeper.afterArmHole());
        assertEquals(counts.sleeveStitches() + 10, deeper.sleeveStitches());
        assertNotEquals(new String(original, StandardCharsets.UTF_8), Files.readString(output));
        assertTrue(second.out().contains("armholeDepthGain=1.4"));
        assertTrue(second.out().contains(deeper.explain()));
    }

    @Test
    void selectedSpecSuppliesBothDefaultsAndWordingAndOnlyLengthCanBeOverridden() throws IOException {
        var path = write("raglan.properties", RAGLAN);
        var output = directory.resolve("raglan.txt");
        var result = invoke("--pattern", "overzided_raglan", "--spec", path.toString(),
                "--output", output.toString(), "--explain");
        assertEquals(0, result.status(), result.err());
        var spec = new SweaterSpec(1.3, 1.6, 1.2, 4, 3, 5, 2, 3);
        var input = PatternInput.requestFromProperties(new Properties(), spec);
        assertEquals(spec.defaultOptions(), input.options());
        var expected = new RaglanPatternGenerator(spec).generate(input.request(), input.options());
        assertEquals(new PatternTextFormatter().format(expected.pattern()), Files.readString(output));
        assertTrue(result.out().contains("bustFactor=1.6, depthFactor=1.2"));
        assertTrue(Files.readString(output).contains("bryst × 1,6"));
        var lengths = new Properties();
        lengths.setProperty("sleeveLengthCm", "8");
        var overridden = PatternInput.requestFromProperties(lengths, spec);
        var custom = new RaglanPatternGenerator(spec).generate(overridden.request(), overridden.options());
        assertEquals(expected.calculations().afterRaglan(), custom.calculations().afterRaglan());
        assertEquals(expected.calculations().bodyStitches(), custom.calculations().bodyStitches());
        assertEquals(6, custom.calculations().sleeveStraightCm());

        var teeSpec = teeSpec(1.4);
        var teeDefault = PatternInput.requestFromProperties(new Properties(), teeSpec);
        var teeLength = PatternInput.requestFromProperties(lengths, teeSpec);
        assertEquals(14, teeDefault.options().sleeveLengthCm());
        var generator = new PatternGenerator(teeSpec);
        var defaultTee = generator.generate(teeDefault.request(), teeDefault.options());
        var customTee = generator.generate(teeLength.request(), teeLength.options());
        assertEquals(defaultTee.calculations(), customTee.calculations());
        assertEquals(new PatternTextFormatter().format(defaultTee.pattern()).replace("ermet måler 14 cm",
                "ermet måler 8 cm"), new PatternTextFormatter().format(customTee.pattern()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"armholeDepthGain", "shoulderWidthGain", "normal.neckFactor", "normal.bustFactor",
            "oversized.neckFactor", "oversized.bustFactor", "neckFactor", "bustFactor", "depthFactor",
            "neckRepeats", "neckbandCm", "hemCm", "cuffCm"})
    void measurementScalingOverridesAreRejectedByAllAdaptersAndDoNotOverwriteOutput(String key) throws IOException {
        var properties = new Properties();
        properties.setProperty(key, "1.2");
        for (var type : no.tuva.knitproj.domain.PatternType.values()) {
            var error = assertThrows(IllegalArgumentException.class,
                    () -> PatternInput.requestFromProperties(properties, type));
            assertTrue(error.getMessage().contains("--spec"));
            assertThrows(IllegalArgumentException.class, () -> PatternInput.fromProperties(properties, type));
        }
        assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(properties, teeSpec(1.2)));
        var input = write("measurements.properties", key + "=1.2");
        var output = write("existing.txt", "keep");
        var result = invoke("--input", input.toString(), "--output", output.toString());
        assertEquals(2, result.status());
        assertTrue(result.err().contains(key));
        assertTrue(result.err().contains("--spec"));
        assertEquals("keep", Files.readString(output));
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "unknown", "number", "nonpositive", "wrong-pattern", "partial"})
    void incompleteInvalidAndWrongPatternSpecsFailBeforeWriting(String failure) throws IOException {
        String contents = switch (failure) {
            case "missing" -> TEE.replace("normal.neckFactor=1.3\n", "");
            case "unknown" -> TEE + "typo=1\n";
            case "number" -> TEE.replace("armholeDepthGain=1.2", "armholeDepthGain=NaN");
            case "nonpositive" -> TEE.replace("shoulderWidthGain=0.85", "shoulderWidthGain=0");
            case "wrong-pattern" -> RAGLAN;
            default -> "armholeDepthGain=1.2\n";
        };
        var spec = write("invalid.properties", contents);
        var output = write("existing.txt", "keep");
        var result = invoke("--spec", spec.toString(), "--output", output.toString());
        assertEquals(2, result.status(), result.err());
        assertTrue(result.err().contains("specification"));
        assertEquals("keep", Files.readString(output));
        var absent = directory.resolve("not-created/pattern.txt");
        assertEquals(2, invoke("--spec", spec.toString(), "--output", absent.toString()).status());
        assertFalse(Files.exists(absent.getParent()));
    }

    @Test
    void missingUnreadableAndWrongSchemaPathsHaveCorrectStatusesAndHelpDoesNoIo() throws IOException {
        var output = write("existing.txt", "keep");
        var missing = directory.resolve("missing.properties").toString();
        assertEquals(1, invoke("--spec", missing, "--output", output.toString()).status());
        assertEquals(1, invoke("--spec", directory.toString(), "--output", output.toString()).status());
        assertEquals(2, invoke("--spec").status());
        assertEquals(2, invoke("--spec", "--output", output.toString()).status());
        var tee = write("tee.properties", TEE);
        assertEquals(2, invoke("--pattern", "oversized_raglan", "--spec", tee.toString(),
                "--output", output.toString()).status());
        var help = invoke("--spec", missing, "--input", missing, "--output", output.toString(), "--help");
        assertEquals(0, help.status());
        assertTrue(help.out().contains("without rebuilding"));
        assertEquals("keep", Files.readString(output));
        var bundled = invoke("--output", directory.resolve("bundled.txt").toString(), "--explain");
        assertEquals(0, bundled.status(), bundled.err());
        assertTrue(bundled.out().contains("Specification: bundled TSHIRT"));
    }

    @Test
    void mainNeverLoadsUnselectedOrReplacedConstructionResources() throws Exception {
        var externalTee = write("tee.properties", TEE);
        var externalRaglan = write("raglan.properties", RAGLAN);
        for (String type : new String[]{"tshirt", "oversized_raglan"}) {
            for (boolean external : new boolean[]{false, true}) {
                String selectedResource = type.equals("tshirt") ? "tshirt.properties" : "oversized-raglan.properties";
                var specReads = new java.util.concurrent.atomic.AtomicInteger();
                try (var loader = new URLClassLoader(new URL[]{Path.of("target/classes").toUri().toURL()},
                        ClassLoader.getPlatformClassLoader()) {
                    @Override
                    public URL getResource(String name) {
                        if (name.startsWith("patterns/") && !name.equals("patterns/text-no.properties")
                                && (external || !name.equals("patterns/" + selectedResource))) {
                            throw new AssertionError("Unselected resource read: " + name);
                        }
                        if (name.equals("patterns/" + selectedResource)) {
                            specReads.incrementAndGet();
                        }
                        return super.getResource(name);
                    }
                }) {
                    var args = new java.util.ArrayList<>(java.util.List.of("--pattern", type, "--output",
                            directory.resolve(type + external + ".txt").toString()));
                    if (external) {
                        args.addAll(java.util.List.of("--spec",
                                (type.equals("tshirt") ? externalTee : externalRaglan).toString()));
                    }
                    var bytes = new ByteArrayOutputStream();
                    try (var stream = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
                        var main = loader.loadClass(Main.class.getName());
                        var run = main.getMethod("run", String[].class, PrintStream.class, PrintStream.class);
                        var help = run.invoke(null, new String[]{"--spec", "missing.properties",
                                "--input", "missing.properties", "--help"}, stream, stream);
                        assertEquals(0, help);
                        assertEquals(0, specReads.get(), "Help must not read construction resources");
                        var status = run.invoke(null, args.toArray(String[]::new), stream, stream);
                        assertEquals(0, status, bytes.toString(StandardCharsets.UTF_8));
                        assertEquals(external ? 0 : 1, specReads.get(),
                                "Load selected bundled spec exactly once, never for external selection");
                    }
                }
            }
        }
    }

    private TeeSpec teeSpec(double depth) {
        return new TeeSpec(1.3, 1.5, 1.1, 1.3, depth, 0.85, 14);
    }

    private Path write(String name, String contents) throws IOException {
        var path = directory.resolve(name);
        return Files.writeString(path, contents, StandardCharsets.UTF_8);
    }

    private Invocation invoke(String... args) {
        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        try (var out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
             var err = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            int status = Main.run(args, out, err);
            return new Invocation(status, stdout.toString(StandardCharsets.UTF_8), stderr.toString(StandardCharsets.UTF_8));
        }
    }

    private record Invocation(int status, String out, String err) {}
}
