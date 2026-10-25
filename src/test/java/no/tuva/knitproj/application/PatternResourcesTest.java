package no.tuva.knitproj.application;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.resources.BundledProperties;
import no.tuva.knitproj.resources.PatternText;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternResourcesTest {
    private final PatternRequest request = new PatternRequest(
            new Measurements(), new Gauge(22, 28, 5), FitPreference.NORMAL);
    private final PatternTextFormatter formatter = new PatternTextFormatter();

    @Test
    void sweaterOutputMatchesPreRefactorCapture() throws Exception {
        try (var stream = getClass().getResourceAsStream("/default-raglan-pattern.txt")) {
            assertNotNull(stream);
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8),
                    formatter.format(new RaglanPatternGenerator(
                            new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 4, 45)).generate(request).pattern()));
        }
    }

    @Test
    void sweaterSpecificationDrivesCalculationsTextAndDefaults() {
        var p = sweaterProperties();
        p.setProperty("bustFactor", "1.6");
        p.setProperty("depthFactor", "1.2");
        p.setProperty("neckbandCm", "3");
        p.setProperty("hemCm", "5");
        p.setProperty("cuffCm", "6");
        p.setProperty("sleeveLengthCm", "48");
        var result = new RaglanPatternGenerator(SweaterSpec.fromProperties(p)).generate(request);
        assertEquals(89 * 1.6, result.calculations().chestTargetCm());
        assertEquals(21 * 1.2, result.calculations().raglanDepthCm());
        assertEquals(48, result.calculations().sleeveLengthCm());
        assertEquals(42, result.calculations().sleeveStraightCm());
        String text = formatter.format(result.pattern());
        assertTrue(text.contains("bryst × 1,6"));
        assertTrue(text.contains("Strikk 3 cm vrangbord"));
        assertTrue(text.contains("deretter 5 cm vrangbord"));
        assertTrue(text.contains("Strikk 6 cm vrangbord"));
        assertTrue(result.calculations().explain().contains("bust=1.6, armhole depth=1.2"));
    }

    @Test
    void teeSpecificationDrivesFitAndDefaultOptions() {
        var p = new Properties();
        p.setProperty("normal.neckFactor", "1.3");
        p.setProperty("oversized.neckFactor", "1.5");
        p.setProperty("oversized.bustFactor", "1.1");
        p.setProperty("normal.bustFactor", "1.1");
        p.setProperty("sleeveLengthCm", "12");
        p.setProperty("armholeDepthGain", "1.0");
        p.setProperty("shoulderWidthGain", "0.8");
        var spec = TeeSpec.fromProperties(p);
        var generator = new PatternGenerator(spec);
        var generated = generator.generate(request);
        var original = new PatternGenerator(new TeeSpec(1.3, 1.5, 1, 1.1, 1.2, 0.85, 10)).generate(request);
        assertNotEquals(original.calculations().afterRaglan(), generated.calculations().afterRaglan());
        assertEquals(generator.generate(request, spec.defaultOptions()).calculations(), generated.calculations());
        assertEquals(184, generated.calculations().afterShoulder().totalStitches());
        assertEquals(20, generated.calculations().afterArmInc().nrIncreases());
        assertTrue(formatter.format(generated.pattern()).contains("ermet måler 12 cm"));
        var customSleeve = generator.generate(request, spec.optionsWithSleeveLength(13));
        assertEquals(generated.calculations(), customSleeve.calculations());
        assertTrue(formatter.format(customSleeve.pattern()).contains("ermet måler 13 cm"));
    }

    @Test
    void injectedSweaterDefaultsBelowBundledCuffDriveGenerationAndWording() {
        var spec = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 2, 3);
        var generator = new RaglanPatternGenerator(spec);
        var generated = generator.generate(request);
        assertEquals(generator.generate(request, spec.defaultOptions()).calculations(), generated.calculations());
        assertEquals(3, generated.calculations().sleeveLengthCm());
        assertEquals(1, generated.calculations().sleeveStraightCm());
        String text = formatter.format(generated.pattern());
        assertTrue(text.contains("Strikk 2 cm vrangbord"));
        assertTrue(text.contains("3.0 cm"));
        var overridden = generator.generate(request, spec.optionsWithSleeveLength(3.5));
        assertEquals(1.5, overridden.calculations().sleeveStraightCm());
        assertTrue(formatter.format(overridden.pattern()).contains("3.5 cm"));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(request, new SweaterOptions(2)));
    }

    @Test
    void invalidSpecificationsFailExplicitly() {
        for (String value : new String[]{"0", "-1", "NaN", "Infinity", "wrong"}) {
            var p = BundledProperties.load("/patterns/oversized-raglan.properties");
            p.setProperty("bustFactor", value);
            assertThrows(IllegalArgumentException.class, () -> SweaterSpec.fromProperties(p));
        }
        var missing = BundledProperties.load("/patterns/tshirt.properties");
        missing.remove("normal.neckFactor");
        assertThrows(IllegalArgumentException.class, () -> TeeSpec.fromProperties(missing));
        var unknown = BundledProperties.load("/patterns/tshirt.properties");
        unknown.setProperty("typo", "1");
        assertThrows(IllegalArgumentException.class, () -> TeeSpec.fromProperties(unknown));
        var fractional = BundledProperties.load("/patterns/oversized-raglan.properties");
        fractional.setProperty("neckRepeats", "1.5");
        assertThrows(IllegalArgumentException.class, () -> SweaterSpec.fromProperties(fractional));
        assertThrows(IllegalArgumentException.class, () -> BundledProperties.load("/patterns/missing.properties"));
    }

    @Test
    void textUsesUtf8AndRejectsMissingOrUnresolvedPlaceholders() {
        var text = new PatternText();
        assertTrue(text.render("raglan.finishing", Map.of()).contains("tråder"));
        assertEquals("Legg opp 100 masker", text.render("common.castOn", Map.of("stitches", 100)));
        assertThrows(IllegalArgumentException.class, () -> text.render("missing", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> text.render("common.castOn", Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> text.render("common.castOn", Map.of("stitches", 100, "unused", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> text.render("common.castOn", Map.of("stitches", "{unresolved}")));
        var malformed = new Properties();
        malformed.setProperty("invalid", "Count {");
        assertThrows(IllegalArgumentException.class,
                () -> new PatternText(malformed).render("invalid", Map.of()));
    }

    private Properties sweaterProperties() {
        var p = new Properties();
        p.setProperty("neckFactor", "1.3");
        p.setProperty("bustFactor", "1.5");
        p.setProperty("depthFactor", "1.4");
        p.setProperty("neckRepeats", "4");
        p.setProperty("neckbandCm", "2");
        p.setProperty("hemCm", "4");
        p.setProperty("cuffCm", "4");
        p.setProperty("sleeveLengthCm", "45");
        return p;
    }
}
