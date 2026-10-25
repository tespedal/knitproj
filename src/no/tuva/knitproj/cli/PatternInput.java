package no.tuva.knitproj.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import no.tuva.knitproj.application.GenerationRequest;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.Gauge;
import no.tuva.knitproj.domain.Measurements;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternType;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.pattern.spec.TeeSpec;

/**
 * Legacy T-shirt-shaped input adapter. New callers should use the typed request methods,
 * which retain sweater options as sweater options.
 */
public record PatternInput(PatternRequest request, TeeOptions options) {
    private static final Set<String> KEYS = Set.of(
            "neckCircumferenceCm", "shoulderWidthCm", "shoulderAcrossCm", "bustCircumferenceCm",
            "underbustCircumferenceCm", "waistCircumferenceCm", "backLengthCm", "armholeDepthCm",
            "upperArmCircumferenceCm", "bodyLengthCm", "stitchesPer10Cm", "rowsPer10Cm",
            "needleSizeMm", "fit", "sleeveLengthCm");
    private static final Set<String> CONSTRUCTION_KEYS = Set.of(
            "normal.neckFactor", "oversized.neckFactor", "normal.bustFactor", "oversized.bustFactor",
            "armholeDepthGain", "shoulderWidthGain", "neckFactor", "bustFactor", "depthFactor",
            "neckRepeats", "neckbandCm", "hemCm", "cuffCm");

    public PatternInput {
        if (request == null || options == null) {
            throw new IllegalArgumentException("Pattern request and T-shirt options are required.");
        }
    }

    public static GenerationRequest defaultRequest(PatternType type) {
        return requestFromProperties(new Properties(), type);
    }

    public static GenerationRequest readRequest(Path path, PatternType type) throws IOException {
        requireType(type);
        return requestFromProperties(readProperties(path), type);
    }

    public static GenerationRequest requestFromProperties(Properties properties, PatternType type) {
        requireType(type);
        return switch (type) {
            case TSHIRT -> requestFromProperties(properties, TeeSpec.bundled());
            case OVERSIZED_RAGLAN -> requestFromProperties(properties, SweaterSpec.bundled());
        };
    }

    public static GenerationRequest.Tee requestFromProperties(Properties properties, TeeSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("T-shirt specification is required.");
        }
        var parsed = parse(properties, spec.sleeveLengthCm());
        return new GenerationRequest.Tee(parsed.request(), spec.optionsWithSleeveLength(parsed.sleeveLengthCm()));
    }

    public static GenerationRequest.Raglan requestFromProperties(Properties properties, SweaterSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Sweater specification is required.");
        }
        var parsed = parse(properties, spec.sleeveLengthCm());
        return new GenerationRequest.Raglan(parsed.request(), spec.optionsWithSleeveLength(parsed.sleeveLengthCm()));
    }

    public static PatternInput defaults() {
        return fromProperties(new Properties());
    }

    public static PatternInput defaults(PatternType type) {
        return fromProperties(new Properties(), type);
    }

    public static PatternInput read(Path path) throws IOException {
        return read(path, PatternType.TSHIRT);
    }

    public static PatternInput read(Path path, PatternType type) throws IOException {
        requireType(type);
        return fromProperties(readProperties(path), type);
    }

    private static Properties readProperties(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Input path is required.");
        }
        return Utf8Properties.read(path);
    }

    public static PatternInput fromProperties(Properties properties) {
        return fromProperties(properties, PatternType.TSHIRT);
    }

    public static PatternInput fromProperties(Properties properties, PatternType type) {
        var selected = requestFromProperties(properties, type);
        double sleeve = selected instanceof GenerationRequest.Tee tee ? tee.options().sleeveLengthCm()
                : ((GenerationRequest.Raglan) selected).options().sleeveLengthCm();
        return new PatternInput(selected.request(), new TeeOptions(sleeve));
    }

    private static ParsedInput parse(Properties properties, double sleeveDefault) {
        if (properties == null) {
            throw new IllegalArgumentException("Input properties are required.");
        }
        for (String key : properties.stringPropertyNames()) {
            if (CONSTRUCTION_KEYS.contains(key)) {
                throw new IllegalArgumentException(key + " belongs to a complete pattern specification, not measurements. "
                        + "Move construction settings to a spec file and select it with --spec PATH.");
            }
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
        return new ParsedInput(new PatternRequest(measurements, gauge, fit),
                positive(properties, "sleeveLengthCm", sleeveDefault));
    }

    private static void requireType(PatternType type) {
        if (type == null) {
            throw new IllegalArgumentException("Pattern type is required.");
        }
    }

    private record ParsedInput(PatternRequest request, double sleeveLengthCm) {}

    private static double positive(Properties properties, String key, double fallback) {
        double value = number(properties, key, fallback);
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(key + " must be finite and positive.");
        }
        return value;
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
