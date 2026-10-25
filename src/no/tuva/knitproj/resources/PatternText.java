package no.tuva.knitproj.resources;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public final class PatternText {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z][A-Za-z0-9]*)\\}");
    private final Properties messages;

    public PatternText() {
        this(BundledProperties.load("/patterns/text-no.properties"));
    }

    public PatternText(Properties messages) {
        if (messages == null) {
            throw new IllegalArgumentException("Pattern text properties are required.");
        }
        this.messages = new Properties();
        this.messages.putAll(messages);
    }

    public String render(String key, Map<String, ?> values) {
        String template = messages.getProperty(key);
        if (template == null) {
            throw new IllegalArgumentException("Missing pattern text key: " + key);
        }
        var matcher = PLACEHOLDER.matcher(template);
        var result = new StringBuilder();
        var used = new java.util.HashSet<String>();
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = values.get(name);
            if (value == null) {
                throw new IllegalArgumentException("Missing placeholder " + name + " for pattern text " + key);
            }
            used.add(name);
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(result);
        if (!used.equals(values.keySet()) || result.indexOf("{") >= 0 || result.indexOf("}") >= 0) {
            throw new IllegalArgumentException("Invalid placeholders or unused values for pattern text " + key);
        }
        return result.toString();
    }
}
