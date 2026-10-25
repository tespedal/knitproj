package no.tuva.knitproj.pattern.spec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.domain.TeeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OptionsDefaultsBTest {
    @Test
    void teeFactoriesUseSelectedSpecificationAndOnlyOverrideSleeveLength() {
        var spec = new TeeSpec(1.3, 1.5, 1, 1.1, 1.4, 0.9, 14);
        assertEquals(new TeeOptions(14), spec.defaultOptions());
        assertEquals(new TeeOptions(3), spec.optionsWithSleeveLength(3));
        assertEquals(new TeeOptions(TeeSpec.bundled().sleeveLengthCm()), TeeSpec.bundled().defaultOptions());
    }

    @Test
    void sweaterFactoriesUseSelectedDefaultAndSelectedCuff() {
        var shortCuff = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 2, 3);
        assertEquals(new SweaterOptions(3), shortCuff.defaultOptions());
        assertEquals(new SweaterOptions(2.5), shortCuff.optionsWithSleeveLength(2.5));
        assertThrows(IllegalArgumentException.class, () -> shortCuff.optionsWithSleeveLength(2));
        assertThrows(IllegalArgumentException.class, () -> shortCuff.optionsWithSleeveLength(1));
        var longCuff = new SweaterSpec(1.3, 1.5, 1.4, 4, 2, 4, 6, 48);
        assertEquals(new SweaterOptions(48), longCuff.defaultOptions());
        assertThrows(IllegalArgumentException.class, () -> longCuff.optionsWithSleeveLength(5));
        assertThrows(IllegalArgumentException.class, () -> longCuff.optionsWithSleeveLength(6));
        assertEquals(new SweaterOptions(SweaterSpec.bundled().sleeveLengthCm()), SweaterSpec.bundled().defaultOptions());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void factoriesRejectNonpositiveAndNonfiniteSleeves(double invalid) {
        assertThrows(IllegalArgumentException.class, () -> TeeSpec.bundled().optionsWithSleeveLength(invalid));
        assertThrows(IllegalArgumentException.class, () -> SweaterSpec.bundled().optionsWithSleeveLength(invalid));
    }

    @Test
    void optionConstructorsRequireCompleteExplicitValues() throws ReflectiveOperationException {
        assertNotNull(TeeOptions.class.getConstructor(double.class));
        assertNotNull(SweaterOptions.class.getConstructor(double.class));
        assertThrows(NoSuchMethodException.class, () -> TeeOptions.class.getConstructor());
        assertThrows(NoSuchMethodException.class, () -> TeeOptions.class.getConstructor(double.class, double.class, double.class));
        assertThrows(NoSuchMethodException.class, () -> SweaterOptions.class.getConstructor());
        assertEquals(1, new SweaterOptions(1).sleeveLengthCm());
    }

    @Test
    void domainSourcesDoNotDependOnSpecificationsResourcesOrApplication() throws IOException {
        try (var sources = Files.walk(Path.of("src/no/tuva/knitproj/domain"))) {
            for (var source : sources.filter(path -> path.toString().endsWith(".java")).toList()) {
                String code = Files.readString(source);
                for (String forbidden : new String[]{"pattern", "resources", "application"}) {
                    assertFalse(code.contains("no.tuva.knitproj." + forbidden + "."),
                            source + " must not depend on " + forbidden);
                }
            }
        }
    }
}
