package com.kskb.se.http;

public interface HttpRewriterContext {
    HttpMethod method();

    String url();

    HttpRequest.Builder request();

    HttpResponse.Builder response();

    static Builder builder() {
        return HttpRewriterContextImpl.builder();
    }

    interface Builder {
        Builder withMethod(HttpMethod method);

        Builder withUrl(String url);

        Builder withRequestBuilder(HttpRequest.Builder request);

        Builder withResponseBuilder(HttpResponse.Builder response);

        HttpRewriterContext build();
    }
}

class HttpRewriterContextImpl implements HttpRewriterContext {

    private final HttpMethod method;
    private final String url;
    private final HttpRequest.Builder request;
    private final HttpResponse.Builder response;

    private HttpRewriterContextImpl(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.request = builder.request;
        this.response = builder.response;
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
    public HttpRequest.Builder request() {
        return this.request;
    }

    @Override
    public HttpResponse.Builder response() {
        return this.response;
    }

    static HttpRewriterContext.Builder builder() {
        return new Builder();
    }

    private static class Builder implements HttpRewriterContext.Builder {

        private HttpResponse.Builder response;
        private HttpMethod method;
        private String url;
        private HttpRequest.Builder request;

        @Override
        public Builder withMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        @Override
        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        @Override
        public Builder withRequestBuilder(HttpRequest.Builder request) {
            this.request = request;
            return this;
        }

        @Override
        public Builder withResponseBuilder(HttpResponse.Builder response) {
            this.response = response;
            return this;
        }

        @Override
        public HttpRewriterContext build() {
            assert request != null;
            assert response != null;
            method = method != null ?
               method : request.method();
            url = url != null ?
               url : request.uri().getPath();
            return new HttpRewriterContextImpl(this);
        }
    }
}

