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

   default String codeAsText() {
      // TODO: critical, add more!
      return switch (code()) {
         case 404 -> "Not Found";
         case 500 -> "Internal Error";
         default -> "OK";
      };
   }


   interface Builder extends HttpPacket.Builder<Builder> {
      int code();

      Builder withResponseCode(int code);
      Builder withDetails(String message);

      HttpResponse build();

   }
}
