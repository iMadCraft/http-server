package com.kskb.se.http;

class HttpRequestImpl extends AbstractHttpPacket implements HttpRequest {
    private final HttpMethod method;
    private final String version;
    private final String url;
    private final String originalUrl;

    private HttpRequestImpl(Builder builder) {
        super(builder);
        this.method = builder.method;
        this.url = builder.url;
        this.version = builder.version;
        this.originalUrl = builder.originalUrl;
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
    public String originalUrl() {
        return this.originalUrl;
    }

    @Override
    public String toString() {
        return this.method.name() + " " + originalUrl + " " + version +
           " { headers: " + headers().size() + ", payload: " + payload().map(HttpResource::size).orElse(0L) + " }";
    }

    static HttpRequest.Builder builder() {
        return new Builder();
    }

    static class Builder extends AbstractHttpPacket.Builder<HttpRequest.Builder> implements HttpRequest.Builder {
        private HttpMethod method;
        private String url;
        private String version;
        private String originalUrl;

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
        public Builder withMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        @Override
        public Builder withUrl(String url) {
            this.url = url;
            if (this.originalUrl == null)
                this.originalUrl = url;
            return this;
        }

        @Override
        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        @Override
        public HttpRequest build() {
            return new HttpRequestImpl(this);
        }
    }
}
