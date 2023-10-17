package com.kskb.se.http;

import java.net.URI;

public interface HttpRequest extends HttpPacket, Request {
    HttpMethod method();
    URI uri();
    String version();
    String originalUrl();
    String query(String key);

    interface Builder extends HttpPacket.Builder<Builder> {
        HttpMethod method();
        URI uri();
        String version();

        Builder withMethod(HttpMethod method);
        Builder withUri(URI uri);
        Builder withVersion(String version);

        HttpRequest build();
    }
}
