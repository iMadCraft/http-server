package com.kskb.se.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 abstract class AbstractHttpPacket implements HttpPacket {
    private final HttpMethod method;
    private final String version;
    private final String url;
    private final List<HttpHeader> headers;
    private final String payload;

    protected AbstractHttpPacket(Builder<?> builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.version = builder.version;
        this.headers = Collections.unmodifiableList(builder.headers);
        this.payload = builder.payload;
    }

    @Override
    public HttpMethod method() {
        return this.method;
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public Iterable<HttpHeader> headers() {
        return this.headers;
    }

    @Override
    public String payload() {
        return this.payload;
    }

    protected static abstract class Builder<T extends HttpPacket.Builder<T>> implements HttpPacket.Builder<T> {
        private final List<HttpHeader> headers = new ArrayList<>();
        private HttpMethod method;
        private String url;
        private String version;
        private String payload;

        protected Builder() {
        }

        protected Builder(HttpRequest from) {
            if (from != null) {
                this.method = from.method();
                this.url = from.url();
                this.version = from.version();
            }
        }

        @Override
        public T withMethod(HttpMethod method) {
            this.method = method;
            return castThis();
        }

        @Override
        public T withUrl(String url) {
            this.url = url;
            return castThis();
        }

        @Override
        public T withVersion(String version) {
            this.version = version;
            return castThis();
        }

        @Override
        public T addHeader(HttpHeader header) {
            this.headers.add(header);
            return castThis();
        }

        @Override
        public T withPayload(String payload) {
            this.payload = payload;
            return castThis();
        }

        @SuppressWarnings("unchecked")
        private T castThis() {
            return (T) this;
        }
    }
}
