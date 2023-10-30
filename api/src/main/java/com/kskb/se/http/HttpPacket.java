package com.kskb.se.http;

import java.util.Optional;

public interface HttpPacket {
    HttpHeaders headers();
    Optional<HttpResource> payload();

    interface Builder<T extends Builder<T>> extends HttpPacket {
        HttpHeaders headers();
        Optional<HttpResource> payload();

        default boolean hasPayload() { return payload().isPresent(); }
        default boolean hasNotPayload() { return payload().isEmpty(); }

        T addHeader(HttpHeader httpHeader);

        Builder withPayload(HttpResource payload);
        Builder withPayload(Optional<? extends HttpResource> payload);
    }
}
