package com.kskb.se.http;

import java.io.InputStream;

// TODO: minor, possible removal and just used reader directly
public interface HttpParser {
    boolean parse(HttpRequest.Builder builder, InputStream input) throws HttpServerException;
}
