package com.kskb.se.http;

public class Main {
    public static void main(final String[] args) throws HttpServerException {
        final HttpServerContext.Builder context = HttpServerContext.builder();
        final HttpServer server = HttpFactory.createServer(context.build());
        server.start();
    }
}
