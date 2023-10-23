package com.kskb.se.http;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
      int index = 0;
      while (index < 256 &&
             index < data.length &&
             data[index] != 0)
         index ++;
      return "Unknown request [" + data.length + "]: " + new String(Arrays.copyOf(data, index), StandardCharsets.US_ASCII);
   }
}