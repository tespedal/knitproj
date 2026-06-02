package no.tuva.knitproj.pattern.tee;

public record TeeCalculationResults(
        TeeStage castOnNeck,
        TeeStage afterNeck,
        TeeStage afterShoulder,
        TeeStage afterArmInc,
        TeeStage afterRaglan,
        TeeStage afterArmHole,
        double backLength
) {}