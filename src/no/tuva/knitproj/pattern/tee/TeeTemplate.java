package no.tuva.knitproj.pattern.tee;

import no.tuva.knitproj.domain.*;
import java.util.List;

public class TeeTemplate {
    public List<PatternSection> createSections(
            PatternRequest request,
            CalculatedPatternValues values
    ) {
        Measurements measurements = request.getMeasurements();
        Gauge gauge = request.getGauge();
        return List.of(
                new PatternSection("Bærestykke", List.of(
                        new PatternParagraph(
                                "Oversikt",
                                "Først strikkes halsen rundt i glattstrikk. Deretter strikkes skulderøkninger med vendepinner, videre strikkes" +
                                        "ermeøkninger og til slutt klassiske raglanøkninger."
                        ),



                        new PatternParagraph(
                                "Opplegg",
                                "Legg opp " + values.castOnNeckStitches() + " masker på rundpinne " + gauge.getNeedleSize()
                        ),
                        new PatternParagraph(
                                "Halskant",
                                "Strikk 6 omganger rett rundt på pinnen "
                        ),
                        new PatternParagraph(
                                "Del inn arbiedet på følgende måte:",
                                values.backPatchStitches() + " til høyre bakstykke " + values.shoulderPatch() + " til høyre skulder " +
                                values.frontPatchStitches() + " til forstykket " + values.shoulderPatch() + " til venstre skulder " +
                                values.backPatchStitches() + " til venstre bastykke. Maskemarkørene følger skuldermaskene slik at det alltid er " +
                                values.shoulderPatch() + " masker mellom maskemarkørene på hver side av skulderen."
                        ),

                        new PatternParagraph(
                                "Informasjonsavsnitt om skulderøkning: ",
                                "Fra retten: Strikk rett frem til maskemarkøren ved skulderen, øk høyre, strikk rett over de " + values.shoulderPatch() +
                                        " skuldermaskene, øk venstre (= 2 masker økt).\n" +
                                        "Fra vrangen: Strikk vrang frem til maskemarkøren ved skulderen, øk vrang høyre, strikk vrang over de " + values.shoulderPatch() +
                                        " skuldermaskene, øk vrang venstre (= 2 masker økt)."
                        ),

                        new PatternParagraph(
                                "Nå strikkes vendepinner for å forme halsen på følgende måte, samtidig som det økes rundt skuldrene: ",
                                "1. pinne (fra retten) : Strikk " + values.backPatchStitches() + " masker rett (bakstykket), strikk 1 skulderøkning fra retten, strikk 3 masker rett, vend.\n" +
                                        "2. pinne (fra vrangen) : Strikk 4 masker vrang, strikk 1 skulderøkning fra vrangen, strikk " +
                                        (values.backPatchStitches() * 2 +2 ) + " masker vrang ( bakstykket), 1 skulderøkning fra vrangen, strikk 3 vrang, vend.\n" +
                                        "3. pinne (fra retten) : Strikk rett til maskemarkøren ved skulderen, strikk 1 skulderøkning fra retten" +
                                        " strikk rett over bakstykket frem til neste maskemarkør, strikk 1 skulderøkning fra retten," +
                                        " strikk rett til 3 masker etter siste vending, vend.\n" +
                                        "4. pinne (fra vrangen) : Strikk vrang til maskemarkøren ved skulderen, 1 skulderøkning fra vrangen,"+
                                        " strikk vrang over bakstykket frem til neste maskemarkør, 1 skulderøkning fra vrangen,"+
                                        " strikk vrang til 3 masker etter siste vending, vend.\n\n" +
                                        "Strikk disse 4 pinnene, og deretter 3. og 4. pinne 3 ganger, det er nå vendt 5 ganger i hver side. Det skal være " + values.afterNeckStitches() +
                                        " masker på pinnen."
                        ),
                        new PatternParagraph(
                                "Strikk til omgangens begynnelse på følgende måte:",
                                "Strikk rett til maskemarkøren, 1 skulderøkning fra retten, strikk til omgangens begynnelse = "+ (values.afterNeckStitches() +2) +" masker."
                        ),
                        new PatternParagraph(
                                "Strikk videre med skulderøkninger uten vendinger: ",
                                "1. omgang: Strikk rett frem til maskemarkøren, 1 skulderøkning, strikk rett over forstykket, 1 skulderøkning, strikk til omgangens begynnelse (= 4 masker økt).\n" +
                                "Strikk denne omgangen totalt " + (values.shoulderLengthStitches()-5) + " ganger = " + values.afterShoulderStitches() + " masker."
                        )
                )),

                new PatternSection("Ermeøkninger:", List.of(
                        new PatternParagraph(
                                "Videre skal det økes til ermer mens maskeantallet på bak og forstykket holdes konstant. Dette gjøres slik: ",
                                "1. omgang: Strikk rett til maskemarkøren, øk venstre, strikk ermemaskene, øk høyre, strikk rett over forstykket, øk venstre, strikk ermemaskene, øk høyre, strikk til starten av omgangen. \n"+
                                        "2. omgang: Strikk rett hele omgangen, uten økninger.\n\n" +
                                        "Strikk disse 2 omgangene totalt " + values.armHoleIncrease() + " ganger = " + values.afterArmholeIncStitches() + " masker."
                        )
                )),

                new PatternSection("Avslutning", List.of(
                        new PatternParagraph(
                                "Fell av",
                                "Fell av i rettmasker og fest alle løse tråder."
                        )
                ))
        );
    }
}
