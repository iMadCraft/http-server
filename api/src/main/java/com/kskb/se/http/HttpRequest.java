package com.kskb.se.http;

import java.net.URI;

public interface HttpRequest extends HttpPacket, Request {
    HttpMethod method();
    URI uri();
    String version();
    String originalUrl();
    String query(String key);
    Cookies cookies();

    default String path() { return uri().getPath(); }
    default String query() { return uri().getQuery(); }

    default String extension() {
        final var parts = path().split("\\.");
        return parts.length > 1 ? parts[parts.length - 1] : null;
    }


    interface Builder extends HttpRequest, HttpPacket.Builder<Builder> {
        HttpRequest.Builder withMethod(HttpMethod method);
        HttpRequest.Builder withUri(URI uri);
        HttpRequest.Builder withVersion(String version);
        HttpRequest.Builder withCookies(Cookies cookies);

        HttpRequest build();

    }
}
