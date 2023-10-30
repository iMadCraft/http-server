package com.kskb.se.http;

import java.util.UUID;

public class SessionManagerMock extends SessionManagerImpl {
   public static final UUID SESSION_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

   @Override
   protected Session.Builder createSessionBuilder() {
      return Session.builder()
         .withId(SESSION_ID);
  }
}
