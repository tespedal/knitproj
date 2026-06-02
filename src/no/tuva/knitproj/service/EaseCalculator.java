package no.tuva.knitproj.service;

import no.tuva.knitproj.domain.FitPreference;

public class EaseCalculator {
    public double calculateBustEaseGain(FitPreference fitPreference) {
        return switch (fitPreference) {
            case NORMAL -> 1.0;
            case OVERSIZED -> 1.1;
        };
    }
    public double calculateNeckEaseGain(FitPreference fitPreference) {
        return switch (fitPreference){
            case NORMAL -> 1.3;
            case OVERSIZED -> 1.5;
        };
    }
    public int calculateNeckType(String neckShape) {
        return switch (neckShape){
            case "deep" -> 7;
            default -> 5;
        };
    }
}

