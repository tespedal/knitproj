package no.tuva.knitproj.cli;

import java.nio.file.Path;
import no.tuva.knitproj.domain.PatternType;

public record CliArguments(PatternType type, Path input, Path spec, Path output, boolean explain, boolean help) {
    public CliArguments {
        if (type == null) {
            throw new IllegalArgumentException("Pattern type is required.");
        }
        if (output == null) {
            output = Path.of("output", type.defaultFilename());
        }
    }

    public static CliArguments parse(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Arguments are required.");
        }
        Path input = null;
        Path spec = null;
        Path output = null;
        PatternType type = PatternType.TSHIRT;
        boolean explain = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException("Argument at position " + i + " is required.");
            }
            switch (args[i]) {
                case "--help" -> {
                    return new CliArguments(type, input, spec, output, explain, true);
                }
                case "--pattern" -> {
                    requireValue(args, i, "selector");
                    type = PatternType.parse(args[++i]);
                }
                case "--input", "--output", "--spec" -> {
                    String flag = args[i];
                    requireValue(args, i, "path");
                    Path path = Path.of(args[++i]);
                    if (flag.equals("--input")) {
                        input = path;
                    } else if (flag.equals("--spec")) {
                        spec = path;
                    } else {
                        output = path;
                    }
                }
                case "--explain" -> explain = true;
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        return new CliArguments(type, input, spec, output, explain, false);
    }

    private static void requireValue(String[] args, int position, String label) {
        if (position + 1 == args.length || args[position + 1] == null
                || args[position + 1].startsWith("--")) {
            throw new IllegalArgumentException("Missing " + label + " after " + args[position]);
        }
    }
}
