package no.tuva.knitproj.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Utf8Properties {
    private Utf8Properties() {}

    public static Properties read(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Properties path is required.");
        }
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
