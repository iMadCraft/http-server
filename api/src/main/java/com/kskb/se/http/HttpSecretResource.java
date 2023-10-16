package com.kskb.se.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.kskb.se.http.HttpResourceLocation.SECRET;
import static com.kskb.se.http.HttpResourceType.DATA;

@HttpResourceProperty(location = SECRET, type = DATA)
public interface HttpSecretResource extends HttpResource {
   InputStream stream();

   static HttpSecretResource create(InputStream stream) {
      return new HttpSecretResourceImpl(stream);
   }
}

class HttpSecretResourceImpl implements HttpSecretResource {
   private final InputStream stream;

   HttpSecretResourceImpl(InputStream stream) {
      this.stream = stream;
   }

   @Override
   public InputStream stream() {
      return this.stream;
   }
}