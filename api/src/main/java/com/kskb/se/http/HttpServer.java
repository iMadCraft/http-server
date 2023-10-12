package com.kskb.se.http;

public interface HttpServer {
    int DEFAULT_PORT = 8081;

    void start() throws HttpServerException;

    void stop() throws HttpServerException;
}
