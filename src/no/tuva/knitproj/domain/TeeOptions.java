package no.tuva.knitproj.domain;

public record TeeOptions(double sleeveLengthCm, double armholeDepthGain, double shoulderWidthGain) {
    public TeeOptions {
        if (!Double.isFinite(sleeveLengthCm) || sleeveLengthCm <= 0) {
            throw new IllegalArgumentException("Sleeve length must be finite, positive centimetres.");
        }
        if (!Double.isFinite(armholeDepthGain) || armholeDepthGain <= 0) {
            throw new IllegalArgumentException("armholeDepthGain must be finite and positive.");
        }
        if (!Double.isFinite(shoulderWidthGain) || shoulderWidthGain <= 0) {
            throw new IllegalArgumentException("shoulderWidthGain must be finite and positive.");
        }
    }

    public TeeOptions(double sleeveLengthCm) {
        this(sleeveLengthCm, 1.2, 0.85);
    }

    public TeeOptions() {
        this(10);
    }
}
