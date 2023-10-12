package com.kskb.se.http;

public interface HttpServerContext {

    static Builder builder() {
        return HttpServerContextImpl.builder();
    }

    int port();

    HttpParser parser();

    HttpSerializer serializer();


    interface Builder {
        Builder withPort(int port);

        HttpServerContext build();
    }
}
