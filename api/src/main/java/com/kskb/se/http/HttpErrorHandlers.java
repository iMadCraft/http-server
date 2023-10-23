package com.kskb.se.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface HttpErrorHandlers extends Iterable<HttpErrorHandler> {
   int size();
   void add(HttpErrorHandler errorHandler);

   default boolean isEmpty() {
      return size() == 0;
   }

   static HttpErrorHandlers create() {
      return new HttpErrorHandlersImpl();
   }
}

class HttpErrorHandlersImpl implements HttpErrorHandlers {
   private final List<HttpErrorHandler> handlers = new ArrayList<>();

   @Override
   public int size() {
      return handlers.size();
   }

   @Override
   public void add(HttpErrorHandler errorHandler) {
      this.handlers.add(errorHandler);
   }

   @Override
   public Iterator<HttpErrorHandler> iterator() {
      return handlers.iterator();
   }

}
