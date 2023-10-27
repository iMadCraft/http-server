package com.kskb.se.http;

public class CliMain {
    public static void main(final String[] args) throws HttpServerException {
        final HttpServerContext.Builder serverContext = HttpServerContext.builder();
        final HttpServer server = HttpFactory.createServer(serverContext.build());
        server.start();
    }
}
