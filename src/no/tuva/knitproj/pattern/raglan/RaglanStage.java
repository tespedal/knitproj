package no.tuva.knitproj.pattern.raglan;

public record RaglanStage(int front, int back, int sleeveEach) {
    public RaglanStage {
        if (front <= 0 || back <= 0 || sleeveEach <= 0 || front != back) {
            throw new IllegalArgumentException("Raglan stages require positive, matching front and back counts.");
        }
    }

    public int bodyStitches() {
        return front + back;
    }

    public int totalStitches() {
        return bodyStitches() + 2 * sleeveEach;
    }
}
