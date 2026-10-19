package no.tuva.knitproj.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Map;
import java.util.regex.Pattern;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.service.PatternTextFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternWebServerTest {
    private final PatternWebServer server = new PatternWebServer();
    private final HttpClient client = HttpClient.newHttpClient();

    PatternWebServerTest() throws IOException {}

    @AfterEach
    void close() {
        server.close();
    }

    @Test
    void formContainsEveryCurrentDefaultAndGeneratesTheExactEngineOutput() throws Exception {
        var page = get("/");
        assertEquals(200, page.statusCode());
        assertEquals("127.0.0.1", server.uri().getHost());
        assertTrue(page.body().contains("lang=\"nb\""));
        assertTrue(page.body().contains("Generer oppskrift"));
        assertTrue(page.body().contains("påvirker ikke formingen ennå"));
        var fields = Pattern.compile("name=\"([^\"]+)\" step=\"[^\"]+\" min=\"[^\"]+\" value=\"([^\"]+)\"")
                .matcher(page.body());
        var values = new Properties();
        while (fields.find()) {
            values.setProperty(fields.group(1), fields.group(2));
        }
        assertEquals(16, values.size());
        values.setProperty("fit", "NORMAL");
        assertEquals(PatternInput.defaults().options(), PatternInput.fromProperties(values).options());
        assertEquals(PatternInput.defaults().request().getMeasurements(),
                PatternInput.fromProperties(values).request().getMeasurements());
        var response = post(form(values), token(page.body()));
        assertEquals(200, response.statusCode());
        assertEquals(pattern(PatternInput.defaults()), preview(response.body()));
        assertTrue(response.body().contains("Lagre som .txt"));
        assertEquals("no-store", response.headers().firstValue("Cache-Control").orElseThrow());
        assertTrue(response.headers().firstValue("Content-Security-Policy").orElseThrow()
                .contains("frame-ancestors 'none'"));
    }

    @Test
    void editedGaugeFitMeasurementsAndGainsUseTheExistingEngine() throws Exception {
        var values = new Properties();
        values.setProperty("stitchesPer10Cm", "18");
        values.setProperty("rowsPer10Cm", "24");
        values.setProperty("needleSizeMm", "4.5");
        values.setProperty("fit", "OVERSIZED");
        values.setProperty("bustCircumferenceCm", "95");
        values.setProperty("sleeveLengthCm", "12.5");
        values.setProperty("armholeDepthGain", "1.0");
        values.setProperty("shoulderWidthGain", "0.8");
        var response = post(form(values), token(get("/").body()));
        assertEquals(200, response.statusCode());
        assertEquals(pattern(PatternInput.fromProperties(values)), preview(response.body()));
        assertTrue(response.body().contains("value=\"OVERSIZED\" selected"));
    }

    @Test
    void invalidValuesAndGeometryKeepInputAndDoNotOfferADownload() throws Exception {
        String token = token(get("/").body());
        for (String body : new String[]{"neckCircumferenceCm=-1", "rowsPer10Cm=2.5",
                "shoulderWidthGain=2", "armholeDepthGain=NaN", "bustCircumferenceCm="}) {
            var response = post(body, token);
            assertEquals(400, response.statusCode(), body);
            assertTrue(response.body().contains("role=\"alert\""));
            assertFalse(response.body().contains("id=\"download\""));
        }
        var response = post("neckCircumferenceCm=0", token);
        assertTrue(response.body().contains("value=\"0\""));
    }

    @Test
    void untrustedValuesAndErrorDetailsAreEscaped() throws Exception {
        var response = post("unknown%3Cscript%3E=1&neckCircumferenceCm=%22%3E%3Cscript%3E",
                token(get("/").body()));
        assertEquals(400, response.statusCode());
        assertFalse(response.body().contains("<script>"));
        assertTrue(response.body().contains("&lt;script&gt;"));
    }

    @Test
    void blocksCrossOriginRequestsMissingTokensAndOversizedBodies() throws Exception {
        assertEquals(403, post("", "wrong").statusCode());
        assertEquals(403, client.send(HttpRequest.newBuilder(server.uri().resolve("/generate"))
                .header("Origin", "https://example.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("")).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(413, post("x=" + "a".repeat(16_384), token(get("/").body())).statusCode());
        assertEquals(400, post("fit=NORMAL&fit=OVERSIZED", token(get("/").body())).statusCode());
        assertEquals(400, post("x=%ZZ", token(get("/").body())).statusCode());
        assertEquals(415, client.send(HttpRequest.newBuilder(server.uri().resolve("/generate"))
                .POST(HttpRequest.BodyPublishers.ofString("{}")).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void localhostSupportsFormAssetsAndSameOriginGeneration() throws Exception {
        URI local = URI.create(server.uri().toString().replace("127.0.0.1", "localhost"));
        var page = client.send(HttpRequest.newBuilder(local).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, page.statusCode());
        assertEquals("same-origin", page.headers().firstValue("Referrer-Policy").orElseThrow());
        assertEquals(200, client.send(HttpRequest.newBuilder(local.resolve("/style.css")).GET().build(),
                HttpResponse.BodyHandlers.ofString()).statusCode());
        var response = client.send(HttpRequest.newBuilder(local.resolve("/generate"))
                .header("Origin", local.toString().replaceFirst("/$", ""))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("token=" + token(page.body())))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("same-origin", response.headers().firstValue("Referrer-Policy").orElseThrow());
        assertEquals(pattern(PatternInput.defaults()), preview(response.body()));
        assertEquals(403, client.send(HttpRequest.newBuilder(local.resolve("/generate"))
                .header("Origin", "http://localhost.evil.example:" + local.getPort())
                .POST(HttpRequest.BodyPublishers.ofString("")).build(),
                HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(403, client.send(HttpRequest.newBuilder(local.resolve("/generate"))
                .header("Origin", server.uri().toString().replaceFirst("/$", ""))
                .POST(HttpRequest.BodyPublishers.ofString("")).build(),
                HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void opaqueNullOriginIsRejectedEvenWithAValidFormToken() throws Exception {
        var response = client.send(HttpRequest.newBuilder(server.uri().resolve("/generate"))
                .header("Origin", "null")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("token=" + token(get("/").body())))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode());
        assertFalse(response.body().contains("id=\"download\""));
    }

    @Test
    void codespacesProxyCanGenerateWithRewrittenOrPreservedHost() throws Exception {
        int port;
        try (var reservation = new ServerSocket(0)) {
            port = reservation.getLocalPort();
        }
        var environment = Map.of("CODESPACES", "true", "CODESPACE_NAME", "synthetic-workspace",
                "GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN", "app.github.dev");
        try (var forwarded = new PatternWebServer(port, environment)) {
            String origin = "https://synthetic-workspace-" + port + ".app.github.dev";
            assertEquals(origin + "/", forwarded.browserUri().toString());
            var page = client.send(HttpRequest.newBuilder(forwarded.uri()).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            String formToken = token(page.body());
            for (String host : new String[]{"localhost:" + port, "127.0.0.1:" + port,
                    "synthetic-workspace-" + port + ".app.github.dev"}) {
                String response = forwardedRequest(port, host, origin, formToken);
                assertTrue(response.startsWith("HTTP/1.1 200"), response);
                assertEquals(pattern(PatternInput.defaults()), preview(response));
                assertTrue(forwardedRequest(port, host, "https://another-workspace-" + port + ".app.github.dev",
                        formToken).startsWith("HTTP/1.1 403"));
                assertTrue(forwardedRequest(port, host, origin, "wrong-token").startsWith("HTTP/1.1 403"));
            }
            assertTrue(forwardedRequest(port, "attacker.example", origin, formToken).startsWith("HTTP/1.1 403"));
            assertTrue(forwardedRequest(port, "localhost:" + port, origin + ".evil.example",
                    formToken).startsWith("HTTP/1.1 403"));
        }
    }

    @Test
    void codespaceMetadataMustBeExplicitAndWellFormed() {
        assertNull(PatternWebServer.codespaceOrigin(8080, Map.of()));
        assertEquals(URI.create("https://synthetic-workspace-8081.app.github.dev"),
                PatternWebServer.codespaceOrigin(8081, Map.of("CODESPACES", "true",
                        "CODESPACE_NAME", "synthetic-workspace",
                        "GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN", "app.github.dev")));
        assertThrows(IllegalArgumentException.class,
                () -> PatternWebServer.codespaceOrigin(8080, Map.of("CODESPACES", "true")));
        for (String invalid : new String[]{"app.github.dev/path", "app.github.dev@evil.example", "bad\nhost"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> PatternWebServer.codespaceOrigin(8080, Map.of("CODESPACES", "true",
                            "CODESPACE_NAME", "synthetic-workspace",
                            "GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN", invalid)));
        }
    }

    private static String forwardedRequest(int port, String host, String origin, String token) throws IOException {
        String body = "token=" + token;
        try (var socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(5000);
            String request = "POST /generate HTTP/1.1\r\nHost: " + host + "\r\nOrigin: " + origin
                    + "\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: "
                    + body.length() + "\r\nConnection: close\r\n\r\n" + body;
            socket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void defaultHttpPortAcceptsBrowserAuthoritiesWithoutAPort() {
        assertTrue(PatternWebServer.isLocalAuthority("localhost", 80));
        assertTrue(PatternWebServer.isLocalAuthority("127.0.0.1", 80));
        assertTrue(PatternWebServer.isLocalAuthority("localhost:80", 80));
        assertFalse(PatternWebServer.isLocalAuthority("localhost", 8080));
        assertFalse(PatternWebServer.isLocalAuthority("localhost:8081", 8080));
        assertFalse(PatternWebServer.isLocalAuthority("localhost.evil.example", 80));
        assertFalse(PatternWebServer.isLocalAuthority(null, 80));
    }

    @Test
    void requestedPortIsUsedAndAnOccupiedPortFails() throws Exception {
        try (var other = new PatternWebServer(0)) {
            int port = other.uri().getPort();
            assertThrows(IOException.class, () -> new PatternWebServer(port));
        }
    }

    @Test
    void servesOnlyKnownResourcesAndDoesNotGenerateViaGet() throws Exception {
        assertEquals(200, get("/style.css").statusCode());
        assertTrue(get("/app.js").body().contains("new Blob"));
        assertEquals(404, get("/generate").statusCode());
        assertEquals(404, get("/unknown").statusCode());
        assertEquals(404, get("/../README.txt").statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(server.uri().toString().replaceFirst("/$", "") + path))
                .GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> post(String body, String token) throws Exception {
        return client.send(HttpRequest.newBuilder(server.uri().resolve("/generate"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("token=" + token + (body.isEmpty() ? "" : "&" + body)))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String token(String page) {
        var matcher = Pattern.compile("name=\"token\" value=\"([^\"]+)\"").matcher(page);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private static String form(Properties values) {
        return values.stringPropertyNames().stream()
                .map(key -> key + "=" + URLEncoder.encode(values.getProperty(key), StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b).orElse("");
    }

    private static String pattern(PatternInput input) {
        return new PatternTextFormatter().format(
                new PatternGenerator().generate(input.request(), input.options()).pattern());
    }

    private static String preview(String page) {
        var matcher = Pattern.compile("<textarea[^>]+>(.*?)</textarea>", Pattern.DOTALL).matcher(page);
        assertTrue(matcher.find());
        return matcher.group(1).replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&");
    }
}
