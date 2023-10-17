package com.kskb.se.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.kskb.se.http.HttpResourceType.DATA;
import static com.kskb.se.http.HttpResourceType.TEXT;

public interface HttpResource {
   long size();
   HttpResourceType type();
   String contentType();
   byte[] bytes();

   default InputStream stream() {
      return new ByteArrayInputStream(bytes());
   }

   default boolean isText() { return TEXT.equals(type()); }
   default boolean isData() { return DATA.equals(type()); }

}
