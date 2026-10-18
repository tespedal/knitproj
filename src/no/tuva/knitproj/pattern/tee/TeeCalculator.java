package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.service.EaseCalculator;
import no.tuva.knitproj.service.StitchCalculator;

public class TeeCalculator {
    private static final int CHEST_RAGLAN_LINES = 4;
    static final int NECK_INCREASE_ROUNDS = 10;

    private final EaseCalculator easeCalculator;
    private final StitchCalculator stitchCalculator;

    public TeeCalculator(EaseCalculator easeCalculator, StitchCalculator stitchCalculator) {
        this.easeCalculator = easeCalculator;
        this.stitchCalculator = stitchCalculator;
    }

    public TeeCalculationResults calculate(PatternRequest request) {
        return calculate(request, new TeeOptions());
    }

    public TeeCalculationResults calculate(PatternRequest request, TeeOptions options) {
        if (request == null) {
            throw new IllegalArgumentException("Pattern request is required.");
        }
        if (options == null) {
            throw new IllegalArgumentException("T-shirt options are required.");
        }
        Measurements measurements = request.getMeasurements();
        Gauge gauge = request.getGauge();

        double neckCm = measurements.neckCircumferenceCm()
                * easeCalculator.calculateNeckEaseGain(request.getFitPreference());

        double shoulderPatchCm = measurements.shoulderAcrossCm();
        double frontPatchCm = (neckCm - 2 * shoulderPatchCm) / 2;
        if (frontPatchCm <= 0) {
            throw new IllegalArgumentException("Neck circumference with ease must exceed both shoulder patches.");
        }

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
                (measurements.shoulderWidthCm() * options.shoulderWidthGain() - frontPatchCm) / 2;

        int shoulderLengthStitches = widthStitches(shoulderLengthCm, gauge);
        if (shoulderLengthStitches < NECK_INCREASE_ROUNDS) {
            throw new IllegalArgumentException("Shoulder width cannot accommodate the ten neck-shaping rounds.");
        }

        TeeStage afterShoulder = castOnNeck.addBody(
                "After shoulder increases",
                shoulderLengthStitches * 2, // økninger på hver side av begge skuldre,
                shoulderLengthStitches
        );

        double armholeLengthCm = measurements.armholeDepthCm() * options.armholeDepthGain() / 2;
        int armholeRows = heightRows(armholeLengthCm, gauge);
        int armholeIncreases = armholeRows / 2;

        TeeStage afterArmInc = afterShoulder.addShoulders(
                "After armhole increases",
                armholeIncreases * 2,
                armholeIncreases
        );



        double finishedChestCm = measurements.bustCircumferenceCm()
                * easeCalculator.calculateBustEaseGain(request.getFitPreference());

        int targetChestStitches = widthStitches(finishedChestCm, gauge);
        int raglanIncreases = (targetChestStitches - afterArmInc.bodyStitches() - castOnNeck.shoulder()) / CHEST_RAGLAN_LINES;
        if (targetChestStitches < afterArmInc.bodyStitches() + castOnNeck.shoulder()) {
            throw new IllegalArgumentException("Bust target is too small for the preceding shoulder shaping.");
        }

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



        for (TeeStage stage : new TeeStage[]{castOnNeck, afterNeck, afterShoulder,
                afterArmInc, afterRaglan, afterArmHole}) {
            if (stage.halfBody() <= 0 || stage.shoulder() <= 0 || stage.nrIncreases() < 0) {
                throw new IllegalArgumentException("Measurements produce an invalid stage: " + stage.name());
            }
        }

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

    private int heightRows(double cm, Gauge gauge) {
        return stitchCalculator.calculateStitches(cm, gauge.getStitchesPer10CmHeight());
    }
}