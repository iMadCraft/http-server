package com.kskb.se.http;

class HttpResponseImpl extends AbstractHttpPacket implements HttpResponse {
    private final int code;
    private final String details;

    private HttpResponseImpl(Builder builder) {
        super(builder);
        this.code = builder.code;
        this.details = builder.details;
    }

    static HttpResponse.Builder builder() {
        return new Builder(null);
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String details() {
        return this.details;
    }

    private static class Builder extends AbstractHttpPacket.Builder<HttpResponse.Builder> implements HttpResponse.Builder {
        private int code;
        private String details;

        Builder(HttpRequest from) {
            super(from);
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public HttpResponse.Builder withResponseCode(int code) {
            this.code = code;
            return this;
        }

        @Override
        public HttpResponse.Builder withDetails(String message) {
            this.details = message;
            return this;
        }

        @Override
        public HttpResponse build() {
            if (hasPayload()) {
                addHeader(HttpHeader.create("Content-Length", String.valueOf(payload().length())));
            }
            return new HttpResponseImpl(this);
        }
    }
}
