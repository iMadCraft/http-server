package com.kskb.se.http;

import java.util.UUID;

public class SessionManagerMock extends SessionManagerImpl {
   public static final long MOST_SIG_BITS = 0xFFFFFFFFFFFFFFFFL;
   public static final long LEAST_SIG_BITS = 0xFFFFFFFFFFFFFF00L;
   public static final UUID FIRST_SESSION_ID = new UUID(MOST_SIG_BITS, LEAST_SIG_BITS);

   public long leastSigBits = LEAST_SIG_BITS;

   private long creationTime = System.currentTimeMillis() / 1000;
   private long accessTime = creationTime;

   void setCreationTime(long timeInSeconds) {
      this.creationTime = timeInSeconds;
   }

   @Override
   protected Session.Builder createSessionBuilder() {
      final var builder = Session.builder()
         .withId(new UUID(MOST_SIG_BITS, leastSigBits))
         .withMaxAge(3600)
         .withCreationTime(creationTime)
         .withAccessTime(accessTime);
      leastSigBits ++;
      return builder;
  }
}
