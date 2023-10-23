package com.kskb.se.http;

public interface HttpErrorHandler {
   void onException(HttpErrorHandlerContext context, Throwable t);
}
