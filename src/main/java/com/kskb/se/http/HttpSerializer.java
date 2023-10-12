package com.kskb.se.http;

import java.io.OutputStream;

public interface HttpSerializer {
    void serialize(OutputStream output, HttpResponse response) throws HttpServerException;
}
