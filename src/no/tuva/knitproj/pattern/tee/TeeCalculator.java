package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.service.EaseCalculator;
import no.tuva.knitproj.service.StitchCalculator;

public class TeeCalculator {
    private static final int RAGLAN_LINES = 4;
    private static final int CHEST_RAGLAN_LINES = 4;
    private static final int SHOULDER_RAGLAN_LINES = 4;
    private static final int NECK_INCREASE_ROUNDS = 10;
    private static final double SHOULDER_WIDTH_FACTOR = 0.8;

    private final EaseCalculator easeCalculator;
    private final StitchCalculator stitchCalculator;

    public TeeCalculator(EaseCalculator easeCalculator, StitchCalculator stitchCalculator) {
        this.easeCalculator = easeCalculator;
        this.stitchCalculator = stitchCalculator;
    }

    public TeeCalculationResults calculate(PatternRequest request) {
        Measurements measurements = request.getMeasurements();
        Gauge gauge = request.getGauge();

        double neckCm = measurements.neckCircumferenceCm()
                * easeCalculator.calculateNeckEaseGain(request.getFitPreference());

        double shoulderPatchCm = measurements.shoulderAcrossCm();
        double frontPatchCm = (neckCm - 2 * shoulderPatchCm) / 2;

        TeeStage castOnNeck = new TeeStage(
                "Cast on neck",
                widthStitches(frontPatchCm, gauge),
                widthStitches(shoulderPatchCm, gauge),
                0
        );

        // Skulderøkninger

        TeeStage afterNeck = castOnNeck.addBody(
                "After neck increases",
                NECK_INCREASE_ROUNDS * 2,  // 2 økninger per skulder
                0
        );

        double shoulderLengthCm =
                (measurements.shoulderWidthCm() * SHOULDER_WIDTH_FACTOR - frontPatchCm) / 2;

        int shoulderLengthStitches = widthStitches(shoulderLengthCm, gauge);

        TeeStage afterShoulder = castOnNeck.addBody(
                "After shoulder increases",
                shoulderLengthStitches * 2, // økninger på hver side av begge skuldre,
                shoulderLengthStitches
        );

        double armholeLengthCm = measurements.armholeDepthCm() / 2;
        int armholeLengthStitches = heightStitches(armholeLengthCm, gauge);
        int armholeIncreases = armholeLengthStitches / 2;

        TeeStage afterArmInc = afterShoulder.addShoulders(
                "After armhole increases",
                armholeIncreases * 2,
                armholeIncreases
        );



        double finishedChestCm = measurements.bustCircumferenceCm()
                * easeCalculator.calculateBustEaseGain(request.getFitPreference());

        int targetChestStitches = widthStitches(finishedChestCm, gauge);
        int raglanIncreases = (targetChestStitches - afterArmInc.bodyStitches() - castOnNeck.shoulder()) / CHEST_RAGLAN_LINES;

        TeeStage afterRaglan = afterArmInc.addBodyAndShoulders(
                "After raglan increases",
                raglanIncreases * 2,
                raglanIncreases
        );
        TeeStage afterArmHole = afterRaglan.addShoulders(
                "After arm holes",
                -afterRaglan.shoulder() + castOnNeck.shoulder(),
                0
        );



        return new TeeCalculationResults(
                castOnNeck,
                afterNeck,
                afterShoulder,
                afterArmInc,
                afterRaglan,
                afterArmHole,
                measurements.backLengthCm()
        );
    }

    private int widthStitches(double cm, Gauge gauge) {
        return stitchCalculator.calculateStitches(cm, gauge.getStitchesPer10Cm());
    }

    private int heightStitches(double cm, Gauge gauge) {
        return stitchCalculator.calculateStitches(cm, gauge.getStitchesPer10CmHeight());
    }
}