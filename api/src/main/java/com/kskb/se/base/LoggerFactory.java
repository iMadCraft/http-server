package com.kskb.se.base;

import java.util.logging.*;

public interface LoggerFactory {
   static void init() {
      if ( ! InternalLogHandler.INIT ) {
         LogManager.getLogManager().reset();
         InternalLogHandler.INIT = true;
      }
   }

   static Logger getLogger(Class<?> clazz) {
      LoggerFactory.init();
      final var log = Logger.getLogger(clazz.getName());
      log.addHandler(InternalLogHandler.HANDLER);
      return log;
   }
}

class InternalLogHandler extends Handler {
   public static boolean INIT = false;
   static final InternalLogHandler HANDLER = new InternalLogHandler();

   @Override
   public void publish(LogRecord record) {
      System.out.printf("%.4096s\n", record.getMessage());
   }

   @Override
   public void flush() {
      System.out.flush();
   }

   @Override
   public void close() throws SecurityException {}
}
