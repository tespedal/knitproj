package no.tuva.knitproj.domain;

public class PatternParagraph {
    private final String title;
    private final String body;

    public PatternParagraph(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
