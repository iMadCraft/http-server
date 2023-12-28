package com.kskb.se.base;

public class Strings {
   public static String join(String separator, String[] ary) {
      assert separator != null;
      assert ary != null;
      StringBuilder builder  = new StringBuilder();
      for (int i = 0; i < ary.length; i++) {
         builder.append(ary[i]);
         if (i + 1 < ary.length) {
            builder.append(separator);
         }
      }
      return builder.toString();
   }

   public static String[] substring(String[] ary, int from) {
      assert ary != null;
      assert from <= ary.length;
      final int length = ary.length - from;
      final String[] sub = new String[length];
      int cur = 0;
      for(int i = from; i < ary.length; i++) {
         sub[cur] = ary[i];
         cur++;
      }
      return sub;
   }
}
