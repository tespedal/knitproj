package no.tuva.knitproj;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.service.PatternFileWriter;
import no.tuva.knitproj.service.PatternTextFormatter;

public class Main {
    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            Path input = null;
            Path output = Path.of("output", "tshirt-pattern.txt");
            boolean explain = false;
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--help" -> {
                        out.println("Usage: knitproj [--input measurements.properties] [--output pattern.txt] [--explain]");
                        return 0;
                    }
                    case "--input", "--output" -> {
                        String flag = args[i];
                        if (++i == args.length || args[i].startsWith("--")) {
                            throw new IllegalArgumentException("Missing path after " + flag);
                        }
                        if (flag.equals("--input")) {
                            input = Path.of(args[i]);
                        } else {
                            output = Path.of(args[i]);
                        }
                    }
                    case "--explain" -> explain = true;
                    default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
                }
            }
            PatternInput configuration = input == null ? PatternInput.defaults() : PatternInput.read(input);
            var generated = new PatternGenerator().generate(configuration.request(), configuration.options());
            String text = new PatternTextFormatter().format(generated.pattern());
            new PatternFileWriter().writeToFile(text, output);
            out.println("Pattern written to " + output);
            if (explain) {
                out.print(generated.calculations().explain());
            }
            return 0;
        } catch (IllegalArgumentException e) {
            err.println("Invalid request: " + e.getMessage());
            return 2;
        } catch (IOException e) {
            err.println("Could not read input or write pattern: " + e.getMessage());
            return 1;
        }
    }
}