package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.SECRET;
import static com.kskb.se.http.HttpResourceType.DATA;

@HttpResourceProperty(location = SECRET, type = DATA)
public interface HttpSecretResource extends HttpResource {
   static HttpSecretResource create(byte[] bytes) {
      return new HttpSecretResourceImpl(bytes);
   }
}

class HttpSecretResourceImpl implements HttpSecretResource {
   private final byte[] bytes;

   HttpSecretResourceImpl(byte[] bytes) {
      this.bytes = bytes;
   }

   @Override
   public long size() {
      return this.bytes.length;
   }

   @Override
   public HttpResourceType type() {
      return HttpResourceType.DATA;
   }

   @Override
   public String contentType() {
      return "application/octet-stream";
   }

   @Override
   public byte[] bytes() {
      return this.bytes;
   }
}