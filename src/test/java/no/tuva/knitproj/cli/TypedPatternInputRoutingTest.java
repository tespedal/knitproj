package no.tuva.knitproj.cli;

import java.util.Properties;
import no.tuva.knitproj.application.GenerationRequest;
import no.tuva.knitproj.domain.PatternType;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TypedPatternInputRoutingTest {
    @Test
    void selectedDefaultsHaveConstructionSpecificOptionTypes() {
        var tee = assertInstanceOf(GenerationRequest.Tee.class,
                PatternInput.defaultRequest(PatternType.TSHIRT));
        var raglan = assertInstanceOf(GenerationRequest.Raglan.class,
                PatternInput.defaultRequest(PatternType.OVERSIZED_RAGLAN));
        assertEquals(TeeSpec.bundled().defaultOptions(), tee.options());
        assertEquals(SweaterSpec.bundled().defaultOptions(), raglan.options());
    }

    @Test
    void typedAndLegacyAdaptersShareMeasurementAndOptionParsing() {
        var properties = properties("sleeveLengthCm", "17.5");
        properties.setProperty("neckCircumferenceCm", "36");
        properties.setProperty("rowsPer10Cm", "30");
        properties.setProperty("fit", " oversized ");
        var legacy = PatternInput.fromProperties(properties);
        var typed = assertInstanceOf(GenerationRequest.Tee.class,
                PatternInput.requestFromProperties(properties, PatternType.TSHIRT));
        assertEquals(legacy.options(), typed.options());
        assertEquals(legacy.request().getMeasurements().neckCircumferenceCm(),
                typed.request().getMeasurements().neckCircumferenceCm());
        assertEquals(legacy.request().getGauge().getStitchesPer10CmHeight(),
                typed.request().getGauge().getStitchesPer10CmHeight());
        assertEquals(legacy.request().getFitPreference(), typed.request().getFitPreference());
        var raglan = assertInstanceOf(GenerationRequest.Raglan.class,
                PatternInput.requestFromProperties(properties, PatternType.OVERSIZED_RAGLAN));
        assertEquals(17.5, raglan.options().sleeveLengthCm());
        assertEquals(17.5, PatternInput.fromProperties(properties, PatternType.OVERSIZED_RAGLAN)
                .options().sleeveLengthCm());
    }

    @ParameterizedTest
    @EnumSource(PatternType.class)
    void bothEntryPointsUseTheSameKeyValidation(PatternType type) {
        var input = properties("unknownProperty", "1");
        var typed = assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(input, type));
        var legacy = assertThrows(IllegalArgumentException.class,
                () -> PatternInput.fromProperties(input, type));
        assertEquals(legacy.getMessage(), typed.getMessage());
        assertTrue(typed.getMessage().contains("unknownProperty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"armholeDepthGain", "shoulderWidthGain"})
    void sweaterRejectsGainOverridesWithMigrationMessage(String key) {
        var invalid = assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(properties(key, "NaN"),
                        PatternType.OVERSIZED_RAGLAN));
        assertTrue(invalid.getMessage().contains(key));
        assertTrue(invalid.getMessage().contains("--spec"));
    }

    @Test
    void nullInputsAndInvalidNumbersHaveActionableErrors() {
        assertThrows(IllegalArgumentException.class, () -> PatternInput.defaultRequest(null));
        assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(null, PatternType.TSHIRT));
        assertThrows(IllegalArgumentException.class,
                () -> PatternInput.readRequest(null, PatternType.TSHIRT));
        assertThrows(IllegalArgumentException.class, () -> new PatternInput(null, null));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(properties("rowsPer10Cm", "2.5"),
                        PatternType.TSHIRT)).getMessage().contains("whole number"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> PatternInput.requestFromProperties(properties("sleeveLengthCm", "text"),
                        PatternType.OVERSIZED_RAGLAN)).getMessage().contains("sleeveLengthCm"));
    }

    private static Properties properties(String key, String value) {
        var properties = new Properties();
        properties.setProperty(key, value);
        return properties;
    }
}
