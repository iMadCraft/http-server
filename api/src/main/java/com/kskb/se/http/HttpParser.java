package com.kskb.se.http;

import java.io.InputStream;

public interface HttpParser {
    void parse(HttpRequest.Builder builder, InputStream input) throws HttpServerException;
}
