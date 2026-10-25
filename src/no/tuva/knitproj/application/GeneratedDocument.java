package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternResult;

public record GeneratedDocument(PatternResult pattern, String trace) {
    public GeneratedDocument {
        if (pattern == null || trace == null) {
            throw new IllegalArgumentException("Generated pattern and calculation trace are required.");
        }
    }
}
