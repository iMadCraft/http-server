package com.kskb.se.http;

import java.util.List;

public interface HttpServer {
    int DEFAULT_PORT = 8081;

    void start() throws HttpServerException;

    void stop() throws HttpServerException;

    HttpEndPoints endPoints();

    default void add(HttpMethod method, List<String> url, HttpEndPoint handler) {
        this.endPoints().add(method, url, handler);
    }
}
