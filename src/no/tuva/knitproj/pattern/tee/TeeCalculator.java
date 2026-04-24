package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.service.EaseCalculator;
import no.tuva.knitproj.service.StitchCalculator;

public class TeeCalculator {
    private final EaseCalculator easeCalculator;
    private final StitchCalculator stitchCalculator;

    public TeeCalculator(EaseCalculator easeCalculator, StitchCalculator stitchCalculator) {
        this.easeCalculator = easeCalculator;
        this.stitchCalculator = stitchCalculator;
    }

    public CalculatedPatternValues calculate(PatternRequest request) {
        Measurements measurements = request.getMeasurements();
        Gauge gauge = request.getGauge();

        // Neck
        double neckEaseGain = easeCalculator.calculateNeckEaseGain(request.getFitPreference());
        double neckCm = measurements.neckCircumferenceCm()* neckEaseGain;


        // Shoulder across
        double shoulderPatch = measurements.shoulderAcrossCm();
        int shoulderAcrossStitches = stitchCalculator.calculateStitches(
                shoulderPatch,
                gauge.getStitchesPer10Cm()
        );

        // Forstykket og bakstykket
        double frontPatchCm = (neckCm - shoulderPatch *2) /2;
        int frontPatchStitches = stitchCalculator.calculateStitches(
                frontPatchCm,
                gauge.getStitchesPer10Cm()
        );
        int backPatchStitches = frontPatchStitches / 2;

        int castOnNeckStitches = frontPatchStitches + 2* backPatchStitches + 2 * shoulderAcrossStitches;

        int afterNeckStitches = castOnNeckStitches + 9 * 4 + 1*2;

        double easeCm = easeCalculator.calculateBustEaseGain(request.getFitPreference());
        double finishedChestCm = measurements.bustCircumferenceCm() + easeCm;
        System.out.println("Shoulder width: "+ measurements.shoulderWidthCm() + "Neck width : " + frontPatchCm);
        double shoulderLength = (measurements.shoulderWidthCm()*0.8 - frontPatchCm)/2;
        int shoulderLengthStitches = stitchCalculator.calculateStitches(shoulderLength, gauge.getStitchesPer10Cm());

        int afterShoulderStitches = afterNeckStitches + 4*shoulderLengthStitches;

        double armHoleLength = measurements.armholeDepthCm() / 2;
        int armHoleLengthStitches = stitchCalculator.calculateStitches(armHoleLength, gauge.getStitchesPer10CmHeight());
        int armHoleIncreases = armHoleLengthStitches/2;

        int afterArmholeIncStitches = afterShoulderStitches + 4*armHoleIncreases;

        return new CalculatedPatternValues(castOnNeckStitches,
                backPatchStitches,
                frontPatchStitches,
                shoulderAcrossStitches,
                afterNeckStitches,
                shoulderLengthStitches,
                afterShoulderStitches,
                armHoleIncreases,
                afterArmholeIncStitches,
                finishedChestCm);
    }
}