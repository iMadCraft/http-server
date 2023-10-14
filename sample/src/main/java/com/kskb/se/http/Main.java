package com.kskb.se.http;

import javax.swing.plaf.TableHeaderUI;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.kskb.se.http.HttpMethod.GET;

public class Main {
    public static void main(final String[] args) throws HttpServerException {
        final HttpServerContext.Builder serverContext = HttpServerContext.builder();
        final HttpServer server = HttpFactory.createServer(serverContext.build());
        final var loader = HttpResourceLoader.create();
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
                final Optional<HttpTemplate> templateOpt = loader.load(url, (newTemplate) -> {
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
