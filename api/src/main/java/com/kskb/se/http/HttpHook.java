package com.kskb.se.http;

@FunctionalInterface
public interface HttpHook<C extends HttpHookContext> {
   void run(C context);
}
