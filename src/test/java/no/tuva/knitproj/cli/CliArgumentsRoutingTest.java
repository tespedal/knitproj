package no.tuva.knitproj.cli;

import java.nio.file.Path;
import no.tuva.knitproj.domain.PatternType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CliArgumentsRoutingTest {
    @Test
    void noArgumentsSelectTeeAndItsDefaultOutput() {
        var arguments = CliArguments.parse(new String[0]);
        assertEquals(PatternType.TSHIRT, arguments.type());
        assertEquals(Path.of("output", "tshirt-pattern.txt"), arguments.output());
        assertNull(arguments.input());
        assertNull(arguments.spec());
        assertFalse(arguments.explain());
        assertFalse(arguments.help());
    }

    @ParameterizedTest
    @ValueSource(strings = {"oversized_raglan", "overzided_raglan"})
    void sweaterAliasesSelectDedicatedDefault(String selector) {
        var arguments = CliArguments.parse(new String[] {"--pattern", selector});
        assertEquals(PatternType.OVERSIZED_RAGLAN, arguments.type());
        assertEquals(Path.of("output", "oversized-raglan-pattern.txt"), arguments.output());
    }

    @Test
    void explicitPathsAndLastRepeatedValuesArePreserved() {
        var arguments = CliArguments.parse(new String[] {"--pattern", "oversized_raglan",
                "--output", "first.txt", "--pattern", "tshirt", "--output", "chosen.txt",
                "--input", "measurements.properties", "--spec", "first.properties",
                "--spec", "selected.properties", "--explain"});
        assertEquals(PatternType.TSHIRT, arguments.type());
        assertEquals(Path.of("chosen.txt"), arguments.output());
        assertEquals(Path.of("measurements.properties"), arguments.input());
        assertEquals(Path.of("selected.properties"), arguments.spec());
        assertTrue(arguments.explain());
    }

    @Test
    void helpShortCircuitsOnlyArgumentsAfterIt() {
        assertTrue(CliArguments.parse(new String[] {"--help", "--unknown"}).help());
        assertThrows(IllegalArgumentException.class,
                () -> CliArguments.parse(new String[] {"--unknown", "--help"}));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--output", "--pattern", "--spec"})
    void missingValuesReportTheirFlag(String flag) {
        var missing = assertThrows(IllegalArgumentException.class,
                () -> CliArguments.parse(new String[] {flag}));
        assertTrue(missing.getMessage().contains(flag));
        assertThrows(IllegalArgumentException.class,
                () -> CliArguments.parse(new String[] {flag, "--explain"}));
        assertThrows(IllegalArgumentException.class,
                () -> CliArguments.parse(new String[] {flag, null}));
    }

    @Test
    void nullAndUnknownInputsAreActionable() {
        assertThrows(IllegalArgumentException.class, () -> CliArguments.parse(null));
        assertThrows(IllegalArgumentException.class, () -> CliArguments.parse(new String[] {null}));
        assertThrows(IllegalArgumentException.class,
                () -> CliArguments.parse(new String[] {"--pattern", "raglan"}));
        assertThrows(IllegalArgumentException.class,
                () -> new CliArguments(null, null, null, null, false, false));
    }
}
