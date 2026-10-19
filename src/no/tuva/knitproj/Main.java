package no.tuva.knitproj;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.service.PatternFileWriter;
import no.tuva.knitproj.service.PatternTextFormatter;
import no.tuva.knitproj.web.PatternWebServer;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--ui")) {
            try (var server = new PatternWebServer()) {
                Thread shutdown = new Thread(server::close);
                Runtime.getRuntime().addShutdownHook(shutdown);
                System.out.println("Åpne " + server.uri() + " i nettleseren. Avslutt med Ctrl+C.");
                try {
                    new CountDownLatch(1).await();
                } finally {
                    Runtime.getRuntime().removeShutdownHook(shutdown);
                }
            } catch (IOException e) {
                System.err.println("Kunne ikke starte nettlesergrensesnittet: " + e.getMessage());
                System.exit(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }
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
                        out.println("       knitproj --ui  (local browser interface)");
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