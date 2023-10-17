package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.CSS;
import static com.kskb.se.http.HttpResourceType.TEXT;

@HttpResourceProperty(location = CSS, type = TEXT)
public interface HttpStyle extends HttpTemplate {
   static HttpTemplate create(String template) {
      return new HttpStyleImpl(template);
   }
}

class HttpStyleImpl extends AbstractHttpTemplate implements HttpHyperText {
   HttpStyleImpl(String text) {
      super(text);
   }

   @Override
   public String contentType() {
      return "text/css";
   }
}