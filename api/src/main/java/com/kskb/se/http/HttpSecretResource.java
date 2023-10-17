package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.SECRET;
import static com.kskb.se.http.HttpResourceType.DATA;

@HttpResourceProperty(location = SECRET, type = DATA)
public interface HttpSecretResource extends HttpBinaryResource {
   static HttpSecretResource create(byte[] bytes) {
      return new HttpSecretResourceImpl(bytes);
   }
}

class HttpSecretResourceImpl extends AbstractBinaryResource implements HttpSecretResource {
   HttpSecretResourceImpl(byte[] bytes) {
      super(bytes);
   }
}