package com.kskb.se.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface HttpHooks extends Iterable<HttpHook>, Cloneable {
   <C extends HttpHookContext> void add(Class<C> clazz, HttpHook<C> hook);
   <C extends HttpHookContext> Iterable<HttpHook<C>> byType(Class<C> clazz);
   HttpHooks clone();
   static HttpHooks create() {
      return new HttpHooksImpl();
   }
}

record HttpHookEntry(Class<? extends HttpHookContext> type, HttpHook hook) {}
class HttpHooksImpl implements HttpHooks {
   private final List<HttpHookEntry> hooks;

   HttpHooksImpl() {
      this(new ArrayList<>());
   }

   private HttpHooksImpl(List<HttpHookEntry> hooks) {
      this.hooks = hooks;
   }

   @Override
   public HttpHooks clone() {
      return new HttpHooksImpl(new ArrayList<>(hooks));
   }

   @Override
   public <C extends HttpHookContext> void add(Class<C> clazz, HttpHook<C> hook) {
      hooks.add(new HttpHookEntry(clazz, hook));
   }

   @Override
   public <C extends HttpHookContext> Iterable<HttpHook<C>> byType(Class<C> type) {
      final var it = hooks.iterator();
      return () -> new Iterator<>() {
         HttpHook<?> next;

         @Override
         public boolean hasNext() {
            return find() != null;
         }

         private HttpHook<?> find() {
            if (next != null)
               return next;

            HttpHookEntry entry;
            while (it.hasNext()) {
               entry = it.next();
               if (type.equals(entry.type())) {
                  next = entry.hook();
                  break;
               }
            }
            return next;
         }

         @Override
         public HttpHook<C> next() {
            if (next == null)
               next = find();

            final var rt = next;
            next = null;
            return (HttpHook<C>) rt;
         }
      };
   }

   @Override
   public Iterator<HttpHook> iterator() {
      return hooks.stream()
         .map(HttpHookEntry::hook)
         .toList()
         .iterator();
   }
}
