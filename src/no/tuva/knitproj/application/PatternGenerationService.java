package no.tuva.knitproj.application;

public final class PatternGenerationService {
    private final PatternGenerator teeGenerator;
    private final RaglanPatternGenerator raglanGenerator;

    public PatternGenerationService() {
        this(new PatternGenerator(), new RaglanPatternGenerator());
    }

    public PatternGenerationService(PatternGenerator teeGenerator, RaglanPatternGenerator raglanGenerator) {
        if (teeGenerator == null || raglanGenerator == null) {
            throw new IllegalArgumentException("T-shirt and sweater generators are required.");
        }
        this.teeGenerator = teeGenerator;
        this.raglanGenerator = raglanGenerator;
    }

    public GeneratedDocument generate(GenerationRequest selection) {
        if (selection == null) {
            throw new IllegalArgumentException("Generation request is required.");
        }
        if (selection instanceof GenerationRequest.Tee tee) {
            var generated = teeGenerator.generate(tee.request(), tee.options());
            return new GeneratedDocument(generated.pattern(), generated.calculations().explain());
        }
        var raglan = (GenerationRequest.Raglan) selection;
        var generated = raglanGenerator.generate(raglan.request(), raglan.options());
        return new GeneratedDocument(generated.pattern(), generated.calculations().explain());
    }
}
