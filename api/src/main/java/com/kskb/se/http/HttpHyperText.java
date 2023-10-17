package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.HTML;
import static com.kskb.se.http.HttpResourceType.TEXT;

@HttpResourceProperty(location = HTML, type = TEXT)
public interface HttpHyperText extends HttpTemplate {
   static HttpHyperText create(String template) {
      return new HttpHyperTextImpl(template);
   }
}

class HttpHyperTextImpl extends AbstractHttpTemplate implements HttpHyperText {
   HttpHyperTextImpl(String text) {
      super(text);
   }

   @Override
   public String contentType() {
      return "text/html";
   }
}