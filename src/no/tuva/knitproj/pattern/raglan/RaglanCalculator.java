package no.tuva.knitproj.pattern.raglan;

import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.service.StitchCalculator;
import no.tuva.knitproj.pattern.spec.SweaterSpec;

public final class RaglanCalculator {
    private final SweaterSpec spec;
    private final StitchCalculator stitches = new StitchCalculator();

    public RaglanCalculator() {
        this(SweaterSpec.bundled());
    }

    public RaglanCalculator(SweaterSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Sweater specification is required.");
        }
        this.spec = spec;
    }

    public RaglanCalculationResults calculate(PatternRequest request, SweaterOptions options) {
        if (request == null || options == null) {
            throw new IllegalArgumentException("Sweater request and options are required.");
        }
        var m = request.getMeasurements();
        var gauge = request.getGauge();
        int horizontalGauge = gauge.getStitchesPer10Cm();
        int verticalGauge = gauge.getStitchesPer10CmHeight();
        if (options.sleeveLengthCm() <= spec.cuffCm()) {
            throw new IllegalArgumentException("Sweater sleeveLengthCm must exceed cuffCm=" + spec.cuffCm());
        }
        double neckCm = m.neckCircumferenceCm() * spec.neckFactor();
        double chestCm = m.bustCircumferenceCm() * spec.bustFactor();
        double depthCm = m.armholeDepthCm() * spec.depthFactor();
        int neck = stitches.calculateSymmetricStitches(neckCm, horizontalGauge);
        int sleevePatch = stitches.calculateStitches(m.shoulderAcrossCm(), horizontalGauge);
        int front = (neck - 2 * sleevePatch) / 2;
        if (front <= 0 || sleevePatch <= 0) {
            throw new IllegalArgumentException(
                    "Neck must accommodate two positive shoulderAcrossCm sleeve patches; enlarge neck or reduce patches.");
        }
        var castOn = new RaglanStage(front, front, sleevePatch);
        var afterNeck = new RaglanStage(front + 2 * spec.neckRepeats(), front + 2 * spec.neckRepeats(), sleevePatch);
        int rows = stitches.calculateRows(depthCm, verticalGauge);
        int raglanRepeats = stitches.twoRowRepeats(rows);
        int chest = stitches.calculateSymmetricStitches(chestCm, horizontalGauge);
        int residual = chest - 2 * sleevePatch - afterNeck.bodyStitches() - 4 * raglanRepeats;
        if (residual < 0) {
            throw new IllegalArgumentException(
                    "Bust target is too small for neck and depth-driven raglan shaping including underarms. "
                            + "Increase bustCircumferenceCm, reduce armholeDepthCm, or review gauge/neck/patch measurements.");
        }
        int shoulderRepeats = residual / 4;
        var afterShoulder = new RaglanStage(afterNeck.front() + 2 * shoulderRepeats,
                afterNeck.back() + 2 * shoulderRepeats, sleevePatch);
        var afterRaglan = new RaglanStage(afterShoulder.front() + 2 * raglanRepeats,
                afterShoulder.back() + 2 * raglanRepeats, sleevePatch + 2 * raglanRepeats);
        double yokeCm = (2.0 * spec.neckRepeats() + 2.0 * shoulderRepeats + rows) * 10 / verticalGauge;
        double belowUnderarmCm = m.bodyLengthCm() - spec.neckbandCm() - yokeCm;
        if (!Double.isFinite(belowUnderarmCm) || belowUnderarmCm <= spec.hemCm()) {
            throw new IllegalArgumentException(
                    "Bust target requires too many shoulder rounds for bodyLengthCm including neckbandCm="
                            + spec.neckbandCm() + " and hemCm=" + spec.hemCm() + ". "
                            + "Increase bodyLengthCm or reduce bustCircumferenceCm; review gauge.");
        }
        return new RaglanCalculationResults(castOn, afterNeck, afterShoulder, afterRaglan,
                spec.neckRepeats(), shoulderRepeats, raglanRepeats, rows, sleevePatch,
                afterRaglan.bodyStitches() + 2 * sleevePatch,
                afterRaglan.sleeveEach() + sleevePatch, neckCm, chestCm, depthCm,
                rows * 10.0 / verticalGauge, belowUnderarmCm - spec.hemCm(), belowUnderarmCm,
                options.sleeveLengthCm() - spec.cuffCm(), options.sleeveLengthCm(), m.bodyLengthCm(),
                spec.neckFactor(), spec.bustFactor(), spec.depthFactor());
    }
}
