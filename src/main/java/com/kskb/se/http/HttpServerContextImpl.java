package com.kskb.se.http;

public class HttpServerContextImpl implements HttpServerContext {
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
    public HttpParser parser() {
        return this.parser;
    }

    @Override
    public HttpSerializer serializer() {
        return this.serializer;
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
            if (parser == null)
                parser = new HttpParserImpl();
            if (serializer == null)
                serializer = new HttpSerializerImpl();
            return new HttpServerContextImpl(this);
        }
    }
}
