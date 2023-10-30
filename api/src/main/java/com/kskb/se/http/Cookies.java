package com.kskb.se.http;

import java.util.*;

public interface Cookies extends Iterable<Cookie>, Cloneable {
   Optional<Cookie> get(String name);
   boolean set(Cookie cookie);
   boolean setAll(Iterable<Cookie> cookie);
   boolean delete(String name);
   default boolean delete(Cookie cookie) {
      return delete(cookie.name);
   }

   Cookies clone();

   static Cookies create() {
      return new CookiesImpl();
   }
}

class CookiesImpl implements Cookies {
   final List<Cookie> cookies = new ArrayList<>();

   @Override
   public Optional<Cookie> get(String name) {
      return cookies.stream()
         .filter(c -> Objects.equals(c.name, name))
         .findFirst();
   }

   @Override
   public boolean set(Cookie cookie) {
      delete(cookie.name);
      return cookies.add(cookie);
   }

   @Override
   public boolean setAll(final Iterable<Cookie> cookies) {
      for (final var cookie: cookies)
         set(cookie);
      return true;
   }

   @Override
   public boolean delete(String name) {
      return cookies.removeIf(c -> Objects.equals(c.name, name));
   }

   @Override
   public Iterator<Cookie> iterator() {
      return cookies.iterator();
   }

   @Override
   public Cookies clone() {
      CookiesImpl clone = new CookiesImpl();
      cookies.forEach(c -> clone.cookies.add(c.clone()));
      return clone;
   }
}