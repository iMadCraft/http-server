package com.kskb.se.http;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import static com.kskb.se.http.HttpResourceType.TEXT;

public interface HttpResourceLoader {
   <T extends HttpResource> Optional<T> load(Class<T> type, String name);
   <T extends HttpResource> Optional<T> load(Class<T> type, String name, Consumer<T> newResource);

   static HttpResourceLoader create(HttpResourceLocator locator) {
      return new HttpResourceLoaderImpl(locator);
   }
}

class HttpResourceLoaderImpl implements HttpResourceLoader {
   private final HttpResourceLocator locator;

   HttpResourceLoaderImpl(HttpResourceLocator locator) {
     this.locator = locator;
   }

   @Override
   public <T extends HttpResource> Optional<T> load(Class<T> type, String name) {
      return this.load(type, name, null);
   }

   @Override
   public <T extends HttpResource> Optional<T> load(Class<T> type, String name, Consumer<T> newResource) {
      final T resource = loadResource(type, name);
      if (resource == null)
         return Optional.empty();
      if(newResource != null)
         newResource.accept(resource);
      return Optional.of(resource);
   }

   @SuppressWarnings("unchecked")
   public <T extends HttpResource> T loadResource(Class<T> type, String name) {
      try {
         final var property = type.getAnnotation(HttpResourceProperty.class);
         final var location = property.location();

         InputStream stream = null;
         for (final String candidate: this.locator.getCandidates(location, name)) {
            try {
               if(candidate.startsWith("resource://")) {
                  final var strippedPath = candidate.substring("resource://".length());
                  stream = HttpResourceLoader.class.getResourceAsStream(strippedPath);
               }
               else {
                  // TODO: Later also used for checking permissions
                  // Get the absolute path and resolve all symbolic links
                  final var normalizedPath = Path.of(candidate)
                     .toAbsolutePath()
                     .normalize()
                     .toString();

                  stream = new FileInputStream(normalizedPath);
               }
            } catch (IOException ignored) {}

            if (stream != null)
               break;
         }

         // Resource does not exist or is not allowed
         if (stream == null)
            return null;

         final Object arg;
         final Method method;
         if (property.type() == TEXT) {
            arg = readAll(stream);
            method = type.getMethod("create", String.class);
         }
         else {
            arg = stream;
            method = type.getMethod("create", InputStream.class);
         }
         final Object obj = method.invoke(null, arg);
         return (T) obj;
      }
      catch (Exception ignored) {}
      return null;
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