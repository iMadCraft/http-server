package com.kskb.se.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class HttpConnectionImpl implements HttpConnection {
    private final Socket socket;
    private final OutputStream output;
    private final InputStream input;

    public HttpConnectionImpl(Builder builder) {
        this.socket = builder.socket;
        this.output = builder.output;
        this.input = builder.input;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    @Override
    public void close() throws HttpServerException {
        try {
            this.input.close();
            this.output.close();
            this.socket.close();
        } catch (IOException e) {
            throw new HttpServerException("Unable to close the client resources", e);
        }
    }

    static class Builder implements HttpConnection.Builder {
        private Socket socket;
        private OutputStream output;
        private InputStream input;

        @Override
        public Builder withSocket(Socket socket) {
            this.socket = socket;
            return this;
        }

        @Override
        public Builder withOutputStream(OutputStream output) {
            this.output = output;
            return this;
        }

        @Override
        public Builder withInputStream(InputStream input) {
            this.input = input;
            return this;
        }

        @Override
        public HttpConnection build() {
            return new HttpConnectionImpl(this);
        }
    }
}
