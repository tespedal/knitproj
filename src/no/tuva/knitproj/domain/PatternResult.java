package no.tuva.knitproj.domain;

import java.util.List;
public class PatternResult {
    private final int castOnStitches;
    private final List<PatternSection> sections;

    public PatternResult(
            int castOnStitches,
            List<PatternSection> sections
    ) {
        this.castOnStitches = castOnStitches;
        this.sections = sections;
    }

    public int getCastOnStitches() {
        return castOnStitches;
    }


    public List<PatternSection> getSections() {
        return sections;
    }
}
