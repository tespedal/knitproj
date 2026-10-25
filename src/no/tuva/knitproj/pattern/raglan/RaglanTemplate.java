package no.tuva.knitproj.pattern.raglan;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import no.tuva.knitproj.resources.PatternText;
import no.tuva.knitproj.pattern.spec.SweaterSpec;
import no.tuva.knitproj.domain.PatternParagraph;
import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.PatternSection;

public final class RaglanTemplate {
    private final PatternText text = new PatternText();
    private final SweaterSpec spec;

    public RaglanTemplate() {
        this(SweaterSpec.bundled());
    }

    public RaglanTemplate(SweaterSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Sweater specification is required.");
        }
        this.spec = spec;
    }

    public List<PatternSection> createSections(PatternRequest request, RaglanCalculationResults c) {
        return List.of(
                section("OVENFRA OG NED",
                        "Forutsetninger", "Rett, romslig genser uten innsving, ermefellinger eller forkortede pinner. "
                                + "Dette er en beregnet oppskrift uten strikkefaglig godkjenning eller prøvestrikking. "
                                + "Kontroller mål og prøvelapp før du begynner. Fast vidde: hals × " + factor(spec.neckFactor())
                                + ", bryst × " + factor(spec.bustFactor()) + "; "
                                + "raglandybde × " + factor(spec.depthFactor()) + ". Fit og T-skjortens gain-faktorer brukes ikke. "
                                + "Ha garn til en langermet genser, rundpinne og settpinner/magic loop, fire markører og maskeholdere. "
                                + "Garnmengde og støttet størrelsesområde er ikke fastsatt."),
                section("STRIKKEFASTHET",
                        "Prøvelapp", text.render("raglan.gauge", Map.of(
                                "stitches", request.getGauge().getStitchesPer10Cm(),
                                "rows", request.getGauge().getStitchesPer10CmHeight(),
                                "needle", request.getGauge().getNeedleSize()))),
                section("HALSKANT OG HALSØKNINGER",
                        "Opplegg", text.render("common.castOn", Map.of("stitches", c.castOn().totalStitches()))
                                + " elastisk. Samle til en ring uten å vri. Strikk " + length(spec.neckbandCm()) + " cm vrangbord, 1 rett, 1 vrang. "
                                + "Sett omgangsstart før forstykket og markører mellom alle fire felter: forstykke "
                                + c.castOn().front() + ", erme " + c.castOn().sleeveEach() + ", bakstykke "
                                + c.castOn().back() + ", erme " + c.castOn().sleeveEach()
                                + " masker. Markørene er mellom masker; ingen ekstra raglanmasker.",
                        "Halsøkninger", bodyIncrease(c.neckRepeats(), c.afterNeck())
                                + " Ingen masker settes tilbake eller telles på nytt etter dette trinnet."),
                section("SKULDERØKNINGER",
                        "Resterende brystvidde", c.shoulderRepeats() == 0
                                ? "Ingen skulderøkninger trengs. Fortsett direkte til raglan."
                                : bodyIncrease(c.shoulderRepeats(), c.afterShoulder())),
                section("RAGLANØKNINGER",
                        "Bærestykke", c.raglanRepeats() == 0
                                ? "Beregnet raglanlengde avrundes til 0 omganger. Hopp over raglanøkningene."
                                : text.render("raglan.increases", Map.of("repeats", c.raglanRepeats(),
                                        "rows", c.raglanRows(), "depth", cm(c.actualRaglanDepthCm()),
                                        "counts", counts(c.afterRaglan())))),
                section("DELING TIL BOL OG ERMER",
                        "Under ermet", text.render("raglan.separation", Map.of("sleeve", c.afterRaglan().sleeveEach(),
                                "underarm", c.underarmEach(), "body", c.bodyStitches()))),
                section("BOL",
                        "Lengde og kant", text.render("raglan.body", Map.of("straight", cm(c.bodyStraightCm()),
                                "hem", length(spec.hemCm()), "below", cm(c.bodyBelowUnderarmCm()),
                                "length", cm(c.bodyLengthCm())))),
                section("ERMER — STRIKK TO LIKE",
                        "Rette, vide ermer", text.render("raglan.sleeves", Map.of(
                                "held", c.afterRaglan().sleeveEach(), "underarm", c.underarmEach(),
                                "total", c.sleeveStitchesEach(), "straight", cm(c.sleeveStraightCm()),
                                "cuff", length(spec.cuffCm()), "length", cm(c.sleeveLengthCm())))),
                section("MONTERING",
                        "Avslutning", text.render("raglan.finishing", Map.of())));
    }

    private String bodyIncrease(int repeats, RaglanStage result) {
        return text.render("raglan.bodyIncrease", Map.of("repeats", repeats, "counts", counts(result)));
    }

    private String counts(RaglanStage stage) {
        return text.render("raglan.counts", Map.of("front", stage.front(), "back", stage.back(),
                "sleeve", stage.sleeveEach(), "total", stage.totalStitches()));
    }

    private static String length(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String factor(double value) {
        return length(value).replace('.', ',');
    }

    private static String cm(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static PatternSection section(String title, String... paragraphs) {
        var result = new java.util.ArrayList<PatternParagraph>();
        for (int i = 0; i < paragraphs.length; i += 2) {
            result.add(new PatternParagraph(paragraphs[i], paragraphs[i + 1]));
        }
        return new PatternSection(title, List.copyOf(result));
    }
}
