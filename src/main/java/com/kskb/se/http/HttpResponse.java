package com.kskb.se.http;

public interface HttpResponse extends HttpPacket {

    static Builder builder(HttpRequest from) {
        return HttpResponseImpl.builder(from);
    }

    int code();

    interface Builder extends HttpPacket.Builder<Builder> {
        Builder withResponseCode(int code);

        HttpResponse build();
    }
}
