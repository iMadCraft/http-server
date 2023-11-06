package com.kskb.se.http;

public interface HttpPostHookContext extends HttpHookContext {
   Session session();
   HttpRequest request();
   HttpResponse.Builder response();

   static HttpPostHookContext.Builder builder() {
      return HttpPostHookContextImpl.builder();
   }


   interface Builder {
      Builder withSession(Session session);
      Builder withRequest(HttpRequest builder);
      Builder withResponse(HttpResponse.Builder builder);
      HttpPostHookContext build();
   }
}

final class HttpPostHookContextImpl implements HttpPostHookContext {
   private final Session session;
   private final HttpRequest request;
   private final HttpResponse.Builder response;

   private HttpPostHookContextImpl(Builder builder) {
      this.session = builder.session;
      this.request = builder.request;
      this.response = builder.response;
   }

   @Override
   public Session session() {
      return session;
   }

   @Override
   public HttpRequest request() {
      return request;
   }

   @Override
   public HttpResponse.Builder response() {
      return response;
   }

   @Override
   public HttpHookType type() {
      return HttpHookType.POST;
   }

   static HttpPostHookContext.Builder builder() {
      return new Builder();
   }

   private static final class Builder implements HttpPostHookContext.Builder {
      private Session session;
      private HttpRequest request;
      private HttpResponse.Builder response;

      @Override
      public Builder withSession(Session session) {
         this.session = session;
         return this;
      }

      @Override
      public Builder withRequest(HttpRequest request) {
         this.request = request;
         return this;
      }

      @Override
      public Builder withResponse(HttpResponse.Builder response) {
         this.response = response;
         return this;
      }

      @Override
      public HttpPostHookContext build() {
         return new HttpPostHookContextImpl(this);
      }
   }
}