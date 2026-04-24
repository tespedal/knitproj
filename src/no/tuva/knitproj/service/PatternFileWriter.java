package no.tuva.knitproj.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PatternFileWriter {
    public void writeToFile(String content, String fileName) throws IOException {
        Path outputPath = Path.of("output", fileName);

        Files.createDirectories(outputPath.getParent());

        Files.writeString(
                outputPath,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
