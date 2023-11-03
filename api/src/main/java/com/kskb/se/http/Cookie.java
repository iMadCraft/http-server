package com.kskb.se.http;

import com.kskb.se.base.NotNull;
import com.kskb.se.base.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Cookie {
   public final @NotNull String name;
   public final @NotNull String value;
   public @Nullable String domain;
   public @Nullable String path;
   public @Nullable String expires;
   public @Nullable String sameSite;
   public @Nullable Boolean httpOnly;
   public @Nullable Boolean secure;

   public Cookie(@NotNull final String name,
                 @NotNull final String value)
   {
      this(name, value, null, null, null, null, null, null);
   }

   public Cookie(@NotNull final String name,
                 @NotNull final String value,
                 @Nullable final String domain,
                 @Nullable final String path,
                 @Nullable final String expires,
                 @Nullable final String sameSite,
                 @Nullable final Boolean httpOnly,
                 @Nullable final Boolean secure)
   {
      assert name != null;
      assert value != null;
      this.name = name;
      this.value = value;
      this.domain = domain;
      this.path = path;
      this.expires = expires;
      this.sameSite = sameSite;
      this.httpOnly = httpOnly;
      this.secure = secure;
   }

   public Cookie clone() {
      return new Cookie(name, value);
   }

   public static List<Cookie> from(@NotNull String value) {
      assert value != null;
      final List<Cookie> result = new ArrayList<>();
      final var cookies = value.split(";");
      for (final var cookie: cookies) {
         final var pair = cookie.split("=");
         result.add(new Cookie(pair[0], pair[1]));
      }
      return result;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(name + "=" + value);
      if (domain != null) {
         builder.append("; Domain=");
         builder.append(domain);
      }
      if (path != null) {
         builder.append("; Path=");
         builder.append(path);
      }
      if (expires != null) {
         builder.append("; Expires=");
         builder.append(expires);
      }
      if (httpOnly != null && httpOnly) {
         builder.append("; HttpOnly");
      }
      if (secure != null && secure) {
         builder.append("; Secure");
      }
      if (sameSite != null) {
         builder.append("; SameSite");
      }
      return builder.toString();
   }
}
