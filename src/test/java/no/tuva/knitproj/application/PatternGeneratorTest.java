package no.tuva.knitproj.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.Measurements;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternGeneratorTest {
    private final PatternGenerator generator = new PatternGenerator();
    private final PatternRequest request = new PatternRequest(
            new Measurements(), new Gauge(22, 28, 5), FitPreference.NORMAL);
    private final PatternTextFormatter formatter = new PatternTextFormatter();

    @Test
    void legacyGainsMatchCapturedOutput() throws IOException {
        GeneratedPattern generated = generator.generate(request, new TeeOptions(10, 1.0, 0.8));
        try (var stream = getClass().getResourceAsStream("/default-pattern.txt")) {
            assertNotNull(stream);
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8),
                    formatter.format(generated.pattern()));
        }
        assertEquals(41, generated.calculations().backLength());
        assertEquals(generator.generate(request).calculations(),
                generator.generate(request, new TeeOptions()).calculations());
    }

    @Test
    void customSleeveLengthChangesOnlySleeveTextNotCalculations() {
        GeneratedPattern standard = generator.generate(request);
        GeneratedPattern custom = generator.generate(request, new TeeOptions(12.5));
        assertEquals(standard.calculations(), custom.calculations());
        assertEquals(formatter.format(standard.pattern()).replace("ermet måler 10 cm", "ermet måler 12.5 cm"),
                formatter.format(custom.pattern()));
    }

    @Test
    void repeatedGenerationIsDeterministic() {
        GeneratedPattern first = generator.generate(request);
        GeneratedPattern second = generator.generate(request);
        assertEquals(first.calculations(), second.calculations());
        assertEquals(formatter.format(first.pattern()), formatter.format(second.pattern()));
    }

    @Test
    void applicationRejectsMissingRequestOrOptions() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(request, null));
    }
}
