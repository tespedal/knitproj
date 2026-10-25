package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.service.StitchCalculator;

public class TeeCalculator {
    private static final int CHEST_RAGLAN_LINES = 4;
    static final int NECK_INCREASE_ROUNDS = 10;

    private final TeeSpec spec;
    private final StitchCalculator stitchCalculator;

    public TeeCalculator(TeeSpec spec, StitchCalculator stitchCalculator) {
        if (spec == null || stitchCalculator == null) {
            throw new IllegalArgumentException("T-shirt specification and stitch calculator are required.");
        }
        this.spec = spec;
        this.stitchCalculator = stitchCalculator;
    }

    public TeeCalculationResults calculate(PatternRequest request) {
        return calculate(request, spec.defaultOptions());
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
                * spec.neckFactor(request.getFitPreference());

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
                (measurements.shoulderWidthCm() * spec.shoulderWidthGain() - frontPatchCm) / 2;

        int shoulderLengthStitches = widthStitches(shoulderLengthCm, gauge);
        if (shoulderLengthStitches < NECK_INCREASE_ROUNDS) {
            throw new IllegalArgumentException("Shoulder width cannot accommodate the ten neck-shaping rounds.");
        }

        TeeStage afterShoulder = castOnNeck.addBody(
                "After shoulder increases",
                shoulderLengthStitches * 2, // økninger på hver side av begge skuldre,
                shoulderLengthStitches
        );

        double finishedChestCm = measurements.bustCircumferenceCm()
                * spec.bustFactor(request.getFitPreference());

        int targetChestStitches = widthStitches(finishedChestCm, gauge);
        int raglanIncreases = (targetChestStitches - afterShoulder.bodyStitches() - castOnNeck.shoulder()) / CHEST_RAGLAN_LINES;
        if (targetChestStitches < afterShoulder.bodyStitches() + castOnNeck.shoulder()) {
            throw new IllegalArgumentException("Bust target is too small for the preceding shoulder shaping.");
        }

        double armholeLengthCm = measurements.armholeDepthCm() * spec.armholeDepthGain();
        int armholeRows = heightRows(armholeLengthCm, gauge);
        int remainingRows = armholeRows - 2 * raglanIncreases;
        if (remainingRows < 0) {
            throw new IllegalArgumentException(
                    "Armhole depth cannot accommodate the bust-driven raglan rounds. "
                            + "Increase armholeDepthCm in measurements or armholeDepthGain in --spec, "
                            + "reduce bustCircumferenceCm, "
                            + "or review the selected fit and gauge.");
        }
        int armholeIncreases = stitchCalculator.twoRowRepeats(remainingRows);
        TeeStage afterArmInc = afterShoulder.addShoulders(
                "After armhole increases",
                armholeIncreases * 2,
                armholeIncreases
        );

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
        return stitchCalculator.calculateRows(cm, gauge.getStitchesPer10CmHeight());
    }
}