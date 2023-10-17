package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.ICO;
import static com.kskb.se.http.HttpResourceType.DATA;

@HttpResourceProperty(location = ICO, type = DATA)
public interface HttpIconImage extends HttpBinaryResource {
   static HttpIconImage create(byte[] bytes) {
      return new HttpIconImageImpl(bytes);
   }
}

class HttpIconImageImpl extends AbstractBinaryResource implements HttpIconImage {
   HttpIconImageImpl(byte[] data) {
      super(data);
   }

   @Override
   public String contentType() {
      return "image/x-icon";
   }
}