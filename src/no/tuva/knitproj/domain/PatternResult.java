package no.tuva.knitproj.domain;

import java.util.List;

public record PatternResult(List<PatternSection> sections, String title) {
    public PatternResult(List<PatternSection> sections) {
        this(sections, "TILPASSET T-SKJORTEOPPSKRIFT");
    }
}
