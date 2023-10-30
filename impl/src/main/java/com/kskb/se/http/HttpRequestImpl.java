package com.kskb.se.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class HttpRequestImpl extends AbstractHttpPacket implements HttpRequest {
    private final HttpMethod method;
    private final String version;
    private final URI uri;
    private final Cookies cookies;
    private final Map<String, String> params;

    private HttpRequestImpl(Builder builder) {
        super(builder);
        this.method = builder.method;
        this.uri = builder.uri;
        this.version = builder.version;
        this.cookies = builder.cookies;
        this.params = builder.params;
    }

    @Override
    public HttpMethod method() {
        return this.method;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public String version() {
        return this.version;
    }

    @Override
    public String originalUrl() {
        return uri.getPath();
    }

    @Override
    public String query(String key) {
        return findParam(uri, params, key);
    }

    @Override
    public Cookies cookies() {
        return this.cookies;
    }

    private static String findParam(URI uri, Map<String, String> params, String key) {
        if ( uri.getQuery() != null) {
            final var paramList = uri.getQuery().split("&");
            for (final var param: paramList) {
                final var parts = param.split("=");
                params.put(parts[0], parts.length > 1 ? parts[1] : "");
            }
            return params.get(key);
        }
        return null;
    }

    @Override
    public String toString() {
        return this.method.name() + " " + originalUrl() + " " + version +
           " { headers: " + headers().size() + ", payload: " + payload().map(HttpResource::size).orElse(0L) + " }";
    }

    static HttpRequest.Builder builder() {
        return new Builder();
    }

    static class Builder extends AbstractHttpPacket.Builder<HttpRequest.Builder> implements HttpRequest.Builder {
        private HttpMethod method;
        private URI uri;
        private String version;
        private Cookies cookies;

        private final Map<String, String> params = new HashMap<>();

        @Override
        public HttpMethod method() {
            return this.method;
        }

        @Override
        public URI uri() {
            return this.uri;
        }

        @Override
        public String version() {
            return this.version;
        }

        @Override
        public String originalUrl() {
            return this.uri.getPath();
        }

        @Override
        public String query(String key) {
            return findParam(uri, params, key);
        }

        @Override
        public Cookies cookies() {
            return cookies;
        }

        @Override
        public HttpRequest.Builder withMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        @Override
        public HttpRequest.Builder withUri(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public HttpRequest.Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        @Override
        public HttpRequest.Builder withCookies(Cookies cookies) {
            this.cookies = cookies;
            return this;
        }

        @Override
        public HttpRequest build() {
            if (cookies == null)
                cookies = Cookies.create();
            return new HttpRequestImpl(this);
        }
    }
}
