package com.kskb.se.http;

public class HttpFactory {

    public static HttpRequest.Builder createRequestBuilder() {
        return HttpRequestImpl.builder();
    }

    public static HttpResponse.Builder createResponseBuilder() {
        return HttpResponseImpl.builder();
    }

    public static HttpResponse.Builder createResponseBuilder(HttpRequest from) {
        return HttpResponseImpl.builder();
    }

    public static HttpServer createServer(HttpServerContext context) {
        return new HttpServerImpl(context);
    }
}
