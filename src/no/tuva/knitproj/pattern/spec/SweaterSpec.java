package no.tuva.knitproj.pattern.spec;

import java.util.Properties;
import java.util.Set;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.resources.BundledProperties;

public record SweaterSpec(double neckFactor, double bustFactor, double depthFactor, int neckRepeats,
                          double neckbandCm, double hemCm, double cuffCm, double sleeveLengthCm) {
    public SweaterSpec {
        SpecValues.positive("neckFactor", neckFactor);
        SpecValues.positive("bustFactor", bustFactor);
        SpecValues.positive("depthFactor", depthFactor);
        if (neckRepeats <= 0 || neckRepeats > Integer.MAX_VALUE / 16) {
            throw new IllegalArgumentException("Pattern specification neckRepeats must be positive and within calculation range.");
        }
        SpecValues.positive("neckbandCm", neckbandCm);
        SpecValues.positive("hemCm", hemCm);
        SpecValues.positive("cuffCm", cuffCm);
        SpecValues.positive("sleeveLengthCm", sleeveLengthCm);
        if (sleeveLengthCm <= cuffCm) {
            throw new IllegalArgumentException("Pattern specification sleeveLengthCm must exceed cuffCm.");
        }
    }

    public static SweaterSpec bundled() {
        return fromProperties(BundledProperties.load("/patterns/oversized-raglan.properties"));
    }

    public static SweaterSpec fromProperties(Properties p) {
        SpecValues.keys(p, Set.of("neckFactor", "bustFactor", "depthFactor", "neckRepeats",
                "neckbandCm", "hemCm", "cuffCm", "sleeveLengthCm"));
        return new SweaterSpec(SpecValues.number(p, "neckFactor"), SpecValues.number(p, "bustFactor"),
                SpecValues.number(p, "depthFactor"), SpecValues.whole(p, "neckRepeats"),
                SpecValues.number(p, "neckbandCm"), SpecValues.number(p, "hemCm"),
                SpecValues.number(p, "cuffCm"), SpecValues.number(p, "sleeveLengthCm"));
    }

    public SweaterOptions defaultOptions() {
        return optionsWithSleeveLength(sleeveLengthCm);
    }

    public SweaterOptions optionsWithSleeveLength(double sleeveLengthCm) {
        var options = new SweaterOptions(sleeveLengthCm);
        if (options.sleeveLengthCm() <= cuffCm) {
            throw new IllegalArgumentException("Sweater sleeveLengthCm must exceed cuffCm=" + cuffCm);
        }
        return options;
    }
}
