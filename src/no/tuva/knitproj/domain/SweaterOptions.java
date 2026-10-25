package no.tuva.knitproj.domain;

public record SweaterOptions(double sleeveLengthCm) {
    public SweaterOptions {
        if (!Double.isFinite(sleeveLengthCm) || sleeveLengthCm <= 0) {
            throw new IllegalArgumentException("Sweater sleeveLengthCm must be finite, positive centimetres.");
        }
    }
}
