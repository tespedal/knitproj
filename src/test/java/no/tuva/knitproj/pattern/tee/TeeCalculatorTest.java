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
import no.tuva.knitproj.service.EaseCalculator;
import no.tuva.knitproj.service.PatternTextFormatter;
import no.tuva.knitproj.service.StitchCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TeeCalculatorTest {
    private final TeeCalculator calculator = new TeeCalculator(new EaseCalculator(), new StitchCalculator());

    @Test
    void defaultPatternExactlyMatchesOutputCapturedBeforeRefactoring() throws IOException {
        PatternRequest request = request(22, 28, FitPreference.NORMAL);
        String actual = new PatternTextFormatter().format(
                new PatternResult(new TeeTemplate().createSections(request, calculator.calculate(request))));
        try (var stream = getClass().getResourceAsStream("/default-pattern.txt")) {
            assertNotNull(stream);
            assertEquals(new String(stream.readAllBytes(), StandardCharsets.UTF_8), actual);
        }
    }

    @Test
    void defaultStageValuesPreserveLegacyArithmetic() {
        TeeCalculationResults result = calculator.calculate(request(22, 28, FitPreference.NORMAL));
        assertEquals(List.of(
                new TeeStage("Cast on neck", 42, 10, 0),
                new TeeStage("After neck increases", 62, 10, 0),
                new TeeStage("After shoulder increases", 82, 10, 20),
                new TeeStage("After armhole increases", 82, 40, 15),
                new TeeStage("After raglan increases", 92, 50, 5),
                new TeeStage("After arm holes", 92, 10, 0)), stages(result));
        assertEquals(41, result.backLength());
    }

    @ParameterizedTest
    @CsvSource({
            "18,24,NORMAL,84,124,148,200,240,168",
            "18,24,OVERSIZED,96,136,152,204,268,184",
            "22,28,NORMAL,104,144,184,244,284,204",
            "22,28,OVERSIZED,120,160,184,244,324,224",
            "26,32,NORMAL,120,160,216,284,332,240",
            "26,32,OVERSIZED,136,176,216,284,380,264"
    })
    void gaugesAndFitPreserveCapturedStageTotals(int width, int height, FitPreference fit,
                                                int castOn, int neck, int shoulder,
                                                int arm, int raglan, int body) {
        TeeCalculationResults result = calculator.calculate(request(width, height, fit));
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
    }

    private static PatternRequest request(int width, int height, FitPreference fit) {
        return new PatternRequest(new Measurements(), new Gauge(width, height, 5), fit);
    }

    private static List<TeeStage> stages(TeeCalculationResults result) {
        return List.of(result.castOnNeck(), result.afterNeck(), result.afterShoulder(),
                result.afterArmInc(), result.afterRaglan(), result.afterArmHole());
    }
}
