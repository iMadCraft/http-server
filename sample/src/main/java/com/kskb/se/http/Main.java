package com.kskb.se.http;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.kskb.se.http.HttpMethod.GET;
import static com.kskb.se.http.HttpResourceLocation.HTML;

public class Main {
    public static void main(final String[] args) throws HttpServerException {
        final HttpResourceLocator locator = HttpResourceLocator.builder()
           .withDefaults("sample")
           .addLocation(HTML, "sample/src/main/html/pages")
           .build();

        final HttpServerContext.Builder serverContext = HttpServerContext.builder()
           .withLocator(locator)
           .withTrustStore("server.truststore")
           .withTrustStorePassword(new char[]{'1', '2', '3', '1', '2', '3'})
           .withKeyStore("server.keystore")
           .withKeyStorePassword(new char[]{'1', '2', '3', '1', '2', '3'});

        final HttpServer server = HttpFactory.createServer(serverContext.build());
        final var loader = server.resourceLoader();

        // Resource End Ponts
        server.add(GET, List.of("/", "/index.html"), (context) -> {
            final var request = context.request();
            final var user = request.headers()
               .get("User-Agent")
               .orElse("Client")
               .split("/")[0];
            context.response().withPayload(loader.load(HttpHyperText.class, request.url(), (newTemplate) -> {
                newTemplate.bind("title", "Demo");
                newTemplate.bind("message", "Hello, " + user);
                newTemplate.bind("date", () -> new Date().toString());
            }));
        });

        server.add(GET, List.of("/favicon.ico"), (context) ->
           context.response().withPayload(loader.load(HttpIconImage.class, context.request().url())));

        server.add(GET, List.of("/css/*"), (context) ->
           context.response().withPayload(loader.load(HttpStyle.class, context.request().url())));

        server.add(GET, List.of("/js/*"), (context) ->
           context.response().withPayload(loader.load(HttpScript.class, context.request().url())));

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