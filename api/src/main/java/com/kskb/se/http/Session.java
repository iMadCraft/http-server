package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface Session {
   String SESSION_COOKIE_NAME = "session";

   UUID id();
   Cookies cookies();
   Map<Object, Object> dataset();

   default Cookie cookie() {
      return cookies().get(SESSION_COOKIE_NAME)
         .orElseThrow();
   }

   static Session.Builder builder() {
      return SessionImpl.builder();
   }


   interface Builder {
      Builder withId(UUID id);
      Builder withCookies(Cookies cookies);
      Session build();
   }
}

class SessionImpl implements Session {
   private final UUID id;
   private final Cookies cookies;
   private final Map<Object, Object> dataset;

   private SessionImpl(Builder builder) {
      this.id = builder.id;
      this.cookies = builder.cookies;
      this.dataset = builder.dataset;
   }

   @Override
   public UUID id() {
      return this.id;
   }

   @Override
   public Cookies cookies() {
      return this.cookies;
   }

   @Override
   public Map<Object, Object> dataset() {
      return this.dataset;
   }

   static Builder builder() {
      return new Builder();
   }

   private static class Builder implements Session.Builder {
      private UUID id;
      private Cookies cookies;
      private Map<Object, Object> dataset = new HashMap<>();

      @Override
      public Session.Builder withId(UUID id) {
         this.id = id;
         return this;
      }

      @Override
      public Session.Builder withCookies(Cookies cookies) {
         this.cookies = cookies;
         return this;
      }

      @Override
      public Session build() {
         if (id == null)
            id = UUID.randomUUID();
         if(cookies == null)
            cookies = Cookies.create();
         cookies.set(new Cookie(SESSION_COOKIE_NAME, id.toString()));
         return new SessionImpl(this);
      }
   }
}

