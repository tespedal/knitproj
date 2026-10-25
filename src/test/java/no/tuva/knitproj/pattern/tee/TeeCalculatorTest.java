package no.tuva.knitproj.pattern.tee;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.Measurements;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.service.PatternTextFormatter;
import no.tuva.knitproj.service.StitchCalculator;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TeeCalculatorTest {
    private final TeeCalculator calculator = new TeeCalculator(
            new TeeSpec(1.3, 1.5, 1.0, 1.1, 1.2, 0.85, 10), new StitchCalculator());

    private TeeCalculator withGains(double depth, double shoulder) {
        return new TeeCalculator(new TeeSpec(1.3, 1.5, 1, 1.1, depth, shoulder, 10),
                new StitchCalculator());
    }

    @Test
    void explicitHistoricalScalingAndCombinedBudgetRetainOriginalGolden() throws IOException {
        PatternRequest request = request(22, 28, FitPreference.NORMAL);
        String actual = new PatternTextFormatter().format(
                new PatternResult(new TeeTemplate().createSections(request,
                        withGains(40.0 / (21 * 2.8), 0.8).calculate(request), new TeeOptions(10))));
        try (var stream = getClass().getResourceAsStream("/default-pattern.txt")) {
            assertNotNull(stream);
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8), actual);
        }
    }

    @Test
    void fullDepthBudgetPreservesBustAccountingWithHistoricalScaling() {
        TeeCalculationResults result = withGains(1.0, 0.8).calculate(request(22, 28, FitPreference.NORMAL));
        assertEquals(List.of(
                new TeeStage("Cast on neck", 42, 10, 0),
                new TeeStage("After neck increases", 62, 10, 0),
                new TeeStage("After shoulder increases", 82, 10, 20),
                new TeeStage("After armhole increases", 82, 60, 25),
                new TeeStage("After raglan increases", 92, 70, 5),
                new TeeStage("After arm holes", 92, 10, 0)), stages(result));
        assertEquals(41, result.backLength());
    }

    @ParameterizedTest
    @CsvSource({
            "18,24,NORMAL,84,124,148,228,268,168",
            "18,24,OVERSIZED,96,136,152,220,284,184",
            "22,28,NORMAL,104,144,184,284,324,204",
            "22,28,OVERSIZED,120,160,184,264,344,224",
            "26,32,NORMAL,120,160,216,328,376,240",
            "26,32,OVERSIZED,136,176,216,304,400,264"
    })
    void gaugesAndFitUseFullDepthBudget(int width, int height, FitPreference fit,
                                                int castOn, int neck, int shoulder,
                                                int arm, int raglan, int body) {
        TeeCalculationResults result = withGains(1.0, 0.8).calculate(request(width, height, fit));
        assertEquals(List.of(castOn, neck, shoulder, arm, raglan, body),
                stages(result).stream().map(TeeStage::totalStitches).toList());
        for (TeeStage stage : stages(result)) {
            assertEquals(0, stage.halfBody() % 2);
            assertEquals(stage.halfBody(), stage.frontPatchStitches());
            assertEquals(stage.halfBody() / 2, stage.backPatchStitches());
            assertEquals(stage.halfBody() * 2, stage.bodyStitches());
            assertEquals(stage.bodyStitches() + stage.shoulder() * 2, stage.totalStitches());
        }
        assertEquals(result.afterShoulder().halfBody(), result.afterArmInc().halfBody());
        assertEquals(result.afterRaglan().halfBody(), result.afterArmHole().halfBody());
        assertEquals(result.castOnNeck().shoulder(), result.afterArmHole().shoulder());
    }

    @Test
    void stageOperationsDoNotMutateTheirSourceAndNormalizeOddBodyByRedistributingOneStitch() {
        TeeStage initial = new TeeStage("initial", 41, 11, 0);
        assertEquals(new TeeStage("initial", 42, 10, 0), initial);
        assertEquals(104, initial.totalStitches());
        assertEquals(new TeeStage("body", 46, 10, 2), initial.addBody("body", 4, 2));
        assertEquals(new TeeStage("shoulder", 42, 14, 2), initial.addShoulders("shoulder", 4, 2));
        assertEquals(new TeeStage("both", 46, 14, 2), initial.addBodyAndShoulders("both", 4, 2));
        assertEquals(new TeeStage("initial", 42, 10, 0), initial);
    }

    @ParameterizedTest
    @CsvSource({"neckCircumferenceCm,5", "shoulderWidthCm,10", "shoulderWidthCm,25",
            "bustCircumferenceCm,20", "shoulderAcrossCm,0.01", "stitchesPer10Cm,1",
            "neckCircumferenceCm,1.7976931348623157E308"})
    void positiveButImpossibleCombinationsAreRejected(String key, String value) {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        PatternRequest request = PatternInput.fromProperties(properties).request();
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(request));
    }

    @Test
    void nullRequestIsRejectedWithValidationError() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(request(22, 28, FitPreference.NORMAL), null));
    }

    @ParameterizedTest
    @CsvSource({
            "1.0,0.8,164,60,25,70,5,80",
            "1.2,0.8,164,72,31,82,5,92",
            "1.0,0.85,172,64,27,70,3,80",
            "1.2,0.85,172,76,33,82,3,92",
            "1.4,0.8,164,82,36,92,5,102"
    })
    void gainFactorsApplyBeforeConversionAndPreserveStitchAccounting(
            double armholeGain, double shoulderGain, int body, int armShoulder,
            int armRepeats, int raglanShoulder, int raglanRepeats, int sleeve) {
        var result = withGains(armholeGain, shoulderGain).calculate(request(22, 28, FitPreference.NORMAL));
        assertEquals(body, result.afterShoulder().bodyStitches());
        assertEquals(armShoulder, result.afterArmInc().shoulder());
        assertEquals(armRepeats, result.afterArmInc().nrIncreases());
        assertEquals(raglanShoulder, result.afterRaglan().shoulder());
        assertEquals(raglanRepeats, result.afterRaglan().nrIncreases());
        assertEquals(sleeve, result.sleeveStitches());
        assertEquals(204, result.afterArmHole().totalStitches());
        for (var stage : stages(result)) {
            assertEquals(stage.bodyStitches() + 2 * stage.shoulder(), stage.totalStitches());
            assertTrue(stage.nrIncreases() >= 0);
        }
    }

    @Test
    void calculatorUsesItsSelectedSpecificationEvenWithoutExplicitOptions() {
        var request = request(22, 28, FitPreference.NORMAL);
        var result = calculator.calculate(request);
        assertEquals(calculator.calculate(request, new TeeOptions(10)), result);
        assertEquals(192, result.afterShoulder().totalStitches());
        assertEquals(324, result.afterArmInc().totalStitches());
        assertEquals(348, result.afterRaglan().totalStitches());
    }

    @ParameterizedTest
    @CsvSource({"18,24,156,264,288,76", "22,28,192,324,348,92",
            "26,32,224,372,404,102"})
    void explicitReferenceSpecUsesHorizontalAndVerticalGaugesSeparately(
            int stitches, int rows, int shoulderTotal, int armTotal, int raglanTotal, int sleeve) {
        var result = calculator.calculate(request(stitches, rows, FitPreference.NORMAL));
        assertEquals(shoulderTotal, result.afterShoulder().totalStitches());
        assertEquals(armTotal, result.afterArmInc().totalStitches());
        assertEquals(raglanTotal, result.afterRaglan().totalStitches());
        assertEquals(sleeve, result.sleeveStitches());
    }

    @ParameterizedTest
    @CsvSource({"1.2,0.1", "1.2,2.0", "1.7976931348623157E308,0.85",
            "1.2,1.7976931348623157E308"})
    void incompatibleOrOverflowingGainsAreRejected(double armholeGain, double shoulderGain) {
        assertThrows(IllegalArgumentException.class, () -> withGains(armholeGain, shoulderGain).calculate(
                request(22, 28, FitPreference.NORMAL)));
    }

    private static PatternRequest request(int width, int height, FitPreference fit) {
        return new PatternRequest(new Measurements(), new Gauge(width, height, 5), fit);
    }

    private static List<TeeStage> stages(TeeCalculationResults result) {
        return List.of(result.castOnNeck(), result.afterNeck(), result.afterShoulder(),
                result.afterArmInc(), result.afterRaglan(), result.afterArmHole());
    }
}
