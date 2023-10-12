package com.kskb.se.http;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    public static HttpMethod softValueOf(String method) {
        HttpMethod result = null;
        switch (method) {
            case "GET":
                result = GET;
                break;
            case "POST":
                result = POST;
                break;
            case "PUT":
                result = PUT;
                break;
            case "DELETE":
                result = DELETE;
                break;
        }
        return result;
    }
}
