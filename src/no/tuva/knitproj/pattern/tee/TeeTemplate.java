package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;

import java.util.List;

public class TeeTemplate {

    public List<PatternSection> createSections(
            PatternRequest request,
            TeeCalculationResults values
    ) {
        Gauge gauge = request.getGauge();

        TeeStage castOn = values.castOnNeck();
        TeeStage afterNeck = values.afterNeck();
        TeeStage afterShoulder = values.afterShoulder();
        TeeStage afterArmInc = values.afterArmInc();
        TeeStage afterRaglan = values.afterRaglan();
        TeeStage afterArmHole = values.afterArmHole();

        return List.of(
                new PatternSection("Bærestykke", List.of(
                        new PatternParagraph(
                                "Oversikt",
                                "Først strikkes halsen rundt i glattstrikk. Deretter strikkes skulderøkninger med vendepinner, " +
                                        "videre strikkes ermeøkninger og til slutt klassiske raglanøkninger."
                        ),

                        new PatternParagraph(
                                "Opplegg",
                                "Legg opp " + castOn.totalStitches() + " masker på rundpinne " + gauge.getNeedleSize()
                        ),

                        new PatternParagraph(
                                "Halskant",
                                "Strikk 6 omganger rett rundt på pinnen."
                        ),

                        new PatternParagraph(
                                "Del inn arbeidet på følgende måte:",
                                castOn.backPatchStitches() + " masker til høyre bakstykke, " +
                                        castOn.shoulder() + " masker til høyre skulder, " +
                                        castOn.frontPatchStitches() + " masker til forstykket, " +
                                        castOn.shoulder() + " masker til venstre skulder, " +
                                        castOn.backPatchStitches() + " masker til venstre bakstykke. " +
                                        "Maskemarkørene følger skuldermaskene slik at det alltid er " +
                                        castOn.shoulder() + " masker mellom maskemarkørene på hver side av skulderen."
                        ),

                        new PatternParagraph(
                                "Informasjonsavsnitt om skulderøkning:",
                                "Fra retten: Strikk rett frem til maskemarkøren ved skulderen, øk høyre, strikk rett over de " +
                                        castOn.shoulder() + " skuldermaskene, øk venstre (= 2 masker økt).\n" +
                                        "Fra vrangen: Strikk vrang frem til maskemarkøren ved skulderen, øk vrang høyre, strikk vrang over de " +
                                        castOn.shoulder() + " skuldermaskene, øk vrang venstre (= 2 masker økt)."
                        ),

                        new PatternParagraph(
                                "Nå strikkes vendepinner for å forme halsen på følgende måte, samtidig som det økes rundt skuldrene:",
                                "1. pinne (fra retten): Strikk " + castOn.backPatchStitches() +
                                        " masker rett (bakstykket), strikk 1 skulderøkning fra retten, strikk 3 masker rett, vend.\n" +

                                        "2. pinne (fra vrangen): Strikk 4 masker vrang, strikk 1 skulderøkning fra vrangen, strikk " +
                                        (castOn.backPatchStitches() * 2 + 2) +
                                        " masker vrang (bakstykket), 1 skulderøkning fra vrangen, strikk 3 vrang, vend.\n" +

                                        "3. pinne (fra retten): Strikk rett til maskemarkøren ved skulderen, strikk 1 skulderøkning fra retten, " +
                                        "strikk rett over bakstykket frem til neste maskemarkør, strikk 1 skulderøkning fra retten, " +
                                        "strikk rett til 3 masker etter siste vending, vend.\n" +

                                        "4. pinne (fra vrangen): Strikk vrang til maskemarkøren ved skulderen, 1 skulderøkning fra vrangen, " +
                                        "strikk vrang over bakstykket frem til neste maskemarkør, 1 skulderøkning fra vrangen, " +
                                        "strikk vrang til 3 masker etter siste vending, vend.\n\n" +

                                        "Strikk disse 4 pinnene, og deretter 3. og 4. pinne 3 ganger til. " +
                                        "Det er nå vendt 5 ganger i hver side. Strikk tilbake til omgangens start på følgende måte:\n\n" +

                                        "Strikk rett til maskemarkøren ved skulderen, strikk 1 skulderøkning fra retten, " +
                                        "strikk til maskemarkøren ved omgangens begynnelse (= " +  afterNeck.totalStitches() + " masker)."
                        ),

                        new PatternParagraph(
                                "Strikk videre rundt på rundpinne med skulderøkninger uten vendinger:",
                                "1. omgang: Strikk rett frem til maskemarkøren, 1 skulderøkning, strikk rett over forstykket, " +
                                        "1 skulderøkning, strikk til omgangens begynnelse (= 4 masker økt).\n" +
                                        "Strikk denne omgangen totalt " + (afterShoulder.nrIncreases() - 10) +
                                        " ganger = " + afterShoulder.totalStitches() + " masker."
                        )
                )),

                new PatternSection("Ermeøkninger", List.of(
                        new PatternParagraph(
                                "Arbeidet er nå delt inn på følgende måte:",
                                afterShoulder.backPatchStitches() + " masker til høyre bakstykke, " +
                                        afterShoulder.shoulder() + " masker til høyre skulder, " +
                                        afterShoulder.frontPatchStitches() + " masker til forstykket, " +
                                        afterShoulder.shoulder() + " masker til venstre skulder, " +
                                        afterShoulder.backPatchStitches() + " masker til venstre bakstykke. " +
                                        "Videre skal maskemarkørene følge for- og bakstykket slik at det alltid er " +
                                        afterShoulder.frontPatchStitches() + " masker mellom maskemarkørene på hver side av for- og bakstykket."
                        ),
                        new PatternParagraph(
                                "Videre skal det økes til ermer mens maskeantallet på bak- og forstykket holdes konstant. Dette gjøres slik:",
                                "1. omgang: Strikk rett til maskemarkøren, øk venstre, strikk ermemaskene, øk høyre, " +
                                        "strikk rett over forstykket, øk venstre, strikk ermemaskene, øk høyre, " +
                                        "strikk til starten av omgangen.\n" +
                                        "2. omgang: Strikk rett hele omgangen, uten økninger.\n\n" +
                                        "Strikk disse 2 omgangene totalt " + afterArmInc.nrIncreases() +
                                        " ganger = " + afterArmInc.totalStitches() + " masker."
                        )
                )),

                new PatternSection("Raglanøkninger", List.of(
                        new PatternParagraph(
                                "Videre skal det strikkes raglanøkninger på hver 2. omgang på følgende måte:",
                                "1. omgang: Strikk rett til 1 maske før maskemarkør, øk høyre, strikk 2 r, øk venstre. " +
                                        "Gjenta økningen for alle 4 maskemarkører (= 8 masker økt).\n" +
                                        "2. omgang: Strikk rett.\n\n" +
                                        "Strikk disse 2 omgangene totalt " + afterRaglan.nrIncreases() +
                                        " ganger = " + afterRaglan.totalStitches() + " masker."
                        )
                )),

                new PatternSection("Bol", List.of(
                        new PatternParagraph(
                                "Videre skal bolen og ermene strikkes videre hver for seg. Denne inndelingen skjer på følgende måte: ",
                                "Begynn ved omgangens begynnelse (midt bak). Strikk " +
                                        afterRaglan.backPatchStitches() + " rett (høyre bakstykke), sett de neste " +
                                        afterRaglan.shoulder() + " maskene på en hviletråd. Legg opp " +
                                        afterNeck.shoulder() + " nye masker på pinnen. Strikk " +
                                        afterRaglan.frontPatchStitches() + " rett, sett de neste " +
                                        afterRaglan.shoulder() + " maskene på en hviletråd. Legg opp " +
                                        afterNeck.shoulder() + " nye masker på pinnen, strikk " +
                                        afterRaglan.backPatchStitches() + " til enden av omgangen. Det er nå " +
                                        afterArmHole.totalStitches() + " masker på pinnen."

                        ),
                        new PatternParagraph(
                                "Strikk rundt i glattstrikk til tskjorten måler " + values.backLength() + " cm.",
                                "Fell av maskene og fest tråden."

                        )
                )),
                new PatternSection("Ermer", List.of(
                        new PatternParagraph(
                                "Ermene strikkes rundt i glattstrikk. ",
                                "Sett de " + afterRaglan.shoulder() + " maskene tilbake på pinne " +
                                        gauge.getNeedleSize() + ". Plukk opp " +
                                        afterArmHole.shoulder() + " masker i maskene du la opp under ermet ( = " +
                                        (afterRaglan.shoulder() + afterArmHole.shoulder()) + " masker). Sett en maskemarkør i midten av de nyopplukkede maskene. "

                        ),
                        new PatternParagraph(
                                "Strikk rundt i glattstrikk til ermet måler 10 cm.",
                                "Fell av maskene og fest tråden. Strikk det andre ermet på samme måte."

                        )
                ))
        );
    }
}