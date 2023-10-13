package com.kskb.se.http;

import java.util.List;

import static com.kskb.se.http.HttpMethod.GET;

public class Main {

    private static final String INDEX_PAYLOAD = """
    <html>
    <body>
        <h1>%s</h1>
        <p>%s</p>
    </body>
    </html>
    """;

    public static void main(final String[] args) throws HttpServerException {
        final HttpServerContext.Builder serverContext = HttpServerContext.builder();
        final HttpServer server = HttpFactory.createServer(serverContext.build());

        server.add(GET, List.of("/", "/index.html", "/index.htm"), (context) -> {
            final String payload = String.format(INDEX_PAYLOAD, "Hello world", "This is just text");
            context.response()
               .withPayload(payload);
        });

        server.add(GET, List.of("/api/v1/users"), (context) -> {
            final String payload = String.format(INDEX_PAYLOAD, "Hello world", "This is just text");
            context.response()
                    .withPayload(payload);
        });

        server.start();
    }
}
