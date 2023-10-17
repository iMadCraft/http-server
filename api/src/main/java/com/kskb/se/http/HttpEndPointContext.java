package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;

public interface HttpEndPointContext {
    HttpMethod method();

    String url();

    HttpRequest request();

    HttpResponse.Builder response();

    Map<Object, Object> dataset();

    static Builder builder() {
        return HttpEndPointContextImpl.builder();
    }

    interface Builder {
        Builder withMethod(HttpMethod method);

        Builder withUrl(String url);

        Builder withRequest(HttpRequest request);

        Builder withResponseBuilder(HttpResponse.Builder response);

        HttpEndPointContext build();
    }
}

class HttpEndPointContextImpl implements HttpEndPointContext {

    private final HttpMethod method;
    private final String url;
    private final HttpRequest request;
    private final HttpResponse.Builder response;
    private final Map<Object, Object> dataset = new HashMap<>();

    public HttpEndPointContextImpl(Builder builder) {
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
    public HttpRequest request() {
        return this.request;
    }

    @Override
    public HttpResponse.Builder response() {
        return this.response;
    }

    @Override
    public Map<Object, Object> dataset() {
        return this.dataset;
    }

    static HttpEndPointContext.Builder builder() {
        return new Builder();
    }

    private static class Builder implements HttpEndPointContext.Builder {

        private HttpResponse.Builder response;
        private HttpMethod method;
        private String url;
        private HttpRequest request;

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
        public Builder withRequest(HttpRequest request) {
            this.request = request;
            return this;
        }

        @Override
        public Builder withResponseBuilder(HttpResponse.Builder response) {
            this.response = response;
            return this;
        }

        @Override
        public HttpEndPointContext build() {
            assert request != null;
            assert response != null;
            method = method != null ?
               method : request.method();
            url = url != null ?
               url : request.uri().getPath();
            return new HttpEndPointContextImpl(this);
        }
    }
}

