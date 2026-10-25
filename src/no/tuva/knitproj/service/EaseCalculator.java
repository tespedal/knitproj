package no.tuva.knitproj.service;

import no.tuva.knitproj.domain.FitPreference;
import no.tuva.knitproj.pattern.spec.TeeSpec;

public class EaseCalculator {
    private final TeeSpec spec;

    public EaseCalculator() {
        this(TeeSpec.bundled());
    }

    public EaseCalculator(TeeSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("T-shirt specification is required.");
        }
        this.spec = spec;
    }

    public double calculateBustEaseGain(FitPreference fitPreference) {
        return spec.bustFactor(fitPreference);
    }
    public double calculateNeckEaseGain(FitPreference fitPreference) {
        return spec.neckFactor(fitPreference);
    }
    public int calculateNeckType(String neckShape) {
        return switch (neckShape){
            case "deep" -> 7;
            default -> 5;
        };
    }
}
