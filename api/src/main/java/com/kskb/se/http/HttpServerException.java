package com.kskb.se.http;

public class HttpServerException extends Exception {

    public HttpServerException(String message) {
        super(message);
    }

    public HttpServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
