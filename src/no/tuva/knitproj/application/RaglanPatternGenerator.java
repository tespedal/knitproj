package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.pattern.raglan.RaglanCalculator;
import no.tuva.knitproj.pattern.raglan.RaglanTemplate;
import no.tuva.knitproj.pattern.spec.SweaterSpec;

public final class RaglanPatternGenerator {
    private final SweaterSpec spec;
    private final RaglanCalculator calculator;
    private final RaglanTemplate template;

    public RaglanPatternGenerator() {
        this(SweaterSpec.bundled());
    }

    public RaglanPatternGenerator(SweaterSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Sweater specification is required.");
        }
        this.spec = spec;
        this.calculator = new RaglanCalculator(spec);
        this.template = new RaglanTemplate(spec);
    }

    public GeneratedRaglanPattern generate(PatternRequest request) {
        return generate(request, spec.defaultOptions());
    }

    public GeneratedRaglanPattern generate(PatternRequest request, SweaterOptions options) {
        var calculations = calculator.calculate(request, options);
        return new GeneratedRaglanPattern(calculations,
                new PatternResult(template.createSections(request, calculations),
                        "OVERSIZED RAGLANGENSER"));
    }
}
