package no.tuva.knitproj.service;

import no.tuva.knitproj.domain.PatternParagraph;
import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.domain.PatternSection;

public class PatternTextFormatter {
    public String format(PatternResult result) {
        StringBuilder builder = new StringBuilder();

        builder.append("TILPASSET T-SKJORTEOPPSKRIFT\n");
        builder.append("==========================\n\n");

        for (PatternSection section : result.sections()) {
            builder.append(section.getTitle().toUpperCase()).append("\n");
            for (PatternParagraph paragraph : section.getParagraphs()) {
                builder.append(paragraph.getTitle()).append("\n");
                builder.append(paragraph.getBody()).append("\n\n");
            }
        }

        return builder.toString();
    }
}
