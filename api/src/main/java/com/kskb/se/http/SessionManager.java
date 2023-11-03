package com.kskb.se.http;

import com.kskb.se.base.NotNull;
import com.kskb.se.base.Nullable;

import java.util.function.Consumer;

public interface SessionManager {
   Session find(@NotNull HttpRequest request);
   Session find(@NotNull HttpRequest request, @Nullable Consumer<Session.Builder> newSession);
   void modified(@NotNull Session session, @NotNull HttpRequest request, @NotNull  HttpResponse.Builder responseBuilder);
}