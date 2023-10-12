package com.kskb.se.http;

public interface HttpHeader {

    static HttpHeader create(String name, String value) {
        return new HttpHeaderImpl(name, value);

    }

    String name();

    String value();
}

record HttpHeaderImpl(String name, String value) implements HttpHeader {}
