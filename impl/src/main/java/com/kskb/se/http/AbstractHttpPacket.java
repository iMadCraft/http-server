package com.kskb.se.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

abstract class AbstractHttpPacket implements HttpPacket {
   private final HttpHeaders headers;
   private final HttpResource payload;

   protected AbstractHttpPacket(Builder<?> builder) {
      this.headers = HttpHeaders.create(builder.headers);
      this.payload = builder.payload;
   }

   @Override
   public HttpHeaders headers() {
      return this.headers;
   }

   @Override
   public Optional<HttpResource> payload() {
      return Optional.ofNullable(this.payload);
   }

   protected static abstract class Builder<T extends HttpPacket.Builder<T>> implements HttpPacket.Builder<T> {
      private HttpResource payload;
      private final List<HttpHeader> headers = new ArrayList<>();


      protected Builder() {
      }

      @Override
      public Optional<HttpResource> payload() {
         return Optional.ofNullable(this.payload);
      }

      @Override
      public HttpHeaders headers() {
         return HttpHeaders.create(this.headers);
      }

      @Override
      public T addHeader(HttpHeader header) {
         this.headers.add(header);
         return castThis();
      }

      @Override
      public T withPayload(HttpResource payload) {
         this.payload = payload;
         return castThis();
      }

      @Override
      public T withPayload(Optional<? extends HttpResource> payload) {
         this.payload = payload.orElse(null);
         return castThis();
      }

      @SuppressWarnings("unchecked")
      private T castThis() {
         return (T) this;
      }
   }
}
