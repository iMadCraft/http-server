package com.kskb.se.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface HttpTemplate extends HttpResource {
   void bind(String label, @Stringable Object value);
   void bind(String label, @Stringable Supplier<Object> value);
   String toString();
}

abstract class AbstractHttpTemplate implements HttpTemplate {
   private final String template;
   private final Map<String, Object> binds = new HashMap<>();

   AbstractHttpTemplate(String template) {
      this.template = template;
   }

   @Override
   public long size() {
      return bytes().length;
   }

   @Override
   public HttpResourceType type() {
      return HttpResourceType.TEXT;
   }

   @Override
   public byte[] bytes() {
      // TODO: minor, charset can not be assumed, extract from meta tag charset
      return toString().getBytes(StandardCharsets.UTF_8);
   }

   @Override
   public void bind(String label, @Stringable Object value) {
      binds.put(label, value);
   }

   @Override
   public void bind(String label, @Stringable Supplier<Object> value) {
      binds.put(label, value);
   }

   @Override
   public String toString() {
      String transformed = template;
      for (Map.Entry<String, Object> entry: binds.entrySet()) {
         final var value = entry.getValue();
         final var valueAsString = value instanceof Supplier<?> ?
            ((Supplier<?>)value).get().toString() : value.toString();
         transformed = transformed.replace("{{ " + entry.getKey() + " }}", valueAsString);
      }
      return transformed;
   }
}