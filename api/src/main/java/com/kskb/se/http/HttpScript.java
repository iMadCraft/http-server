package com.kskb.se.http;

import java.nio.charset.Charset;

import static com.kskb.se.http.HttpResourceLocation.JAVASCRIPT;
import static com.kskb.se.http.HttpResourceType.TEXT;

@HttpResourceProperty(location = JAVASCRIPT, type = TEXT)
public interface HttpScript extends HttpTemplate {
   static HttpTemplate create(@NotNull String template) {
      return HttpScript.create(template, null);
   }
   static HttpTemplate create(@NotNull String template, @Nullable Charset charset) {
      return new HttpScriptImpl(template, charset);
   }
}

class HttpScriptImpl extends AbstractHttpTemplate implements HttpHyperText {
   private final Charset charset;

   HttpScriptImpl(@NotNull String text, @Nullable Charset charset) {
      super(text);
      this.charset = charset;
   }

   @Override
   public String contentType() {
      return "text/javascript" + ( charset != null ? "; charset=" + charset.name().toLowerCase() : "");
   }
}