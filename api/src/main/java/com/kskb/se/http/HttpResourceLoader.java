package com.kskb.se.http;

import java.io.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface HttpResourceLoader {
   Optional<HttpTemplate> load(String string);
   Optional<HttpTemplate> load(String string, Consumer<HttpTemplate> newResource);

   static HttpResourceLoader create() {
      return new HttpResourceLoaderImpl();
   }
}

class HttpResourceLoaderImpl implements HttpResourceLoader {
   private static final Function<String, String> ourLoader = HttpResourceLoaderImpl.getResourceLoader();

   @Override
   public Optional<HttpTemplate> load(String name) {
      return this.load(name, null);
   }

   @Override
   public Optional<HttpTemplate> load(String name, Consumer<HttpTemplate> newResource) {
      final var stream = ourLoader.apply(name);
      if (stream == null)
         return Optional.empty();
      final var template = HttpTemplate.create(stream);
      if(newResource != null)
         newResource.accept(template);
      return Optional.of(template);
   }

   public static Function<String, String> getResourceLoader() {
      File checkFile = new File("sample/src/main/html/pages/index.html");
      if(checkFile.exists()) {
         return (name) -> {
            String result = null;
            try {
               File file = new File("sample/src/main/html/pages/" + name);
               if (file.exists()) {
                  result = readAll(new FileInputStream(file));
               }
            } catch (Throwable ignored) {}
            return result;
         };
      }
      else {
         return (name) -> {
            String result = null;
            try {
               result = readAll(HttpResourceLoaderImpl.class.getClassLoader().getResourceAsStream(name));
            } catch (Throwable ignored) {}
            return result;
         };
      }
   }

   public static String readAll(InputStream stream) throws IOException {
      if (stream == null)
         return null;

      StringBuilder data = new StringBuilder();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      String line = reader.readLine();
      while (line != null) {
         data.append(line).append('\n');
         line = reader.readLine();
      }

      return data.toString();
   }
}