package com.kskb.se.http;

abstract class AbstractBinaryResource implements HttpBinaryResource {
   private final byte[] bytes;

   protected AbstractBinaryResource(byte[] bytes) {
      this.bytes = bytes;
   }

   @Override
   public long size() {
      return this.bytes.length;
   }

   @Override
   public HttpResourceType type() {
      return HttpResourceType.DATA;
   }

   @Override
   public String contentType() {
      return "application/octet-stream";
   }

   @Override
   public byte[] bytes() {
      return this.bytes;
   }
}