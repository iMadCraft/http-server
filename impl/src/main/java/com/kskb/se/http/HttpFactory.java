package com.kskb.se.http;

public class HttpFactory {
    public static HttpServer createServer(HttpServerContext context) {
        return new HttpServerImpl(context);
    }
}
