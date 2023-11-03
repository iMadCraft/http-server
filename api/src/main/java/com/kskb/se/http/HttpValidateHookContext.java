package com.kskb.se.http;

public interface HttpValidateHookContext extends HttpHookContext {
   Session session();
   HttpFlowController flow();
   HttpRequest.Builder request();
   HttpResponse.Builder response();

   default int port() { return request().port(); }
   default String host() { return request().host(); }
   default String path() { return request().path(); }

   static HttpValidateHookContext.Builder builder() {
      return HttpValidateHookContextImpl.builder();
   }

   interface Builder {
      Builder withSession(Session session);
      Builder withFlowController(HttpFlowController flow);
      Builder withResponse(HttpResponse.Builder builder);
      Builder withRequest(HttpRequest.Builder builder);
      HttpValidateHookContext build();
   }
}

final class HttpValidateHookContextImpl implements HttpValidateHookContext {
   private final Session session;
   private final HttpFlowController flow;
   private final HttpRequest.Builder request;
   private final HttpResponse.Builder response;

   private HttpValidateHookContextImpl(Builder builder) {
      this.session = builder.session;
      this.flow = builder.flow;
      this.request = builder.request;
      this.response = builder.response;
   }

   @Override
   public Session session() {
      return session;
   }

   @Override
   public HttpFlowController flow() {
      return flow;
   }

   public HttpRequest.Builder request() {
      return request;
   }

   @Override
   public HttpResponse.Builder response() {
      return response;
   }

   @Override
   public HttpHookType type() {
      return HttpHookType.VALIDATE;
   }

   static HttpValidateHookContext.Builder builder() {
      return new Builder();
   }

   private static final class Builder implements HttpValidateHookContext.Builder {
      private Session session;
      private HttpFlowController flow;
      private HttpRequest.Builder request;
      private HttpResponse.Builder response;

      @Override
      public Builder withFlowController(HttpFlowController flow) {
         this.flow = flow;
         return this;
      }

      @Override
      public Builder withSession(Session session) {
         this.session = session;
         return this;
      }

      @Override
      public Builder withResponse(HttpResponse.Builder response) {
         this.response = response;
         return this;
      }

      @Override
      public Builder withRequest(HttpRequest.Builder request) {
         this.request = request;
         return this;
      }

      @Override
      public HttpValidateHookContext build() {
         return new HttpValidateHookContextImpl(this);
      }
   }
}