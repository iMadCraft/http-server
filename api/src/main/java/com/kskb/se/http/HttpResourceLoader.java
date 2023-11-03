package com.kskb.se.http;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
   private final Map<String, HttpResource> cache = new HashMap<>();

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
      Object cached = cache.get(name);
      if (cached != null) {
         System.out.printf("Loaded cached %.4096s resource%n", name);
         return (T) cached;
      }

      try {
         final var property = type.getAnnotation(HttpResourceProperty.class);
         final var location = property.location();

         InputStream stream = null;
         final var candidates = this.locator.getCandidates(location, name);
         for (final String candidate: candidates) {
            try {
               if(candidate.startsWith("resource://")) {
                  final var strippedPath = candidate.substring("resource://".length())
                     .replaceAll("//", "/");

                  stream = HttpResourceLoader.class.getResourceAsStream(strippedPath);
               }
               else if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
                  // TODO: minor, Change the use of JDK http client
                  //       to internal client, when implemented.
                  // TODO: major, improve security.
                  final java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                     .version(java.net.http.HttpClient.Version.HTTP_1_1)
                     .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                     .connectTimeout(Duration.ofSeconds(20))
                     .build();
                  final java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                     .GET()
                     .timeout(Duration.ofSeconds(3))
                     .uri(URI.create(candidate))
                     .build();
                  java.net.http.HttpResponse<String> response =
                     client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                  if (response.statusCode() >= 200 && response.statusCode() <= 210) {
                     stream = new ByteArrayInputStream(response.body().getBytes(StandardCharsets.US_ASCII));
                  }
               }
               else {
                  // TODO: major, Later also used for checking permissions
                  // Get the absolute path and resolve all symbolic links
                  final var normalizedPath = Path.of(candidate)
                     .toAbsolutePath()
                     .normalize()
                     .toString();

                  stream = new FileInputStream(normalizedPath);
               }
            } catch (IOException ignored) {}

            if (stream != null) {
               System.out.printf("Loaded %.4096s%n", candidate);
               break;
            }
            else {
               System.out.printf("Candidate %.4096s does not exists%n", candidate);
            }
         }

         // Resource does not exist or is not allowed
         if (stream == null) {
            if (candidates.iterator().hasNext()) {
               System.out.printf("No candidates for %.4096s was found%n", name);
            }
            return null;
         }

         final Object arg;
         final Method method;
         if (property.type() == TEXT) {
            arg = readAll(stream);
            method = type.getMethod("create", String.class);
         }
         else {
            arg = stream.readAllBytes();
            method = type.getMethod("create", byte[].class);
         }
         final Object obj = method.invoke(null, arg);
         cache.put(name, (T) obj);
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