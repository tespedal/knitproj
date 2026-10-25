package no.tuva.knitproj.pattern.spec;

import java.util.Properties;
import java.util.Set;
import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.resources.BundledProperties;

public record TeeSpec(double normalNeckFactor, double oversizedNeckFactor,
                      double normalBustFactor, double oversizedBustFactor,
                      double armholeDepthGain, double shoulderWidthGain, double sleeveLengthCm) {
    public TeeSpec {
        SpecValues.positive("normal.neckFactor", normalNeckFactor);
        SpecValues.positive("oversized.neckFactor", oversizedNeckFactor);
        SpecValues.positive("normal.bustFactor", normalBustFactor);
        SpecValues.positive("oversized.bustFactor", oversizedBustFactor);
        SpecValues.positive("armholeDepthGain", armholeDepthGain);
        SpecValues.positive("shoulderWidthGain", shoulderWidthGain);
        SpecValues.positive("sleeveLengthCm", sleeveLengthCm);
    }

    public static TeeSpec bundled() {
        return fromProperties(BundledProperties.load("/patterns/tshirt.properties"));
    }

    public static TeeSpec fromProperties(Properties p) {
        SpecValues.keys(p, Set.of("normal.neckFactor", "oversized.neckFactor", "normal.bustFactor",
                "oversized.bustFactor", "armholeDepthGain", "shoulderWidthGain", "sleeveLengthCm"));
        return new TeeSpec(SpecValues.number(p, "normal.neckFactor"), SpecValues.number(p, "oversized.neckFactor"),
                SpecValues.number(p, "normal.bustFactor"), SpecValues.number(p, "oversized.bustFactor"),
                SpecValues.number(p, "armholeDepthGain"), SpecValues.number(p, "shoulderWidthGain"),
                SpecValues.number(p, "sleeveLengthCm"));
    }

    public double neckFactor(FitPreference fit) {
        return switch (fit) {
            case NORMAL -> normalNeckFactor;
            case OVERSIZED -> oversizedNeckFactor;
        };
    }

    public double bustFactor(FitPreference fit) {
        return switch (fit) {
            case NORMAL -> normalBustFactor;
            case OVERSIZED -> oversizedBustFactor;
        };
    }

    public TeeOptions defaultOptions() {
        return optionsWithSleeveLength(sleeveLengthCm);
    }

    public TeeOptions optionsWithSleeveLength(double sleeveLengthCm) {
        return new TeeOptions(sleeveLengthCm);
    }
}
