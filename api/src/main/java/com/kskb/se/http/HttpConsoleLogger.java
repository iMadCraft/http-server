package com.kskb.se.http;

public class HttpConsoleLogger implements HttpErrorHandler {
   @Override
   public void onException(HttpErrorHandlerContext context, Throwable t) {
      if (t instanceof HttpServerException) {
         System.err.println(t.getMessage());
         if(t.getCause() != null) {
            t.printStackTrace(System.err);
         }
      }
      else {
         t.printStackTrace(System.err);
      }
   }
}
