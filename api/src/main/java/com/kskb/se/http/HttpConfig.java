package com.kskb.se.http;

import java.util.Hashtable;
import java.util.function.Supplier;

public interface HttpConfig {
   void setBoolean(String key, Boolean value);
   Supplier<Boolean> getBoolean(String key);
   Supplier<Boolean> getBoolean(String key, Boolean def);

   void setString(String key, String value);
   Supplier<String> getString(String key);
   Supplier<String> getString(String key, String def);

   static HttpConfig create() {
      return new HttpConfigImpl();
   }
}

class HttpConfigValue<T> implements Supplier<T> {
   private T value;

   HttpConfigValue(final T value) {
      this.value = value;
   }

   public synchronized void set(T value) {
      this.value = value;
   }

   @Override
   public synchronized T get() {
      return value;
   }
}

class HttpConfigImpl implements  HttpConfig {
   private final Hashtable<String, HttpConfigValue<?>> map = new Hashtable<>();

   @Override
   public void setBoolean(String key, Boolean value) { set(key, value); }

   @Override
   public void setString(String key, String value) { set(key, value); }

   @Override
   public Supplier<Boolean> getBoolean(String key) {
      return get(key, Boolean.class);
   }

   @Override
   public Supplier<Boolean> getBoolean(String key, Boolean def) {
      setIfMissing(key, def);
      return get(key, Boolean.class);
   }

   @Override
   public Supplier<String> getString(String key) {
      return get(key, String.class);
   }

   @Override
   public Supplier<String> getString(String key, String def) {
      setIfMissing(key, def);
      return get(key, String.class);
   }

   @SuppressWarnings("unchecked")
   private <T> Supplier<T> get(String key, Class<T> type) {
      final var entryValue = map.get(key);
      if (entryValue == null)
         throw new NullPointerException();

      final var value = entryValue.get();
      if ( ! type.isInstance(value) )  {
         throw new IllegalStateException();
      }

      return (Supplier<T>) entryValue;
   }

   @SuppressWarnings("unchecked")
   private <T> void set(String key, T value) {
      final HttpConfigValue<T> entryValue = (HttpConfigValue<T>) map.get(key);
      if (entryValue != null) {
         entryValue.set(value);
      }
      else {
         map.put(key, new HttpConfigValue<>(value));
      }
   }

   private <T> void setIfMissing(String key, T value) {
      if ( ! map.containsKey(key) ) {
         map.put(key, new HttpConfigValue<>(value));
      }
   }
}
