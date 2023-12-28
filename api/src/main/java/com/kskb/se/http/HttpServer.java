package com.kskb.se.http;

import java.util.List;
import java.util.regex.Pattern;

public interface HttpServer {
    int DEFAULT_PORT = 8081;

    void start() throws HttpServerException;
    void stop() throws HttpServerException;

    HttpServerState state();
    HttpRewriters rewriters();
    HttpEndPoints endPoints();
    HttpResourceLoader resourceLoader();
    HttpHooks hooks();

    default boolean isRunning() {
        return this.state() == HttpServerState.RUNNING;
    }

    default void add(HttpMethod method, String path, HttpEndPoint handler) {
        this.endPoints().add(method, List.of(Pattern.compile(path)), handler);
    }

    default void add(HttpMethod method, List<String> url, HttpEndPoint handler) {
        this.endPoints().add(method, url.stream().map(Pattern::compile).toList(), handler);
    }
}
