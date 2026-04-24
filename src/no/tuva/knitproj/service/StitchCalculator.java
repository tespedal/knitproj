package no.tuva.knitproj.service;

public class StitchCalculator {
    public int calculateStitches(double circumferenceCm, double stitchesPer10Cm) {
        double rawStitches = circumferenceCm * stitchesPer10Cm / 10.0;
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
