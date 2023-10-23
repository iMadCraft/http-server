package com.kskb.se.http;

import java.net.URI;
import java.util.Calendar;

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

    interface Builder extends HttpPacket.Builder<Builder> {
        HttpMethod method();
        URI uri();
        String version();

        Builder withMethod(HttpMethod method);
        Builder withUri(URI uri);
        Builder withVersion(String version);
        Builder withCookies(Cookies cookies);

        HttpRequest build();

    }
}
