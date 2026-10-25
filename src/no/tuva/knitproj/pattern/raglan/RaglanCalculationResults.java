package no.tuva.knitproj.pattern.raglan;

public record RaglanCalculationResults(
        RaglanStage castOn, RaglanStage afterNeck, RaglanStage afterShoulder, RaglanStage afterRaglan,
        int neckRepeats, int shoulderRepeats, int raglanRepeats, int raglanRows,
        int underarmEach, int bodyStitches, int sleeveStitchesEach,
        double neckTargetCm, double chestTargetCm, double raglanDepthCm,
        double actualRaglanDepthCm, double bodyStraightCm, double bodyBelowUnderarmCm,
        double sleeveStraightCm, double sleeveLengthCm, double bodyLengthCm,
        double neckFactor, double bustFactor, double depthFactor) {

    public String explain() {
        return "Oversized raglan: fixed neck=" + neckFactor + ", bust=" + bustFactor
                + ", armhole depth=" + depthFactor + "; fit/gains not applied.\n"
                + stage("Cast on neck", castOn)
                + stage("After neck increases", afterNeck)
                + stage("After shoulder increases", afterShoulder)
                + stage("After raglan increases", afterRaglan)
                + "Repeats: neck=" + neckRepeats + ", shoulder=" + shoulderRepeats
                + ", raglan=" + raglanRepeats + "; raglan rows=" + raglanRows + "\n"
                + "Raglan depth: requested=" + raglanDepthCm + " cm, actual=" + actualRaglanDepthCm + " cm\n"
                + "Separation: body=" + bodyStitches + ", sleeve each=" + sleeveStitchesEach
                + ", underarm cast-on each=" + underarmEach + "\n"
                + "Targets: neck=" + neckTargetCm + " cm, chest=" + chestTargetCm
                + " cm; symmetric rounding applied. No knit-through approval.\n";
    }

    private static String stage(String label, RaglanStage stage) {
        return label + ": front=" + stage.front() + ", back=" + stage.back()
                + ", sleeve each=" + stage.sleeveEach() + ", body=" + stage.bodyStitches()
                + ", total=" + stage.totalStitches() + "\n";
    }
}
