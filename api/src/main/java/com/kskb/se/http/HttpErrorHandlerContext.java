package com.kskb.se.http;

import com.kskb.se.base.Nullable;

public interface HttpErrorHandlerContext {

   static Builder builder() {
      return HttpErrorHandlerContextImpl.builder();
   }

   interface Builder {
      Builder withRequest(HttpRequest request);
      Builder withResponse(HttpResponse response);
      Builder clear();

      HttpErrorHandlerContext build();
   }
}

class HttpErrorHandlerContextImpl implements HttpErrorHandlerContext {
   private final @Nullable HttpRequest request;
   private final @Nullable HttpResponse response;

   HttpErrorHandlerContextImpl(Builder builder) {
      this.request = builder.request;
      this.response = builder.response;
   }

   static Builder builder() {
      return new Builder();
   }

   private static class Builder implements HttpErrorHandlerContext.Builder {
      private HttpRequest request;
      private HttpResponse response;

      @Override
      public Builder withRequest(HttpRequest request) {
         this.request = request;
         return this;
      }

      @Override
      public Builder withResponse(HttpResponse response) {
         this.response = response;
         return this;
      }

      @Override
      public Builder clear() {
         this.request = null;
         this.response = null;
         return this;
      }

      @Override
      public HttpErrorHandlerContext build() {
         return new HttpErrorHandlerContextImpl(this);
      }
   }
}