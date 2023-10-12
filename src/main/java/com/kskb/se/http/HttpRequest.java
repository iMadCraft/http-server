package com.kskb.se.http;

public interface HttpRequest extends HttpPacket {
    static Builder builder() {
        return HttpRequestImpl.builder();
    }

    interface Builder extends HttpPacket.Builder<Builder> {
        HttpRequest build();
    }
}
