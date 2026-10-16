package no.tuva.knitproj.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PatternFileWriter {
    public void writeToFile(String content, String fileName) throws IOException {
        writeToFile(content, Path.of("output", fileName));
    }

    public void writeToFile(String content, Path outputPath) throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                outputPath,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
