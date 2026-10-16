package no.tuva.knitproj.cli;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.Measurements;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.TeeOptions;

public record PatternInput(PatternRequest request, TeeOptions options) {
    private static final Set<String> KEYS = Set.of(
            "neckCircumferenceCm", "shoulderWidthCm", "shoulderAcrossCm", "bustCircumferenceCm",
            "underbustCircumferenceCm", "waistCircumferenceCm", "backLengthCm", "armholeDepthCm",
            "upperArmCircumferenceCm", "bodyLengthCm", "stitchesPer10Cm", "rowsPer10Cm",
            "needleSizeMm", "fit", "sleeveLengthCm");

    public static PatternInput defaults() {
        return fromProperties(new Properties());
    }

    public static PatternInput read(Path path) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return fromProperties(properties);
    }

    public static PatternInput fromProperties(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            if (!KEYS.contains(key)) {
                throw new IllegalArgumentException("Unknown input property: " + key);
            }
        }
        Measurements defaults = new Measurements();
        Measurements measurements = new Measurements(
                number(properties, "neckCircumferenceCm", defaults.neckCircumferenceCm()),
                number(properties, "shoulderWidthCm", defaults.shoulderWidthCm()),
                number(properties, "shoulderAcrossCm", defaults.shoulderAcrossCm()),
                number(properties, "bustCircumferenceCm", defaults.bustCircumferenceCm()),
                number(properties, "underbustCircumferenceCm", defaults.underbustCircumferenceCm()),
                number(properties, "waistCircumferenceCm", defaults.waistCircumferenceCm()),
                number(properties, "backLengthCm", defaults.backLengthCm()),
                number(properties, "armholeDepthCm", defaults.armholeDepthCm()),
                number(properties, "upperArmCircumferenceCm", defaults.upperArmCircumferenceCm()),
                number(properties, "bodyLengthCm", defaults.bodyLengthCm()));
        Gauge gauge = new Gauge(
                integer(properties, "stitchesPer10Cm", 22),
                integer(properties, "rowsPer10Cm", 28),
                number(properties, "needleSizeMm", 5));
        FitPreference fit;
        try {
            fit = FitPreference.valueOf(properties.getProperty("fit", "NORMAL").strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("fit must be NORMAL or OVERSIZED.", e);
        }
        return new PatternInput(new PatternRequest(measurements, gauge, fit),
                new TeeOptions(number(properties, "sleeveLengthCm", 10)));
    }

    private static double number(Properties properties, String key, double fallback) {
        try {
            return Double.parseDouble(properties.getProperty(key, Double.toString(fallback)).strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number.", e);
        }
    }

    private static int integer(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a whole number.", e);
        }
    }
}
