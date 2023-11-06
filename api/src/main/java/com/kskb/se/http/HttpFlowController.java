package com.kskb.se.http;

public interface HttpFlowController {

   boolean isInterrupted();

   boolean shouldInitialize();
   boolean shouldParse();
   boolean shouldValidate();
   boolean shouldRewrite();
   boolean shouldExecuteEndpoints();
   Boolean shouldLoadResource();
   boolean shouldPostProcessing();

   void stop();
   void stillLoadResource();
   void stopLoadResource();

   static HttpFlowController create() {
      return new HttpFlowControllerImpl();
   }

   void setLoadResource(boolean shouldLoadResourceByDefault);
}

class HttpFlowControllerImpl implements HttpFlowController {
   private Boolean loadResource = null;
   private boolean interrupted = false;
   private boolean initialize = true;
   private boolean parse = true;
   private boolean validate = true;
   private boolean rewrite = true;
   private boolean endpoints = true;
   private boolean post = true;

   @Override
   public void stop() {
      interrupted = true;
      initialize = false;
      parse = false;
      validate = false;
      rewrite = false;
      endpoints = false;
      post = false;
   }

   @Override
   public void stillLoadResource() {
      loadResource = true;
   }

   @Override
   public void stopLoadResource() {
      loadResource = false;
   }

   @Override
   public boolean isInterrupted() {
      return this.interrupted;
   }

   @Override
   public boolean shouldInitialize() {
      return initialize;
   }

   @Override
   public boolean shouldParse() {
      return parse;
   }

   @Override
   public boolean shouldValidate() {
      return validate;
   }

   @Override
   public boolean shouldRewrite() {
      return rewrite;
   }

   @Override
   public boolean shouldExecuteEndpoints() {
      return endpoints;
   }

   @Override
   public Boolean shouldLoadResource() {
      return this.loadResource;
   }

   @Override
   public boolean shouldPostProcessing() {
      return post;
   }

   @Override
   public void setLoadResource(boolean loadResource) {
      this.loadResource = loadResource;
   }
}