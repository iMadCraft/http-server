package com.kskb.se.http;

public interface HttpPacket {
    HttpMethod method();
    String url();
    String version();
    Iterable<HttpHeader> headers();
    String payload();

    interface Builder<T extends Builder<T>> {
        HttpMethod method();
        String url();
        T withMethod(HttpMethod method);
        T withUrl(String url);
        T withVersion(String version);
        T addHeader(HttpHeader httpHeader);
        T withPayload(String payload);
    }
}
