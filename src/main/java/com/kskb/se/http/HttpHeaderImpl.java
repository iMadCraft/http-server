package com.kskb.se.http;

public class HttpHeaderImpl implements HttpHeader {
    private final String name;
    private final String value;

    public HttpHeaderImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String value() {
        return this.value;
    }
}
