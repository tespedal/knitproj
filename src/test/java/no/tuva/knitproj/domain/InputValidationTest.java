package no.tuva.knitproj.domain;

import java.util.Properties;
import no.tuva.knitproj.cli.PatternInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InputValidationTest {
    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void everyMeasurementRejectsNonpositiveAndNonfiniteValues(double invalid) {
        for (int index = 0; index < 10; index++) {
            double[] values = {35, 46, 4, 89, 82, 79, 41, 21, 29, 58};
            values[index] = invalid;
            assertThrows(IllegalArgumentException.class, () -> new Measurements(
                    values[0], values[1], values[2], values[3], values[4],
                    values[5], values[6], values[7], values[8], values[9]));
        }
        assertThrows(IllegalArgumentException.class, () -> new Gauge(22, 28, invalid));
        assertThrows(IllegalArgumentException.class, () -> new TeeOptions(invalid));
        assertThrows(IllegalArgumentException.class, () -> new SweaterOptions(invalid));
    }

    @ParameterizedTest
    @CsvSource({"0,28", "-1,28", "22,0", "22,-1"})
    void invalidGaugeCountsAreRejected(int width, int height) {
        assertThrows(IllegalArgumentException.class, () -> new Gauge(width, height, 5));
    }

    @Test
    void allRequestComponentsAreRequired() {
        Measurements measurements = new Measurements();
        Gauge gauge = new Gauge(22, 28, 5);
        assertThrows(IllegalArgumentException.class, () -> new PatternRequest(null, gauge, FitPreference.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new PatternRequest(measurements, null, FitPreference.NORMAL));
        assertThrows(IllegalArgumentException.class, () -> new PatternRequest(measurements, gauge, null));
    }

    @ParameterizedTest
    @CsvSource({"typo,10", "fit,tight", "rowsPer10Cm,2.5", "stitchesPer10Cm,abc",
            "needleSizeMm,NaN", "bustCircumferenceCm,Infinity", "sleeveLengthCm,0",
            "armholeDepthGain,0", "armholeDepthGain,NaN", "shoulderWidthGain,-1",
            "shoulderWidthGain,Infinity", "armholeDepthGain,abc"})
    void invalidPropertiesAreRejectedInsteadOfSilentlyUsingDefaults(String key, String value) {
        Properties properties = new Properties();
        properties.setProperty(key, value);
        assertThrows(IllegalArgumentException.class, () -> PatternInput.fromProperties(properties));
    }
}
