package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@interface Stringable {}

public interface HttpTemplate {
   void bind(String label, @Stringable Object value);
   void bind(String label, @Stringable Supplier<Object> value);

   String toString();
   static HttpTemplate create(String template) {
      return new HttpTemplateImpl(template);
   }
}

class HttpTemplateImpl implements HttpTemplate {
   private final String template;
   private final Map<String, Object> binds = new HashMap<>();

   HttpTemplateImpl(String template) {
      this.template = template;
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