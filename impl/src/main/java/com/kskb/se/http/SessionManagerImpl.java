package com.kskb.se.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static com.kskb.se.http.Session.SESSION_COOKIE_NAME;

class SessionManagerImpl implements SessionManager {
   List<Session> sessions = new ArrayList<>();

   @Override
   public Session find(final HttpRequest request) {
      return this.find(request, null);
   }

   @Override
   public Session find(final HttpRequest request,
                       final Consumer<Session.Builder> newSession)
   {
      assert request != null;
      for (final var cookie: request.cookies()) {
         if (Objects.equals(cookie.name, SESSION_COOKIE_NAME)) {
            for (final var session: sessions) {
               if (Objects.equals(cookie.value, session.id().toString())) {
                  return session;
               }
            }
         }
      }
      Session.Builder builder = Session.builder()
         .withId(UUID.randomUUID());
      if (newSession != null)
         newSession.accept(builder);
      Session session = builder.build();
      sessions.add(session);
      return session;
   }
}
