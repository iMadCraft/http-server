package com.kskb.se.http;

import java.io.OutputStream;
import java.io.PrintWriter;

public class HttpSerializerImpl implements HttpSerializer {
    @Override
    public void serialize(OutputStream output, HttpResponse response) throws HttpServerException {
        final var writer = new PrintWriter(output);
        writer.printf("HTTP/%.3s %d %s%c%c", response.version(), response.code(), "OK", 0x0D, 0x0A);
        for (final var header : response.headers()) {
            writer.printf("%s: %s%c%c", header.name(), header.value(), 0x0D, 0x0A);
        }
        writer.printf("%c%c%s", 0x0D, 0x0A, response.payload());
        writer.flush();
    }
}

/**
 * HTTP/2 200 OK
 * content-type: text/html
 * server: Microsoft-IIS/8.5
 * date: Thu, 12 Oct 2023 18:34:44 GMT
 **/