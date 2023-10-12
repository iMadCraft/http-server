package com.kskb.se.http;

import java.io.InputStream;

public interface HttpParser {
    HttpRequest parse(InputStream input) throws HttpServerException;
}
