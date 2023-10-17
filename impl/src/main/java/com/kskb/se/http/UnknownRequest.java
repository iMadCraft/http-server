package com.kskb.se.http;

import java.nio.charset.StandardCharsets;

public interface UnknownRequest extends Request {
   static UnknownRequest create(byte[] data) {
      return new UnknownRequestImpl(data);
   }
}

class UnknownRequestImpl implements UnknownRequest {
   private final byte[] data;
   UnknownRequestImpl(byte[] data) {
     this.data = data;
   }

   @Override
   public String toString() {
      return new String(data, StandardCharsets.UTF_8);
   }
}