package no.tuva.knitproj.domain;

public record CalculatedPatternValues(int castOnNeckStitches,
                                      int backPatchStitches,
                                      int frontPatchStitches,
                                      int shoulderPatch,
                                      int afterNeckStitches,
                                      int shoulderLengthStitches,
                                      int afterShoulderStitches,
                                      int armHoleIncrease,
                                      int afterArmholeIncStitches,
                                      double finishedChestCm) {

}
