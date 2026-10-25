package no.tuva.knitproj.pattern.spec;

import java.util.Properties;
import java.util.Set;

final class SpecValues {
    private SpecValues() {}

    static void keys(Properties properties, Set<String> allowed) {
        if (properties == null) {
            throw new IllegalArgumentException("Pattern specification properties are required.");
        }
        for (String key : properties.stringPropertyNames()) {
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException("Unknown pattern specification key: " + key);
            }
        }
    }

    static double number(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing pattern specification key: " + key);
        }
        try {
            return positive(key, Double.parseDouble(value.strip()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Pattern specification " + key + " must be a finite positive number.", e);
        }
    }

    static double positive(String key, double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Pattern specification " + key + " must be finite and positive.");
        }
        return value;
    }

    static int whole(Properties properties, String key) {
        double value = number(properties, key);
        if (value != Math.rint(value) || value > Integer.MAX_VALUE / 16) {
            throw new IllegalArgumentException("Pattern specification " + key + " must be a positive whole number within calculation range.");
        }
        return (int) value;
    }
}
