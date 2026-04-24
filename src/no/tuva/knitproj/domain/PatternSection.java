package no.tuva.knitproj.domain;

import java.util.List;

public class PatternSection {
    private final String title;
    private final List<PatternParagraph> paragraphs;

    public PatternSection(String title, List<PatternParagraph> paragraphs) {
        this.title = title;
        this.paragraphs = paragraphs;
    }

    public String getTitle() {
        return title;
    }

    public List<PatternParagraph> getParagraphs() {
        return paragraphs;
    }
}
