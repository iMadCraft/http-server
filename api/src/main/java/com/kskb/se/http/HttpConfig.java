package com.kskb.se.http;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public interface HttpConfig {
   void setBoolean(String key, Boolean value);
   Supplier<Boolean> getBoolean(String key);
   Supplier<Boolean> getBoolean(String key, Boolean def);

   void setString(String key, String value);
   Supplier<String> getString(String key);
   Supplier<String> getString(String key, String def);

   <K extends String, V extends Object> void setAll(Map<K, V> pairs);
   
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

   @Override
   public String toString() {
      return value.toString();
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
      final var value = get(key, String.class);
      return value;
   }

   @Override
   public Supplier<String> getString(String key, String def) {
      setIfMissing(key, def);
      return get(key, String.class);
   }

   @Override
   public <K extends String, V extends Object> void setAll(Map<K, V> pairs) {
      for (final var pair: pairs.entrySet()) {
         final var key = pair.getKey().toString();
         final var value = pair.getValue();
         set(key, value);
      }
   }

   @SuppressWarnings("unchecked")
   private <T> Supplier<T> get(String key, Class<T> type) {
      final HttpConfigValue<?> entryValue = map.get(key);
      if (entryValue == null) {
         if (String.class.equals(type)) {
            return set(key, (T) System.getProperty(key));
         }
         else {
            return set(key, null);
         }
      }

      final var value = entryValue.get();
      if ( value != null && ! type.isInstance(value ) ) {
         throw new IllegalStateException();
      }

      return (Supplier<T>) entryValue;
   }

   @SuppressWarnings("unchecked")
   private <T> HttpConfigValue<T> set(String key, T value) {
      HttpConfigValue<T> entryValue = (HttpConfigValue<T>) map.get(key);
      if (entryValue != null) {
         entryValue.set(value);
      }
      else {
         entryValue = new HttpConfigValue<>(value);
         map.put(key, entryValue);
      }
      return entryValue;
   }

   private <T> void setIfMissing(String key, T value) {
      if ( ! map.containsKey(key) ) {
         map.put(key, new HttpConfigValue<>(value));
      }
   }
}
