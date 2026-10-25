package no.tuva.knitproj.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.Measurements;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternGenerationRoutingTest {
    private final PatternRequest request = new PatternRequest(
            new Measurements(), new Gauge(22, 28, 5), FitPreference.NORMAL);
    private final PatternGenerationService service = new PatternGenerationService();
    private final PatternTextFormatter formatter = new PatternTextFormatter();

    @Test
    void applicationGeneratesBothDocumentsInMemoryWithOriginalTraces() {
        var teeOptions = TeeSpec.bundled().defaultOptions();
        var tee = service.generate(new GenerationRequest.Tee(request, teeOptions));
        var originalTee = new PatternGenerator().generate(request, teeOptions);
        assertEquals(formatter.format(originalTee.pattern()), formatter.format(tee.pattern()));
        assertEquals(originalTee.calculations().explain(), tee.trace());

        var sweaterOptions = SweaterSpec.bundled().defaultOptions();
        var sweater = service.generate(new GenerationRequest.Raglan(request, sweaterOptions));
        var originalSweater = new RaglanPatternGenerator().generate(request, sweaterOptions);
        assertEquals(formatter.format(originalSweater.pattern()), formatter.format(sweater.pattern()));
        assertEquals(originalSweater.calculations().explain(), sweater.trace());
    }

    @Test
    void routedDocumentsPreserveBothNorwegianSnapshots() throws IOException {
        var historical = new PatternGenerationService(
                new PatternGenerator(new TeeSpec(1.3, 1.5, 1, 1.1, 40.0 / (21 * 2.8), 0.8, 10)),
                new RaglanPatternGenerator(new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 4, 45)));
        assertSnapshot("default-pattern.txt", historical.generate(
                new GenerationRequest.Tee(request, new TeeOptions(10))));
        assertSnapshot("default-raglan-pattern.txt", historical.generate(
                new GenerationRequest.Raglan(request, new SweaterOptions(45))));
    }

    @Test
    void sweaterUsesItsSelectedSpecificationRatherThanBundledCuff() {
        var bundled = SweaterSpec.bundled();
        var custom = new SweaterSpec(bundled.neckFactor(), bundled.bustFactor(), bundled.depthFactor(),
                bundled.neckRepeats(), bundled.neckbandCm(), bundled.hemCm(), 1, 3);
        var selected = new PatternGenerationService(new PatternGenerator(), new RaglanPatternGenerator(custom));
        var result = selected.generate(new GenerationRequest.Raglan(request, custom.defaultOptions()));
        var expected = new RaglanPatternGenerator(custom).generate(request);
        assertEquals(formatter.format(expected.pattern()), formatter.format(result.pattern()));
        assertThrows(IllegalArgumentException.class, () -> selected.generate(
                new GenerationRequest.Raglan(request, new SweaterOptions(1))));
    }

    @Test
    void nullRequestsOptionsGeneratorsAndResultsAreActionable() {
        assertThrows(IllegalArgumentException.class, () -> service.generate(null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRequest.Tee(null, new TeeOptions(10)));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRequest.Tee(request, null));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRequest.Raglan(null, new SweaterOptions(45)));
        assertThrows(IllegalArgumentException.class, () -> new GenerationRequest.Raglan(request, null));
        assertThrows(IllegalArgumentException.class, () -> new PatternGenerationService(null, new RaglanPatternGenerator()));
        assertThrows(IllegalArgumentException.class, () -> new PatternGenerationService(new PatternGenerator(), null));
        assertThrows(IllegalArgumentException.class, () -> new GeneratedDocument(null, ""));
        var pattern = service.generate(new GenerationRequest.Tee(request, TeeSpec.bundled().defaultOptions())).pattern();
        assertThrows(IllegalArgumentException.class, () -> new GeneratedDocument(pattern, null));
    }

    private void assertSnapshot(String name, GeneratedDocument generated) throws IOException {
        try (var stream = getClass().getResourceAsStream("/" + name)) {
            assertNotNull(stream);
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8),
                    formatter.format(generated.pattern()));
        }
    }
}
