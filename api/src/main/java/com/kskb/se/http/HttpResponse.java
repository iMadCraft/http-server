package com.kskb.se.http;

public interface HttpResponse extends HttpPacket {
    int code();

    interface Builder extends HttpPacket.Builder<Builder> {
        Builder withResponseCode(int code);

        HttpResponse build();
    }
}
