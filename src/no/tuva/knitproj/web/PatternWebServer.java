package no.tuva.knitproj.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import no.tuva.knitproj.application.PatternGenerator;
import no.tuva.knitproj.cli.PatternInput;
import no.tuva.knitproj.service.PatternTextFormatter;

public final class PatternWebServer implements AutoCloseable {
    private static final int MAX_BODY = 16_384;
    private final HttpServer server;
    private final String token = UUID.randomUUID().toString();

    public PatternWebServer() throws IOException {
        this(0);
    }

    public PatternWebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    public URI uri() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String host = exchange.getRequestHeaders().getFirst("Host");
            if (!isLocalAuthority(host, server.getAddress().getPort())
                    || (exchange.getRequestHeaders().containsKey("Origin")
                    && !("http://" + host).equals(exchange.getRequestHeaders().getFirst("Origin")))) {
                send(exchange, 403, "text/plain", "Kun lokale forespørsler fra denne siden er tillatt.");
                return;
            }
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/") && method.equals("GET")) {
                send(exchange, 200, "text/html", PatternPage.render(new Properties(), token, "", ""));
            } else if ((path.equals("/style.css") || path.equals("/app.js")) && method.equals("GET")) {
                try (var resource = PatternWebServer.class.getResourceAsStream("/web" + path)) {
                    if (resource == null) {
                        send(exchange, 404, "text/plain", "Fant ikke ressursen.");
                    } else {
                        send(exchange, 200, path.endsWith(".css") ? "text/css" : "text/javascript",
                                new String(resource.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
            } else if (path.equals("/generate") && method.equals("POST")) {
                generate(exchange);
            } else {
                send(exchange, 404, "text/plain", "Fant ikke siden eller handlingen.");
            }
        }
    }

    static boolean isLocalAuthority(String host, int port) {
        return ("127.0.0.1:" + port).equals(host) || ("localhost:" + port).equals(host)
                || (port == 80 && ("127.0.0.1".equals(host) || "localhost".equals(host)));
    }

    private void generate(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";", 2)[0].strip()
                .equalsIgnoreCase("application/x-www-form-urlencoded")) {
            send(exchange, 415, "text/plain", "Bruk skjemaet for å sende inn mål.");
            return;
        }
        byte[] body = exchange.getRequestBody().readNBytes(MAX_BODY + 1);
        if (body.length > MAX_BODY) {
            send(exchange, 413, "text/plain", "Skjemaet er for stort.");
            return;
        }
        Properties values = new Properties();
        try {
            for (String entry : new String(body, StandardCharsets.UTF_8).split("&")) {
                String[] pair = entry.split("=", 2);
                if (pair.length != 2) {
                    throw new IllegalArgumentException("Ugyldig skjemafelt. Fyll ut skjemaet på nytt.");
                }
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                if (values.putIfAbsent(key, value) != null) {
                    throw new IllegalArgumentException("Et felt er sendt inn flere ganger.");
                }
            }
            if (!token.equals(values.remove("token"))) {
                send(exchange, 403, "text/plain", "Åpne startsiden på nytt før du genererer.");
                return;
            }
            var input = PatternInput.fromProperties(values);
            var generated = new PatternGenerator().generate(input.request(), input.options());
            send(exchange, 200, "text/html", PatternPage.render(values, token,
                    new PatternTextFormatter().format(generated.pattern()), ""));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, "text/html", PatternPage.render(values, token, "",
                    "Bruk positive tall (hele tall for masker og rader). Kontroller også forholdet mellom målene. "
                            + "Detaljer fra beregningen: " + e.getMessage()));
        }
    }

    private static void send(HttpExchange exchange, int status, String type, String body) throws IOException {
        var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", type + "; charset=utf-8");
        headers.set("Cache-Control", "no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("Content-Security-Policy",
                "default-src 'none'; style-src 'self'; script-src 'self'; form-action 'self'; "
                        + "frame-ancestors 'none'; base-uri 'none'");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
