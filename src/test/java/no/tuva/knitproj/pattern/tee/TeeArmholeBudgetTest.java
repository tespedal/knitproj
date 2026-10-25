package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeeArmholeBudgetTest {
    private final PatternGenerator generator = new PatternGenerator(
            new TeeSpec(1.3, 1.5, 1.1, 1.3, 1.2, 0.85, 10));

    private PatternRequest request(int widthGauge, int rowGauge, double depth) {
        var m = new Measurements();
        return new PatternRequest(new Measurements(m.neckCircumferenceCm(), m.shoulderWidthCm(),
                m.shoulderAcrossCm(), m.bustCircumferenceCm(), m.underbustCircumferenceCm(),
                m.waistCircumferenceCm(), m.backLengthCm(), depth, m.upperArmCircumferenceCm(),
                m.bodyLengthCm()), new Gauge(widthGauge, rowGauge, 4.5), FitPreference.NORMAL);
    }

    @Test
    void approvedComparisonMatchesExactCountsAndNorwegianInstructions() {
        var generated = generator.generate(request(24, 28, 21));
        var c = generated.calculations();
        assertEquals(112, c.castOnNeck().totalStitches());
        assertEquals(208, c.afterShoulder().totalStitches());
        assertEquals(27, c.afterArmInc().nrIncreases());
        assertEquals(316, c.afterArmInc().totalStitches());
        assertEquals(9, c.afterRaglan().nrIncreases());
        assertEquals(388, c.afterRaglan().totalStitches());
        assertEquals(244, c.afterArmHole().totalStitches());
        assertEquals(82, c.afterRaglan().shoulder());
        assertEquals(92, c.sleeveStitches());
        assertEquals(72, c.armholeShapingRows());
        String text = new PatternTextFormatter().format(generated.pattern());
        assertTrue(text.contains("27 ganger = 316 masker"));
        assertTrue(text.contains("9 ganger = 388 masker"));
        assertTrue(text.contains("92 masker"));
        assertTrue(c.explain().contains("after shoulder shaping)=72"));
    }

    @Test
    void deeperArmholeAddsOnlySleeveRoundsAndLeavesBustAndRaglanUnchanged() {
        var shallow = generator.generate(request(24, 28, 21)).calculations();
        var deeper = generator.generate(request(24, 28, 25)).calculations();
        assertEquals(shallow.afterRaglan().nrIncreases(), deeper.afterRaglan().nrIncreases());
        assertEquals(shallow.afterArmHole().bodyStitches(), deeper.afterArmHole().bodyStitches());
        assertEquals(shallow.afterArmHole().totalStitches(), deeper.afterArmHole().totalStitches());
        assertEquals(84, deeper.armholeShapingRows());
        assertEquals(33, deeper.afterArmInc().nrIncreases());
    }

    @Test
    void rowGaugeControlsHeightBudgetIndependentlyOfHorizontalGauge() {
        var c = generator.generate(request(24, 30, 21)).calculations();
        assertEquals(76, c.armholeShapingRows());
        assertEquals(29, c.afterArmInc().nrIncreases());
        assertEquals(9, c.afterRaglan().nrIncreases());
        assertEquals(244, c.afterArmHole().totalStitches());
    }

    @Test
    void exactRaglanBudgetAllowsZeroSleeveIncreasesAndSkipsTheirInstructions() {
        var generated = generator.generate(request(24, 28, 18 / (1.2 * 2.8)));
        assertEquals(0, generated.calculations().afterArmInc().nrIncreases());
        assertEquals(18, generated.calculations().armholeShapingRows());
        assertTrue(new PatternTextFormatter().format(generated.pattern()).contains("Ingen ermeøkninger trengs"));
    }

    @Test
    void insufficientDepthIsRejectedRatherThanClampedOrExtended() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> generator.generate(request(24, 28, 16 / (1.2 * 2.8))));
        assertTrue(error.getMessage().contains("Armhole depth"));
    }
}
