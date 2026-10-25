package no.tuva.knitproj.pattern.tee;

public record TeeCalculationResults(
        TeeStage castOnNeck,
        TeeStage afterNeck,
        TeeStage afterShoulder,
        TeeStage afterArmInc,
        TeeStage afterRaglan,
        TeeStage afterArmHole,
        double backLength
) {
    public int shoulderRoundsAfterNeck() {
        return afterShoulder.nrIncreases() - TeeCalculator.NECK_INCREASE_ROUNDS;
    }

    public int neckReturnBackStitches() {
        return castOnNeck.backPatchStitches() * 2 + 2;
    }

    public int sleeveStitches() {
        return afterRaglan.shoulder() + afterArmHole.shoulder();
    }

    public int armholeShapingRows() {
        return 2 * (afterArmInc.nrIncreases() + afterRaglan.nrIncreases());
    }

    public String explain() {
        StringBuilder result = new StringBuilder("Calculation trace (not knitting approval)\n");
        for (TeeStage stage : new TeeStage[]{castOnNeck, afterNeck, afterShoulder,
                afterArmInc, afterRaglan, afterArmHole}) {
            result.append(stage.name()).append(": body=").append(stage.bodyStitches())
                    .append(", shoulder/underarm per side=").append(stage.shoulder())
                    .append(", total=").append(stage.totalStitches())
                    .append(", repeats=").append(stage.nrIncreases()).append('\n');
        }
        result.append("Armhole shaping rows (sleeve-only + raglan, after shoulder shaping)=")
                .append(armholeShapingRows()).append('\n');
        return result.toString();
    }
}