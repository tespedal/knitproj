package no.tuva.knitproj.web;

import java.util.Properties;
import no.tuva.knitproj.cli.PatternInput;

final class PatternPage {
    private PatternPage() {}

    static String render(Properties values, String token, String pattern, String error) {
        var defaults = PatternInput.defaults();
        var m = defaults.request().getMeasurements();
        var gauge = defaults.request().getGauge();
        var options = defaults.options();
        var html = new StringBuilder("""
                <!doctype html>
                <html lang="nb">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Knitproj – din strikkeoppskrift</title>
                  <link rel="stylesheet" href="/style.css">
                  <script src="/app.js" defer></script>
                </head>
                <body>
                <header><span class="brand">KNITPROJ / STRIKKEVERKSTED</span>
                <h1>En T-skjorte, på dine mål.</h1>
                <p>Tilpass målene, velg passform og lag en norsk strikkeoppskrift.</p></header>
                <main>
                """);
        if (!error.isEmpty()) {
            html.append("<div class=\"error\" role=\"alert\"><strong>Kontroller verdiene.</strong> ")
                    .append(escape(error)).append("</div>");
        }
        html.append("<form method=\"post\" action=\"/generate\"><input type=\"hidden\" name=\"token\" value=\"")
                .append(escape(token)).append("\"><div class=\"layout\"><div class=\"settings\">");
        html.append("<fieldset><legend>01 / Kroppsmål</legend><p>Alle mål er i centimeter.</p><div class=\"fields\">");
        field(html, values, "neckCircumferenceCm", "Halsomkrets", "cm", m.neckCircumferenceCm(), false);
        field(html, values, "shoulderWidthCm", "Full skulderbredde", "cm", m.shoulderWidthCm(), false);
        field(html, values, "shoulderAcrossCm", "Bredde på én skulderdel", "cm", m.shoulderAcrossCm(), false);
        field(html, values, "bustCircumferenceCm", "Brystomkrets", "cm", m.bustCircumferenceCm(), false);
        field(html, values, "backLengthCm", "Rygglengde (stoppmål for bol)", "cm", m.backLengthCm(), false);
        field(html, values, "armholeDepthCm", "Ermehullsdybde", "cm", m.armholeDepthCm(), false);
        field(html, values, "sleeveLengthCm", "Ermelengde", "cm", options.sleeveLengthCm(), false);
        html.append("</div><details><summary>Andre kroppsmål – påvirker ikke formingen ennå</summary>")
                .append("<p>Disse målene beholdes som inndata, men endrer ikke oppskriften.</p><div class=\"fields\">");
        field(html, values, "underbustCircumferenceCm", "Omkrets under bryst", "cm", m.underbustCircumferenceCm(), false);
        field(html, values, "waistCircumferenceCm", "Midjeomkrets", "cm", m.waistCircumferenceCm(), false);
        field(html, values, "upperArmCircumferenceCm", "Overarmsomkrets", "cm", m.upperArmCircumferenceCm(), false);
        field(html, values, "bodyLengthCm", "Kroppslengde", "cm", m.bodyLengthCm(), false);
        html.append("</div></details></fieldset><fieldset><legend>02 / Passform og forming</legend>")
                .append("<label for=\"fit\">Passform</label><select id=\"fit\" name=\"fit\">");
        String fit = values.getProperty("fit", defaults.request().getFitPreference().name());
        html.append("<option value=\"NORMAL\"").append(fit.equals("NORMAL") ? " selected" : "")
                .append(">Normal</option><option value=\"OVERSIZED\"")
                .append(fit.equals("OVERSIZED") ? " selected" : "").append(">Oversized / romslig</option></select>")
                .append("<p>Normal: bryst × 1,0, hals × 1,3. Oversized: bryst × 1,1, hals × 1,5.</p><div class=\"fields\">");
        field(html, values, "armholeDepthGain", "Faktor for ermehullsdybde", "×", options.armholeDepthGain(), false);
        field(html, values, "shoulderWidthGain", "Faktor for skulderbredde", "×", options.shoulderWidthGain(), false);
        html.append("</div><p>Faktorene brukes før omregning. Ermehullsfaktoren gir vertikal plass, ikke ekstra overarmsomkrets.</p>")
                .append("</fieldset><fieldset><legend>03 / Strikkefasthet og pinner</legend><div class=\"fields\">");
        field(html, values, "stitchesPer10Cm", "Masker per 10 cm", "masker", gauge.getStitchesPer10Cm(), true);
        field(html, values, "rowsPer10Cm", "Rader per 10 cm", "rader", gauge.getStitchesPer10CmHeight(), true);
        field(html, values, "needleSizeMm", "Pinnestørrelse", "mm", gauge.getNeedleSize(), false);
        html.append("""
                </div></fieldset>
                <div class="actions"><button type="submit">Generer oppskrift</button>
                <a href="/">Tilbakestill til standard</a></div>
                <p class="privacy">Målene behandles lokalt og lagres ikke av programmet.</p>
                </div><section class="preview" aria-labelledby="preview-title">
                <span class="eyebrow">DIN OPPSKRIFT</span><h2 id="preview-title">Fra mål til masker</h2>
                <p>Oppskriften er ikke faglig godkjent. Kontroller mål og masketall før du strikker.</p>
                """);
        if (pattern.isEmpty()) {
            html.append("<div class=\"empty\">Oppskriften vises her når du trykker «Generer oppskrift».</div>");
        } else {
            html.append("<p role=\"status\">Oppskriften er klar.</p><button id=\"download\" type=\"button\">Lagre som .txt</button>")
                    .append("<label for=\"pattern\">Forhåndsvisning</label><textarea id=\"pattern\" readonly rows=\"30\">")
                    .append(escape(pattern)).append("</textarea>");
        }
        return html.append("</section></div></form></main><footer>Knitproj · Beregnet lokalt med Java</footer></body></html>")
                .toString();
    }

    private static void field(StringBuilder html, Properties values, String key, String label,
                              String unit, double fallback, boolean integer) {
        String value = values.getProperty(key, integer ? Integer.toString((int) fallback) : Double.toString(fallback));
        html.append("<div><label for=\"").append(key).append("\">").append(label)
                .append(" <span>(").append(unit).append(")</span></label><input type=\"number\" required id=\"")
                .append(key).append("\" name=\"").append(key).append("\" step=\"")
                .append(integer ? "1" : "any").append("\" min=\"")
                .append(integer ? "1" : "0").append("\" value=\"").append(escape(value)).append("\"></div>");
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
