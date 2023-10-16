package com.kskb.se.http;

public interface HttpResponse extends HttpPacket {
    int code();
    String details();

    interface Builder extends HttpPacket.Builder<Builder> {
        int code();

        Builder withResponseCode(int code);
        Builder withDetails(String message);

        HttpResponse build();

    }
}
