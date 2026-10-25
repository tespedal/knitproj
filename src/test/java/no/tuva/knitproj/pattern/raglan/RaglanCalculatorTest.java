package no.tuva.knitproj.pattern.raglan;

import java.util.Properties;
import no.tuva.knitproj.application.RaglanPatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.service.PatternTextFormatter;
import no.tuva.knitproj.service.StitchCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RaglanCalculatorTest {
    private final SweaterSpec spec = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 4, 45);
    private final RaglanCalculator calculator = new RaglanCalculator(spec);

    @Test
    void exactReferenceStagesAndLengths() {
        var c = calculate("");
        assertEquals(new RaglanStage(40, 40, 10), c.castOn());
        assertEquals(new RaglanStage(48, 48, 10), c.afterNeck());
        assertEquals(new RaglanStage(56, 56, 10), c.afterShoulder());
        assertEquals(new RaglanStage(138, 138, 92), c.afterRaglan());
        assertEquals(100, c.castOn().totalStitches());
        assertEquals(116, c.afterNeck().totalStitches());
        assertEquals(132, c.afterShoulder().totalStitches());
        assertEquals(460, c.afterRaglan().totalStitches());
        assertEquals(4, c.neckRepeats());
        assertEquals(4, c.shoulderRepeats());
        assertEquals(41, c.raglanRepeats());
        assertEquals(82, c.raglanRows());
        assertEquals(296, c.bodyStitches());
        assertEquals(102, c.sleeveStitchesEach());
        assertEquals(10, c.underarmEach());
        assertEquals(45.5, c.neckTargetCm(), 1e-10);
        assertEquals(133.5, c.chestTargetCm(), 1e-10);
        assertEquals(29.4, c.raglanDepthCm(), 1e-10);
        assertEquals(82 / 2.8, c.actualRaglanDepthCm(), 1e-10);
        assertEquals(21, c.bodyBelowUnderarmCm(), 1e-10);
        assertEquals(17, c.bodyStraightCm(), 1e-10);
        assertEquals(41, c.sleeveStraightCm(), 1e-10);
    }

    @Test
    void everyStageContinuesFromPreviousAndConservesStitchesAtSeparation() {
        var c = calculate("");
        assertEquals(4 * c.neckRepeats(), c.afterNeck().bodyStitches() - c.castOn().bodyStitches());
        assertEquals(c.castOn().sleeveEach(), c.afterNeck().sleeveEach());
        assertEquals(4 * c.shoulderRepeats(),
                c.afterShoulder().bodyStitches() - c.afterNeck().bodyStitches());
        assertEquals(c.afterNeck().sleeveEach(), c.afterShoulder().sleeveEach());
        assertEquals(8 * c.raglanRepeats(), c.afterRaglan().totalStitches() - c.afterShoulder().totalStitches());
        assertEquals(4 * c.raglanRepeats(), c.afterRaglan().bodyStitches() - c.afterShoulder().bodyStitches());
        assertEquals(2 * c.raglanRepeats(), c.afterRaglan().sleeveEach() - c.afterShoulder().sleeveEach());
        assertEquals(c.afterRaglan().totalStitches() + 4 * c.underarmEach(),
                c.bodyStitches() + 2 * c.sleeveStitchesEach());
    }

    @Test
    void depthAndVerticalGaugeDriveRaglanNotChestOrHorizontalGauge() {
        var baseline = calculate("");
        var shallower = calculate("armholeDepthCm=20");
        var shorterRows = calculate("rowsPer10Cm=20");
        var wider = calculate("bustCircumferenceCm=92");
        var horizontal = calculate("stitchesPer10Cm=24");
        assertEquals(39, shallower.raglanRepeats());
        assertEquals(6, shallower.shoulderRepeats());
        assertEquals(30, shorterRows.raglanRepeats());
        assertEquals(15, shorterRows.shoulderRepeats());
        assertEquals(baseline.raglanRepeats(), wider.raglanRepeats());
        assertEquals(baseline.raglanRepeats(), horizontal.raglanRepeats());
        assertTrue(wider.shoulderRepeats() > baseline.shoulderRepeats());
        assertEquals(baseline.bodyStitches(), shallower.bodyStitches());
        assertEquals(baseline.bodyStitches(), shorterRows.bodyStitches());
        assertEquals(2 * baseline.raglanRepeats(), baseline.raglanRows());
    }

    @Test
    void zeroShoulderOrRaglanRepeatsAreValidAndRenderWithoutBogusInstructions() {
        var noShoulder = calculate("armholeDepthCm=23");
        assertEquals(0, noShoulder.shoulderRepeats());
        assertEquals(noShoulder.afterNeck(), noShoulder.afterShoulder());
        var noRaglan = calculate("armholeDepthCm=0.01");
        assertEquals(0, noRaglan.raglanRepeats());
        assertEquals(0, noRaglan.raglanRows());
        assertEquals(noRaglan.afterShoulder(), noRaglan.afterRaglan());
        assertEquals(45, noRaglan.shoulderRepeats());
        assertTrue(text("armholeDepthCm=23").contains("Ingen skulderøkninger trengs"));
        assertTrue(text("armholeDepthCm=0.01").contains("Hopp over raglanøkningene"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bustCircumferenceCm=60", "armholeDepthCm=24"})
    void tooNarrowForDepthIsRejectedInsteadOfTruncatingNegativeRepeats(String property) {
        var error = assertThrows(IllegalArgumentException.class, () -> calculate(property));
        assertTrue(error.getMessage().contains("Bust target is too small"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bustCircumferenceCm=1000", "bodyLengthCm=40", "rowsPer10Cm=1"})
    void upperWidthOrTooShortBodyIsRejectedBeforeRendering(String property) {
        var error = assertThrows(IllegalArgumentException.class, () -> calculate(property));
        assertTrue(error.getMessage().contains("bodyLengthCm"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"shoulderAcrossCm=25", "shoulderAcrossCm=0.01", "neckCircumferenceCm=1",
            "bustCircumferenceCm=1.7976931348623157E308", "armholeDepthCm=1.7976931348623157E308"})
    void impossiblePatchesAndOverflowAreRejected(String property) {
        assertThrows(IllegalArgumentException.class, () -> calculate(property));
    }

    @Test
    void targetsAreSymmetricAndBoundedWithoutDroppingShoulderResiduals() {
        for (int tenths = 840; tenths < 950; tenths++) {
            double bust = tenths / 10.0;
            var c = calculate("bustCircumferenceCm=" + bust + "\nbodyLengthCm=80");
            double raw = bust * 1.5 * 2.2;
            double error = c.bodyStitches() - raw;
            assertTrue(error >= -1.0000001 && error < 3.0000001, "error=" + error);
            assertEquals(0, c.bodyStitches() % 4);
            assertEquals(c.bodyStitches(), c.afterNeck().bodyStitches() + 4 * c.shoulderRepeats()
                    + 4 * c.raglanRepeats() + 2 * c.underarmEach());
            assertTrue(c.shoulderRepeats() >= 0);
        }
    }

    @Test
    void fitAndUnusedMeasurementsDoNotChangeSweater() {
        var baseline = calculate("");
        var changed = calculate("fit=OVERSIZED\n"
                + "shoulderWidthCm=99\nunderbustCircumferenceCm=99\nwaistCircumferenceCm=99\n"
                + "backLengthCm=99\nupperArmCircumferenceCm=99");
        assertEquals(baseline, changed);
        var normal = input("");
        assertEquals(baseline, calculator.calculate(
                new PatternRequest(normal.request().getMeasurements(), normal.request().getGauge(), FitPreference.OVERSIZED),
                spec.defaultOptions()));
    }

    @Test
    void completeNorwegianPatternHasCountsFinishingAndConfigurableLongSleeves() {
        String text = text("sleeveLengthCm=50");
        for (String expected : new String[]{"OVERSIZED RAGLANGENSER", "Legg opp 100 masker",
                "HALSØKNINGER", "SKULDERØKNINGER", "RAGLANØKNINGER", "DELING TIL BOL OG ERMER",
                "Bolen har nå 296 masker", "ERMER — STRIKK TO LIKE", "totalt 102 masker",
                "46.0 cm", "50.0 cm", "4 cm vrangbord", "MONTERING", "Fest alle tråder", "prøvestrikking"}) {
            assertTrue(text.contains(expected), expected);
        }
        assertFalse(text.contains("0 ganger"));
        assertEquals(calculate("").afterRaglan(), calculate("sleeveLengthCm=50").afterRaglan());
    }

    @Test
    void applicationDefaultsDeterminismAndNullValidation() {
        var generator = new RaglanPatternGenerator(spec);
        var request = input("").request();
        var generated = generator.generate(request);
        assertEquals(45, generated.calculations().sleeveLengthCm());
        assertEquals(generated.calculations(), generator.generate(request).calculations());
        assertEquals(new PatternTextFormatter().format(generated.pattern()),
                new PatternTextFormatter().format(generator.generate(request).pattern()));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(request, null));
        assertEquals(4, new SweaterOptions(4).sleeveLengthCm());
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(request, new SweaterOptions(4)));
        assertThrows(IllegalArgumentException.class, () -> spec.optionsWithSleeveLength(4));
        assertThrows(IllegalArgumentException.class, () -> new SweaterOptions(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new SweaterOptions(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new RaglanStage(1, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> new RaglanStage(2, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new StitchCalculator()
                .calculateSymmetricStitches(Double.MAX_VALUE, 22));
    }

    @Test
    void sleeveCompatibilityUsesSelectedCuffRatherThanBundledCuff() {
        var spec = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 2, 3);
        var custom = new RaglanCalculator(spec);
        var request = input("").request();
        var result = custom.calculate(request, new SweaterOptions(3));
        assertEquals(3, result.sleeveLengthCm());
        assertEquals(1, result.sleeveStraightCm());
        for (double invalid : new double[]{1, 2}) {
            var error = assertThrows(IllegalArgumentException.class,
                    () -> custom.calculate(request, new SweaterOptions(invalid)));
            assertTrue(error.getMessage().contains("cuffCm=2.0"));
        }
        var largerCuff = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 6, 45);
        assertThrows(IllegalArgumentException.class,
                () -> new RaglanCalculator(largerCuff).calculate(request, new SweaterOptions(5)));
    }

    private RaglanCalculationResults calculate(String properties) {
        var input = input(properties);
        return calculator.calculate(input.request(), new SweaterOptions(input.options().sleeveLengthCm()));
    }

    private String text(String properties) {
        var input = input(properties);
        return new PatternTextFormatter().format(new RaglanPatternGenerator(spec).generate(input.request(),
                new SweaterOptions(input.options().sleeveLengthCm())).pattern());
    }

    private no.tuva.knitproj.application.GenerationRequest.Raglan input(String values) {
        var properties = new Properties();
        for (String line : values.split("\n")) {
            if (!line.isBlank()) {
                String[] entry = line.split("=", 2);
                properties.setProperty(entry[0], entry[1]);
            }
        }
        return PatternInput.requestFromProperties(properties, spec);
    }
}
