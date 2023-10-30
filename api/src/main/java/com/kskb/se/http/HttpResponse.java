package com.kskb.se.http;

import java.util.Optional;

public interface HttpResponse extends HttpPacket {
   int code();
   String details();
   default String version() {
      // TODO: critical, implement!
      return "1.1";
   }
   Optional<HttpResource> payload();

   String codeAsText();

   interface Builder extends HttpPacket.Builder<Builder> {
      int code();

      HttpResponse.Builder withResponseCode(int code);
      HttpResponse.Builder withDetails(String message);

      HttpResponse build();

   }
}
