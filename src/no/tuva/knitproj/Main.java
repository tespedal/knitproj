package no.tuva.knitproj;

import no.tuva.knitproj.domain.*;
import no.tuva.knitproj.pattern.tee.TeeCalculator;
import no.tuva.knitproj.pattern.tee.TeeTemplate;
import no.tuva.knitproj.service.*;

import java.io.IOException;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        Measurements measurements = new Measurements();
        Gauge gauge = new Gauge(28, 32,3);
        PatternRequest request = new PatternRequest(
                measurements,
                gauge,
                FitPreference.NORMAL
        );

        TeeCalculator calculator = new TeeCalculator(
                new EaseCalculator(),
                new StitchCalculator()
        );

        CalculatedPatternValues values = calculator.calculate(request);

        // Generate sections from template
        TeeTemplate template = new TeeTemplate();
        List<PatternSection> sections = template.createSections(request, values);

        // Build full result object
        PatternResult result = new PatternResult(
                values.castOnNeckStitches(),
                sections
        );

        PatternTextFormatter formatter = new PatternTextFormatter();
        String output = formatter.format(result);

        PatternFileWriter fileWriter = new PatternFileWriter();

        try{
            fileWriter.writeToFile(output, "tshirt-pattern.txt");

            System.out.println("Pattern written to file.");
        } catch (IOException e) {
            System.out.println("Couldn't write pattern to file.");
            System.out.println(output);
        }

    }
}