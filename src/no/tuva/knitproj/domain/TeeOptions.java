package no.tuva.knitproj.domain;

public record TeeOptions(double sleeveLengthCm) {
    public TeeOptions {
        if (!Double.isFinite(sleeveLengthCm) || sleeveLengthCm <= 0) {
            throw new IllegalArgumentException("Sleeve length must be finite, positive centimetres.");
        }
    }

    public TeeOptions() {
        this(10);
    }
}
