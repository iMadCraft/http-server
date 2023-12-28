package com.kskb.se.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface Session {
   String SESSION_COOKIE_NAME = "session";

   UUID id();
   long maxAge();
   long accessTime();
   long creationTime();
   Cookies cookies();
   Map<Object, Object> dataset();

   void setAccessTime(long accessTime);

   default Object get(Object key) { return dataset().get(key); }
   default Object get(Object key, Object def) {
      final var stored = dataset().get(key);
      final var rt = stored != null ?
         stored : def;
      return rt;
   }

   default Object put(Object key, Object value) { return dataset().put(key, value); }
   default Object remove(Object key) { return dataset().remove(key); }

   default boolean hasExpired() {
      return ( accessTime() - creationTime() ) >= maxAge();
   }
   default boolean hasNotExpired() {
      return ! hasExpired();
   }

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
      Builder withMaxAge(long maxAgeInSeconds);
      Builder withTime(long timeInSeconds);
      Builder withCreationTime(long creationTimeInSeconds);
      Builder withAccessTime(long accessTimeInSeconds);
      Session build();

   }
}

class SessionImpl implements Session {
   private final UUID id;
   private final Cookies cookies;
   private final Map<Object, Object> dataset;
   private final long maxAgeInSeconds;
   private final long creationTimeInSeconds;
   private long accessTimeInSeconds;

   private SessionImpl(Builder builder) {
      this.id = builder.id;
      this.maxAgeInSeconds = builder.maxAgeInSeconds;
      this.accessTimeInSeconds = builder.accessTimeInSeconds;
      this.creationTimeInSeconds = builder.creationTimeInSeconds;
      this.cookies = builder.cookies;
      this.dataset = builder.dataset;
   }

   @Override
   public UUID id() {
      return this.id;
   }

   @Override
   public long maxAge() {
      return this.maxAgeInSeconds;
   }

   @Override
   public long accessTime() {
      return this.accessTimeInSeconds;
   }

   @Override
   public long creationTime() {
      return this.creationTimeInSeconds;
   }

   @Override
   public Cookies cookies() {
      return this.cookies;
   }

   @Override
   public void setAccessTime(long accessTime) {
      this.accessTimeInSeconds = accessTime;
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
      private long maxAgeInSeconds;
      private long accessTimeInSeconds;
      private long creationTimeInSeconds;

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
      public Session.Builder withMaxAge(long maxAge) {
         this.maxAgeInSeconds = maxAge;
         return this;
      }

      @Override
      public Session.Builder withTime(long time) {
         this.accessTimeInSeconds = time;
         this.creationTimeInSeconds = time;
         return this;
      }

      @Override
      public Session.Builder withCreationTime(long creationTime) {
         this.creationTimeInSeconds = creationTime;
         return this;
      }

      @Override
      public Session.Builder withAccessTime(long accessTime) {
         this.accessTimeInSeconds = accessTime;
         return this;
      }

      @Override
      public Session build() {
         if (id == null)
            id = UUID.randomUUID();
         if(cookies == null)
            cookies = Cookies.create();
         return new SessionImpl(this);
      }
   }
}

