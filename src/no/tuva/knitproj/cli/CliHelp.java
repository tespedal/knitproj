package no.tuva.knitproj.cli;

public final class CliHelp {
    private CliHelp() {
    }

    public static String text() {
        String newline = System.lineSeparator();
        return String.join(newline,
                "Usage: knitproj [--pattern tshirt|oversized_raglan] [--input measurements.properties] [--spec construction.properties] [--output pattern.txt] [--explain]",
                "Alias: overzided_raglan. Default: tshirt -> output/tshirt-pattern.txt.",
                "Sweater default: output/oversized-raglan-pattern.txt; sleeve length is measured from underarm.",
                "--input: wearer measurements, gauge, fit and desired lengths only.",
                "--spec: complete UTF-8 specification replacing the selected bundled construction; no overlays.",
                "Edit external specifications and rerun without rebuilding. Bundled changes require a rebuild.",
                "--explain: specification source, effective scaling and stage counts; does not change pattern text.",
                "--help: show help without reading input or specification files.") + newline;
    }
}
