package no.tuva.knitproj.service;

import no.tuva.knitproj.domain.FitPreference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculationServicesTest {
    @ParameterizedTest
    @CsvSource({"NORMAL,1.0,1.3", "OVERSIZED,1.1,1.5"})
    void easeMultipliersAreStable(FitPreference fit, double bust, double neck) {
        EaseCalculator calculator = new EaseCalculator(
                new no.tuva.knitproj.pattern.spec.TeeSpec(1.3, 1.5, 1, 1.1, 1.2, 0.85, 10));
        assertEquals(bust, calculator.calculateBustEaseGain(fit));
        assertEquals(neck, calculator.calculateNeckEaseGain(fit));
    }

    @ParameterizedTest
    @CsvSource({"deep,7", "round,5", "Deep,5", "unknown,5"})
    void legacyNeckTypeMappingIsCaseSensitive(String shape, int expected) {
        assertEquals(expected, new EaseCalculator().calculateNeckType(shape));
    }

    @ParameterizedTest
    @CsvSource({
            "0,10,0", "0.49,10,0", "0.5,10,2", "1.49,10,2", "1.5,10,2",
            "2.49,10,2", "2.5,10,4", "3.49,10,4", "3.5,10,4", "4,22,10",
            "18.75,22,42", "10.5,28,30"
    })
    void stitchesRoundToIntegerThenRoundOddIntegersUp(double cm, double gauge, int expected) {
        assertEquals(expected, new StitchCalculator().calculateStitches(cm, gauge));
    }

    @ParameterizedTest
    @CsvSource({"-1,22", "NaN,22", "Infinity,22", "10,0", "10,-1", "10,NaN",
            "10,Infinity", "1.7976931348623157E308,22"})
    void invalidOrOverflowingStitchInputsAreRejected(double cm, double gauge) {
        assertThrows(IllegalArgumentException.class,
                () -> new StitchCalculator().calculateStitches(cm, gauge));
    }
}
