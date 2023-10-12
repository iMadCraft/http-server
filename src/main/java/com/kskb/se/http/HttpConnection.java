package com.kskb.se.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface HttpConnection {

    static Builder builder() {
        return HttpConnectionImpl.builder();
    }

    InputStream input();

    OutputStream output();

    void close() throws HttpServerException;

    interface Builder {
        Builder withSocket(Socket socket);

        Builder withOutputStream(OutputStream outputStream);

        Builder withInputStream(InputStream inputStream);

        HttpConnection build();
    }
}
