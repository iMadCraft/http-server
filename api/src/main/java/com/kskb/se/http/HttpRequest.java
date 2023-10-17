package com.kskb.se.http;

public interface HttpRequest extends HttpPacket, Request {
    HttpMethod method();
    String url();
    String version();
    String originalUrl();
    interface Builder extends HttpPacket.Builder<Builder> {
        HttpMethod method();
        String url();
        String version();

        Builder withMethod(HttpMethod method);
        Builder withUrl(String url);
        Builder withVersion(String version);

        HttpRequest build();
    }
}
