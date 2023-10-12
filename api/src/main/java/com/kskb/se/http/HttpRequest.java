package com.kskb.se.http;

public interface HttpRequest extends HttpPacket {
    interface Builder extends HttpPacket.Builder<Builder> {
        HttpRequest build();
    }
}
