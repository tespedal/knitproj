package no.tuva.knitproj.pattern.tee;

public record TeeStage(String name, int halfBody, int shoulder, int nrIncreases) {

    public TeeStage {
        if (halfBody % 2 != 0) {
            halfBody +=1;
            shoulder -=1;
        }
    }

    public int frontPatchStitches() {
        return halfBody;
    }

    public int backPatchStitches() {
        return halfBody / 2;
    }

    public int totalStitches() {
        return 2 * halfBody + 2 * shoulder;
    }

    public int bodyStitches() {
        return 2 * halfBody;
    }

    TeeStage addBody(String newName, int stitchesPerHalfBody, int nrIncreases) {
        return new TeeStage(
                newName,
                halfBody + stitchesPerHalfBody,
                shoulder,
                nrIncreases
        );
    }


    TeeStage addShoulders(String newName, int stitchesPerShoulder, int nrIncreases) {
        return new TeeStage(
                newName,
                halfBody,
                shoulder + stitchesPerShoulder,
                nrIncreases
        );
    }

    TeeStage addBodyAndShoulders(String newName, int stitchesPerPart, int nrIncreases) {
        return new TeeStage(
                newName,
                halfBody + stitchesPerPart,
                shoulder + stitchesPerPart,
                nrIncreases
        );
    }
}