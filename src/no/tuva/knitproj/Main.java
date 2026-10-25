package no.tuva.knitproj;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import no.tuva.knitproj.application.GeneratedDocument;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.application.RaglanPatternGenerator;
import no.tuva.knitproj.cli.CliArguments;
import no.tuva.knitproj.cli.CliHelp;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.cli.Utf8Properties;
import no.tuva.knitproj.pattern.spec.TeeSpec;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.service.PatternFileWriter;
import no.tuva.knitproj.service.PatternTextFormatter;

public class Main {
    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            var arguments = CliArguments.parse(args);
            if (arguments.help()) {
                out.print(CliHelp.text());
                return 0;
            }
            Properties input = arguments.input() == null ? new Properties() : Utf8Properties.read(arguments.input());
            GeneratedDocument generated;
            String scaling;
            switch (arguments.type()) {
                case TSHIRT -> {
                    var spec = arguments.spec() == null ? TeeSpec.bundled()
                            : TeeSpec.fromProperties(Utf8Properties.read(arguments.spec()));
                    var configuration = PatternInput.requestFromProperties(input, spec);
                    var result = new PatternGenerator(spec).generate(configuration.request(), configuration.options());
                    generated = new GeneratedDocument(result.pattern(), result.calculations().explain());
                    scaling = "neckFactor=" + spec.neckFactor(configuration.request().getFitPreference())
                            + ", bustFactor=" + spec.bustFactor(configuration.request().getFitPreference())
                            + ", armholeDepthGain=" + spec.armholeDepthGain()
                            + ", shoulderWidthGain=" + spec.shoulderWidthGain();
                }
                case OVERSIZED_RAGLAN -> {
                    var spec = arguments.spec() == null ? SweaterSpec.bundled()
                            : SweaterSpec.fromProperties(Utf8Properties.read(arguments.spec()));
                    var configuration = PatternInput.requestFromProperties(input, spec);
                    var result = new RaglanPatternGenerator(spec).generate(configuration.request(), configuration.options());
                    generated = new GeneratedDocument(result.pattern(), result.calculations().explain());
                    scaling = "neckFactor=" + spec.neckFactor() + ", bustFactor=" + spec.bustFactor()
                            + ", depthFactor=" + spec.depthFactor();
                }
                default -> throw new IllegalArgumentException("Unsupported pattern.");
            }
            String text = new PatternTextFormatter().format(generated.pattern());
            new PatternFileWriter().writeToFile(text, arguments.output());
            out.println("Pattern written to " + arguments.output());
            if (arguments.explain()) {
                out.println("Specification: " + (arguments.spec() == null ? "bundled " + arguments.type()
                        : "external " + arguments.spec()));
                out.println("Scaling: " + scaling);
                out.print(generated.trace());
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