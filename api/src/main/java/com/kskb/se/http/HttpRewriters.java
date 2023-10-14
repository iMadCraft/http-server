package com.kskb.se.http;

import java.util.*;
import java.util.stream.Collectors;

public interface HttpRewriters extends Iterable<HttpRewriter> {
   void add(HttpMethod method, List<String> url, HttpRewriter handler);

   static HttpRewriters create() {
      return new HttpRewritersImpl();
   }

   Iterable<HttpRewriter> match(HttpRequest request);
}

class HttpRewritersImpl implements HttpRewriters {
   final List<RewriterEntry> entryList = new ArrayList<>();

   @Override
   public Iterable<HttpRewriter> match(HttpRequest request) {
      List<HttpRewriter> matches = new ArrayList<>();
      for (RewriterEntry entry: entryList) {
         final boolean condition =
            entry.method == request.method() &&
               entry.url.contains(request.url());
         if(condition)
            matches.add(entry.endPoint);
      }
      return Collections.unmodifiableList(matches);
   }

   @Override
   public void add(HttpMethod method, List<String> url, HttpRewriter handler) {
      this.entryList.add(new RewriterEntry(method, Collections.unmodifiableList(url), handler));
   }

   @Override
   public Iterator<HttpRewriter> iterator() {
      return entryList.stream()
         .map((e) -> e.endPoint)
         .iterator();
   }

   private record RewriterEntry(HttpMethod method, List<String> url, HttpRewriter endPoint) {}
}