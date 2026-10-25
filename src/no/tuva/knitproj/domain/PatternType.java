package no.tuva.knitproj.domain;

public enum PatternType {
    TSHIRT("tshirt-pattern.txt"),
    OVERSIZED_RAGLAN("oversized-raglan-pattern.txt");

    private final String defaultFilename;

    PatternType(String defaultFilename) {
        this.defaultFilename = defaultFilename;
    }

    public String defaultFilename() {
        return defaultFilename;
    }

    public static PatternType parse(String value) {
        return switch (value) {
            case "tshirt" -> TSHIRT;
            case "oversized_raglan", "overzided_raglan" -> OVERSIZED_RAGLAN;
            default -> throw new IllegalArgumentException(
                    "Unknown pattern: " + value + ". Choose tshirt or oversized_raglan (alias overzided_raglan).");
        };
    }
}
