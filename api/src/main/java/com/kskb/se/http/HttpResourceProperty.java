package com.kskb.se.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HttpResourceProperty {
   HttpResourceType type();
   HttpResourceLocation location();
}