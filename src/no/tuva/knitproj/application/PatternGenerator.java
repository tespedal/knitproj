package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.pattern.tee.TeeCalculator;
import no.tuva.knitproj.pattern.tee.TeeTemplate;
import no.tuva.knitproj.service.StitchCalculator;
import no.tuva.knitproj.pattern.spec.TeeSpec;

public final class PatternGenerator {
    private final TeeSpec spec;
    private final TeeCalculator calculator;
    private final TeeTemplate template;

    public PatternGenerator() {
        this(TeeSpec.bundled());
    }

    public PatternGenerator(TeeSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("T-shirt specification is required.");
        }
        this.spec = spec;
        this.calculator = new TeeCalculator(spec, new StitchCalculator());
        this.template = new TeeTemplate();
    }

    public GeneratedPattern generate(PatternRequest request) {
        return generate(request, spec.defaultOptions());
    }

    public GeneratedPattern generate(PatternRequest request, TeeOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("T-shirt options are required.");
        }
        var calculations = calculator.calculate(request, options);
        var pattern = new PatternResult(template.createSections(request, calculations, options));
        return new GeneratedPattern(calculations, pattern);
    }
}
