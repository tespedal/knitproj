package no.tuva.knitproj.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class BundledProperties {
    private BundledProperties() {}

    public static Properties load(String path) {
        var stream = BundledProperties.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalArgumentException("Missing bundled resource: " + path);
        }
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var properties = new Properties();
            properties.load(reader);
            return properties;
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Cannot read bundled UTF-8 properties " + path + ": " + e.getMessage(), e);
        }
    }
}
