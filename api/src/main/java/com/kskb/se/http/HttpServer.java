package com.kskb.se.http;

import java.util.List;

public interface HttpServer {
    int DEFAULT_PORT = 8081;

    void start() throws HttpServerException;
    void stop() throws HttpServerException;

    HttpServerState state();
    HttpRewriters rewriters();
    HttpEndPoints endPoints();
    HttpResourceLoader resourceLoader();

    default boolean isRunning() {
        return this.state() == HttpServerState.RUNNING;
    }

    default void add(HttpMethod method, String path, HttpEndPoint handler) {
        this.endPoints().add(method, List.of(path), handler);
    }

    default void add(HttpMethod method, List<String> url, HttpEndPoint handler) {
        this.endPoints().add(method, url, handler);
    }
}
