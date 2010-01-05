package org.joushou.FiveProxy;

class Caching {
  private static boolean[] caching = new boolean[MusicDB.entries];
    public static void init() {
      int i;
      for(i = 0; i < caching.length; i++)
        caching[i] = false;
    }

      public static synchronized boolean isCaching(int id) {
        return caching[id];
      }

      public static synchronized boolean cache(int id) {
        if (caching[id] == false) {
          caching[id] = true;
          return true;
        }
        return false;
      }

      public static synchronized void doneCaching(int id) {
        caching[id] = false;
      }
}