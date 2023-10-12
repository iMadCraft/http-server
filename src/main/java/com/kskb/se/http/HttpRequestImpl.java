package com.kskb.se.http;

class HttpRequestImpl extends AbstractHttpPacket implements HttpRequest {
    private HttpRequestImpl(Builder builder) {
        super(builder);
    }

    static Builder builder() {
        return new Builder();
    }

    private static class Builder extends AbstractHttpPacket.Builder<HttpRequest.Builder> implements HttpRequest.Builder {
        @Override
        public HttpRequest build() {
            return new HttpRequestImpl(this);
        }
    }
}
