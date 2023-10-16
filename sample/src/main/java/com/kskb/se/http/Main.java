package com.kskb.se.http;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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

        server.add(GET, List.of("/site/*"), (context) -> {
            try {
                final var url = context.url().substring("/site/".length());
                final Optional<HttpTemplate> templateOpt = loader.load(HttpTemplate.class, url, (newTemplate) -> {
                    newTemplate.bind("title", "Hello world");
                    newTemplate.bind("date", () -> new Date().toString());
                });

                if (templateOpt.isPresent()) {
                    context.response()
                       .withResponseCode(200)
                       .withPayload(templateOpt.get().toString());
                }
                else {
                    context.response()
                       .withDetails(context.url())
                       .withResponseCode(404);
                }
            }
            catch (Throwable e) {
                context.response()
                   .withDetails(e.getMessage())
                   .withResponseCode(500);
            }
        });

        server.start();
    }
}
