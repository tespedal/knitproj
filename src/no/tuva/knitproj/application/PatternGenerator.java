package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.domain.TeeOptions;
import no.tuva.knitproj.pattern.tee.TeeCalculator;
import no.tuva.knitproj.pattern.tee.TeeTemplate;
import no.tuva.knitproj.service.EaseCalculator;
import no.tuva.knitproj.service.StitchCalculator;

public final class PatternGenerator {
    public GeneratedPattern generate(PatternRequest request) {
        return generate(request, new TeeOptions());
    }

    public GeneratedPattern generate(PatternRequest request, TeeOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("T-shirt options are required.");
        }
        var calculations = new TeeCalculator(new EaseCalculator(), new StitchCalculator())
                .calculate(request);
        var pattern = new PatternResult(new TeeTemplate().createSections(request, calculations, options));
        return new GeneratedPattern(calculations, pattern);
    }
}
