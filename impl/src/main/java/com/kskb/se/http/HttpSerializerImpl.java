package com.kskb.se.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class HttpSerializerImpl implements HttpSerializer {

    @Override
    public void serialize(OutputStream output2, HttpResponse response) throws HttpServerException {
        try {
            final var output = new BufferedOutputStream(output2);
            output.write(String.format("HTTP/%.3s %d %s", response.version(), response.code(), response.codeAsText()).getBytes());
            output.write(new byte[]{ 0x0D, 0x0A });
            for (final var header : response.headers()) {
                output.write(String.format("%s: %s", header.name(), header.value()).getBytes());
                output.write(new byte[]{ 0x0D, 0x0A });
            }
            output.write(new byte[]{ 0x0D, 0x0A });

            if (response.payload().isPresent()) {
                byte[] bytes = response.payload().get().bytes();
                output.write(bytes);
            }
            output.flush();
        }
        catch (IOException e) {
            throw new HttpServerException("Unable to serialize response", e);
        }
    }
}