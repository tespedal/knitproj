package no.tuva.knitproj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path directory;

    @Test
    void legacyGainsWriteExactUtf8SnapshotAndExplainDoesNotChangePattern() throws IOException {
        Path input = directory.resolve("legacy.properties");
        Files.writeString(input, "armholeDepthGain=1.0\nshoulderWidthGain=0.8\n");
        Path output = directory.resolve("nested/pattern.txt");
        var result = run("--input", input.toString(), "--output", output.toString(), "--explain");
        assertEquals(0, result.status());
        assertEquals("", result.err());
        assertTrue(result.out().contains("Cast on neck"));
        assertTrue(result.out().contains("104"));
        try (var stream = getClass().getResourceAsStream("/default-pattern.txt")) {
            assertNotNull(stream);
            assertArrayEquals(stream.readAllBytes(), Files.readAllBytes(output));
        }
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
        assertTrue(result.out().contains("After armhole increases: body=172, shoulder/underarm per side=46, total=264, repeats=18"));
        String pattern = Files.readString(output);
        assertTrue(pattern.contains("18 ganger = 264 masker"));
        assertTrue(pattern.contains("3 ganger = 288 masker"));
        assertTrue(pattern.contains("62 masker"));
    }

    @Test
    void propertiesCustomizeGaugeFitAndSleeveLength() throws IOException {
        Path input = directory.resolve("request.properties");
        Path output = directory.resolve("custom.txt");
        Files.writeString(input, "stitchesPer10Cm=18\nrowsPer10Cm=24\nneedleSizeMm=4.5\n"
                + "fit=oversized\nsleeveLengthCm=12.5\narmholeDepthGain=1.0\nshoulderWidthGain=0.8\n");
        var result = run("--input", input.toString(), "--output", output.toString());
        assertEquals(0, result.status(), result.err());
        String pattern = Files.readString(output);
        assertTrue(pattern.contains("Legg opp 96 masker på rundpinne 4.5"));
        assertTrue(pattern.contains("12.5 cm"));
        assertTrue(pattern.contains("13 ganger = 204 masker"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--unknown", "--input", "--output"})
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
            "shoulderWidthGain=2", "armholeDepthGain=1.7976931348623157E308"})
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
    void uiUsesPredictableDefaultPortAndAllowsAnAlternate() {
        assertEquals(8080, Main.uiPort(new String[]{"--ui"}));
        assertEquals(8081, Main.uiPort(new String[]{"--ui", "--port", "8081"}));
        assertEquals(65535, Main.uiPort(new String[]{"--ui", "--port", "65535"}));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "65536", "abc", "8080.5"})
    void invalidUiPortsHaveActionableDiagnostics(String port) {
        var error = assertThrows(IllegalArgumentException.class,
                () -> Main.uiPort(new String[]{"--ui", "--port", port}));
        assertTrue(error.getMessage().contains("1 til 65535"));
    }

    @Test
    void uiRejectsMissingPortAndMixedGenerationFlags() {
        assertThrows(IllegalArgumentException.class, () -> Main.uiPort(new String[]{"--ui", "--port"}));
        assertThrows(IllegalArgumentException.class, () -> Main.uiPort(new String[]{"--ui", "--explain"}));
        assertThrows(IllegalArgumentException.class,
                () -> Main.uiPort(new String[]{"--ui", "--output", "pattern.txt"}));
    }

    @Test
    void helpSucceedsWithoutGeneratingAFile() {
        var result = run("--help");
        assertEquals(0, result.status());
        assertTrue(result.out().contains("--input"));
        assertTrue(result.out().contains("--output"));
        assertTrue(result.out().contains("--explain"));
        assertEquals("", result.err());
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
