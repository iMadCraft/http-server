package com.kskb.se.http;

import java.util.Date;
import java.util.List;

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

        server.rewriters().add(GET, List.of("*"), (context) -> {
            final var url = context.url();
            // All none api request should be redirected to site
            if ( ! url.startsWith("/api") ) {
                context.request()
                   .withUrl("/site" + url);
            }
        });

        server.add(GET, List.of("/site/", "/site/index.html"), (context) -> {
            final var request = context.request();
            final var user = request.headers()
               .get("User-Agent")
               .orElse("Client")
               .split("/")[0];
            context.response().withPayload(loader.load(HttpHyperText.class, request.originalUrl(), (newTemplate) -> {
                newTemplate.bind("title", "Demo");
                newTemplate.bind("message", "Hello, " + user);
                newTemplate.bind("date", () -> new Date().toString());
            }));
        });

        server.add(GET, List.of("/site/css/*"), (context) ->
           context.response().withPayload(loader.load(HttpStyle.class, context.request().originalUrl()))
        );

        server.start();
    }
}
