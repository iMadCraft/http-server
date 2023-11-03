package com.kskb.se.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static com.kskb.se.http.Session.SESSION_COOKIE_NAME;

public class SessionManagerImpl implements SessionManager {
   private final long maxAgeInSeconds = 3600;
   private final List<Session> sessions = new ArrayList<>();

   @Override
   public Session find(final HttpRequest request) {
      return this.find(request, null);
   }

   @Override
   public Session find(final HttpRequest request,
                       final Consumer<Session.Builder> newSession)
   {
      assert request != null;
      sessions.removeIf(Session::hasExpired);
      for (final var cookie: request.cookies()) {
         if (Objects.equals(cookie.name, SESSION_COOKIE_NAME)) {
            for (final var session: sessions) {
               final long accessTime = System.currentTimeMillis() / 1000;
               if (Objects.equals(cookie.value, session.id().toString())) {
                  session.setAccessTime(accessTime);
                  return session;
               }
            }
         }
      }
      Session.Builder builder = createSessionBuilder();
      if (newSession != null)
         newSession.accept(builder);
      Session session = builder.build();
      sessions.add(session);
      return session;
   }

   @Override
   public void modified(final Session session,
                        final HttpRequest request,
                        final HttpResponse.Builder responseBuilder)
   {
      session.cookies()
         .set(new Cookie(SESSION_COOKIE_NAME, session.id().toString(), null, "/", null, null, null, null));
   }

   protected Session.Builder createSessionBuilder() {
      final long creationTime = System.currentTimeMillis() / 1000;
      return Session.builder()
         .withId(UUID.randomUUID())
         .withMaxAge(maxAgeInSeconds)
         .withTime(creationTime);
   }
}
