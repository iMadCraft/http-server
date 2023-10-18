package com.kskb.se.http;

import static com.kskb.se.http.HttpResourceLocation.HTDOCS;
import static com.kskb.se.http.HttpResourceLocation.HTML;
import static com.kskb.se.http.HttpResourceType.TEXT;

@HttpResourceProperty(location = HTDOCS, type = TEXT)
public interface HttpPlainText extends HttpTemplate {
   static HttpPlainText create(String template) {
      return new HttpPlainTextImpl(template);
   }
}

class HttpPlainTextImpl extends AbstractHttpTemplate implements HttpPlainText {
   HttpPlainTextImpl(String text) {
      super(text);
   }

   @Override
   public String contentType() {
      return "text/plain";
   }
}