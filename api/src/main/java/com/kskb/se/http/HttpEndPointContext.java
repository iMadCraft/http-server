package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public interface HttpEndPointContext {
    HttpRequest request();
    HttpResponse.Builder response();

    Session session();
    Cookies cookies();
    Map<Object, Object> dataset();
    HttpFlowController flow();
    HttpHooks hooks();
    Matcher matcher();

    default HttpMethod method() { return request().method(); }
    default String path() { return request().path(); }
    default String host() { return request().host(); }
    default int port() { return request().port(); }
    static Builder builder() {
        return HttpEndPointContextImpl.builder();
    }



    interface Builder {
        Builder withRequest(HttpRequest request);
        Builder withResponseBuilder(HttpResponse.Builder response);
        Builder withCookies(Cookies cookies);
        Builder withSession(Session session);
        Builder withFlowController(HttpFlowController flow);
        Builder withHooks(HttpHooks hooks);
        Builder withMatcher(Matcher matcher);

        HttpEndPointContext build();

    }
}

class HttpEndPointContextImpl implements HttpEndPointContext {
    private final HttpRequest request;
    private final HttpResponse.Builder response;
    private final Cookies cookies;
    private final Session session;
    private final HttpFlowController flow;
    private final Map<Object, Object> dataset = new HashMap<>();
    private final HttpHooks hooks;
    private final Matcher matcher;

    public HttpEndPointContextImpl(Builder builder) {
        this.request = builder.request;
        this.response = builder.response;
        this.cookies = builder.cookies;
        this.session = builder.session;
        this.flow = builder.flow;
        this.hooks = builder.hooks;
        this.matcher = builder.matcher;
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

    @Override
    public HttpFlowController flow() {
        return this.flow;
    }

    @Override
    public HttpHooks hooks() {
        return this.hooks;
    }

    @Override
    public Matcher matcher() {
        return this.matcher;
    }

    static HttpEndPointContext.Builder builder() {
        return new Builder();
    }

    private static class Builder implements HttpEndPointContext.Builder {
        private HttpRequest request;
        private HttpResponse.Builder response;
        public Cookies cookies;
        private Session session;
        private HttpFlowController flow;
        private HttpHooks hooks;
        private Matcher matcher;

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
        public Builder withFlowController(HttpFlowController flow) {
            this.flow = flow;
            return this;
        }

        @Override
        public Builder withHooks(HttpHooks hooks) {
            this.hooks = hooks;
            return this;
        }

        @Override
        public Builder withMatcher(Matcher matcher) {
            this.matcher = matcher;
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

