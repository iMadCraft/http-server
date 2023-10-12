package com.kskb.se.http;

import java.util.Optional;

public interface HttpServerContext {

    static Builder builder() {
        return HttpServerContextImpl.builder();
    }

    int port();

    Optional<HttpParser> parser();

    Optional<HttpSerializer> serializer();

    interface Builder {
        Builder withPort(int port);

        HttpServerContext build();
    }
}

class HttpServerContextImpl implements HttpServerContext {
    private final int port;
    private final HttpParser parser;
    private final HttpSerializer serializer;

    private HttpServerContextImpl(Builder builder) {
        this.port = builder.port;
        this.parser = builder.parser;
        this.serializer = builder.serializer;
    }

    static Builder builder() {
        return new Builder();
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public Optional<HttpParser> parser() {
        return Optional.ofNullable(this.parser);
    }

    @Override
    public Optional<HttpSerializer> serializer() {
        return Optional.ofNullable(this.serializer);
    }

    static class Builder implements HttpServerContext.Builder {
        int port = HttpServer.DEFAULT_PORT;
        HttpParser parser;
        HttpSerializer serializer;

        @Override
        public HttpServerContext.Builder withPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public HttpServerContext build() {
            return new HttpServerContextImpl(this);
        }
    }
}
