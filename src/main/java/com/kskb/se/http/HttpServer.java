package com.kskb.se.http;

public interface HttpServer {
    int DEFAULT_PORT = 8080;

    static HttpServer create(HttpServerContext context) {
        return new HttpServerImpl(context);
    }

    void start() throws HttpServerException;

    void stop() throws HttpServerException;
}
