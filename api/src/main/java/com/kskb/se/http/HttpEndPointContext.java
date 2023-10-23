package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;

public interface HttpEndPointContext {
    HttpRequest request();
    HttpResponse.Builder response();

    Session session();
    Cookies cookies();
    Map<Object, Object> dataset();

    default HttpMethod method() { return request().method(); }
    default String path() { return request().path(); }

    static Builder builder() {
        return HttpEndPointContextImpl.builder();
    }

    interface Builder {
        Builder withRequest(HttpRequest request);
        Builder withResponseBuilder(HttpResponse.Builder response);
        Builder withCookies(Cookies cookies);
        Builder withSession(Session session);

        HttpEndPointContext build();
    }
}

class HttpEndPointContextImpl implements HttpEndPointContext {
    private final HttpRequest request;
    private final HttpResponse.Builder response;
    private final Cookies cookies;
    private final Session session;
    private final Map<Object, Object> dataset = new HashMap<>();

    public HttpEndPointContextImpl(Builder builder) {
        this.request = builder.request;
        this.response = builder.response;
        this.cookies = builder.cookies;
        this.session = builder.session;
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
    public Cookies cookies() {
        return this.cookies;
    }

    @Override
    public Session session() {
        return this.session;
    }

    @Override
    public Map<Object, Object> dataset() {
        return this.dataset;
    }

    static HttpEndPointContext.Builder builder() {
        return new Builder();
    }

    private static class Builder implements HttpEndPointContext.Builder {
        private HttpRequest request;
        private HttpResponse.Builder response;
        public Cookies cookies;
        private Session session;

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
        public Builder withCookies(Cookies cookies) {
            this.cookies = cookies;
            return this;
        }

        @Override
        public Builder withSession(Session session) {
            this.session = session;
            return this;
        }

        @Override
        public HttpEndPointContext build() {
            assert request != null;
            assert response != null;
            return new HttpEndPointContextImpl(this);
        }
    }
}

