package com.kskb.se.http;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.kskb.se.http.HttpMethod.GET;

public class Main {
    public static void main(final String[] args) throws HttpServerException {
        final var locator = HttpResourceLocator.builder()
           .withDefaults("sample")
           .build();

        final HttpServerContext.Builder serverContext = HttpServerContext.builder()
           .withLocator(locator)
           .withTrustStore("server.truststore")
           .withTrustStorePassword(new char[]{'1', '2', '3', '1', '2', '3'})
           .withKeyStore("server.keystore")
           .withKeyStorePassword(new char[]{'1', '2', '3', '1', '2', '3'});

        final HttpServer server = HttpFactory.createServer(serverContext.build());
        final var loader = server.resourceLoader();

        // Resource End Points
        server.add(GET, List.of("/", "/index.html"), (context) -> {
            final var request = context.request();
            final var user = request.headers()
               .get("User-Agent")
               .orElse("Client")
               .split("/")[0];
            final var param = request.query("param");
            final Consumer<HttpHyperText> newTemplate = (t) -> {
                t.bind("title", "Demo");
                t.bind("message", "Hello, " + user + (param != null ? " : " + param : ""));
                t.bind("date", () -> new Date().toString());
            };
            final var index = loader.load(HttpHyperText.class, "/index.html", newTemplate)
                  .or(() -> loader.load(HttpHyperText.class, "/pages/index.html", newTemplate));
            context.response().withPayload(index);
        });

        // API End Points
        server.add(GET, List.of("/api/v1/jvm/props"), (context) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("<table>");
            for (final var prop: System.getProperties().entrySet()) {
                builder.append("<tr><td>")
                       .append(prop.getKey())
                       .append("</td><td>")
                       .append(prop.getValue())
                       .append("</td></tr>");
            }
            builder.append("</table>");
            context.response().withPayload(HttpHyperText.create(builder.toString()));
        });

        server.add(GET, List.of("/api/v1/process/envvars"), (context) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("<table>");
            for (final var envvar: System.getenv().entrySet()) {
                builder.append("<tr><td>")
                   .append(envvar.getKey())
                   .append("</td><td>")
                   .append(envvar.getValue())
                   .append("</td></tr>");
            }
            builder.append("</table>");
            context.response().withPayload(HttpHyperText.create(builder.toString()));
        });
        server.start();
    }
}