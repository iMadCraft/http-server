package com.kskb.se.http;

class HttpResponseImpl extends AbstractHttpPacket implements HttpResponse {
    private final int code;

    private HttpResponseImpl(Builder builder) {
        super(builder);
        this.code = builder.code;
    }

    static HttpResponse.Builder builder(HttpRequest from) {
        return new Builder(from);
    }

    @Override
    public int code() {
        return this.code;
    }

    private static class Builder extends AbstractHttpPacket.Builder<HttpResponse.Builder> implements HttpResponse.Builder {
        private int code;

        Builder(HttpRequest from) {
            super(from);
        }

        @Override
        public HttpResponse.Builder withResponseCode(int code) {
            this.code = code;
            return this;
        }

        @Override
        public HttpResponse build() {
            return new HttpResponseImpl(this);
        }
    }
}
