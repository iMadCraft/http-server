package com.kskb.se.http;

public class Main {
    public static void main(final String[] args) throws HttpServerException {
        final HttpServerContext.Builder serverContext = HttpServerContext.builder();
        final HttpServer server = HttpFactory.createServer(serverContext.build());
        server.start();
    }
}
