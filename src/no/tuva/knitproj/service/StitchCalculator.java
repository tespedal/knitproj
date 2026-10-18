package no.tuva.knitproj.service;

public class StitchCalculator {
    public int calculateStitches(double circumferenceCm, double stitchesPer10Cm) {
        if (!Double.isFinite(circumferenceCm) || circumferenceCm < 0
                || !Double.isFinite(stitchesPer10Cm) || stitchesPer10Cm <= 0) {
            throw new IllegalArgumentException("Length must be finite and nonnegative; gauge must be positive.");
        }
        double rawStitches = circumferenceCm * stitchesPer10Cm / 10.0;
        if (!Double.isFinite(rawStitches) || rawStitches > Integer.MAX_VALUE / 16.0) {
            throw new IllegalArgumentException("Requested stitch count exceeds the supported calculation range.");
        }
        return roundToNearestEvenNumber(rawStitches);
    }

    private int roundToNearestEvenNumber(double value) {
        int rounded = (int) Math.round(value);

        if (rounded % 2 != 0) {
            rounded++;
        }

        return rounded;
    }
}
