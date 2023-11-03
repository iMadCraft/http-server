package com.kskb.se.http;

public interface HttpInitialHookContext extends HttpHookContext {
   HttpFlowController flow();
   HttpRequest.Builder request();
   HttpResponse.Builder response();

   static HttpInitialHookContext.Builder builder() {
      return HttpInitialHookContextImpl.builder();
   }

   interface Builder {
      Builder withFlowController(HttpFlowController flow);
      Builder withRequest(HttpRequest.Builder builder);
      Builder withResponse(HttpResponse.Builder builder);
      HttpInitialHookContext build();
   }
}

final class HttpInitialHookContextImpl implements HttpInitialHookContext {
   private final HttpFlowController flow;
   private final HttpRequest.Builder request;
   private final HttpResponse.Builder response;

   private HttpInitialHookContextImpl(Builder builder) {
      this.flow = builder.flow;
      this.request = builder.request;
      this.response = builder.response;
   }

   @Override
   public HttpFlowController flow() {
      return this.flow;
   }

   @Override
   public HttpRequest.Builder request() {
      return this.request;
   }

   @Override
   public HttpResponse.Builder response() {
      return this.response;
   }

   @Override
   public HttpHookType type() {
      return HttpHookType.INITIAL;
   }

   static HttpInitialHookContext.Builder builder() {
      return new Builder();
   }

   private static final class Builder implements HttpInitialHookContext.Builder {
      private HttpFlowController flow;
      private HttpRequest.Builder request;
      private HttpResponse.Builder response;

      @Override
      public Builder withFlowController(HttpFlowController flow) {
         this.flow = flow;
         return this;
      }

      @Override
      public Builder withRequest(HttpRequest.Builder request) {
         this.request = request;
         return this;
      }

      @Override
      public Builder withResponse(HttpResponse.Builder response) {
         this.response = response;
         return this;
      }

      @Override
      public HttpInitialHookContext build() {
         return new HttpInitialHookContextImpl(this);
      }
   }
}