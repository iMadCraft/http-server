package com.kskb.se.http;

public class HttpFactory {
    public static HttpServer createServer() {
        HttpServerContext context = HttpServerContext.builder().build();
        return new HttpServerImpl(context);
    }

    public static HttpServer createServer(HttpServerContext context) {
        return new HttpServerImpl(context);
    }
}
