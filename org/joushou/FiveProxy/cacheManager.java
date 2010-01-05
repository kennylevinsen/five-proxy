package org.joushou.FiveProxy;

import java.io.File;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class cacheManager extends Thread {

  public synchronized void clean() {
    notify();
  }

  public synchronized void run() {
    while (true) {
        try {
          Main.log("CacheManager: " + getPercentageUsed() + "% used ["+getCacheSize() / 1048576+"MiB/"+Settings.maxCache/1048576+"MiB]");
          if (getCacheSize() > Settings.maxCache) {
            Main.log("CacheManager: Initiating cleanup-routine...");
            Class.forName("SQLite.JDBCDriver");
            Connection con = DriverManager.getConnection("jdbc:sqlite:/music.db","","");
            Statement st = con.createStatement();
            String sql = "select songId, count(*) TotalCount from playLog group by songId having (strftime('%s','now') - max(time)) > "+Settings.bufferTime+" "+(Settings.preservedPlaycount != -1 ? "and count(*) < " + Settings.preservedPlaycount : "")+" order by TotalCount asc, max(time) asc";
            ResultSet rs = st.executeQuery(sql);
            while(rs.next() && getCacheSize() > Settings.maxCache) {
              int id = rs.getInt("songId");
              if(!Caching.isCaching(id)) {
                Main.log("CacheManager: Deleting '" + MusicDB.getTitleFromId(id) + "' from cache ("+rs.getInt("TotalCount")+" playbacks since caching)");
                File f = new File(Settings.cacheFolder + id);
                f.delete();
                con.createStatement().executeUpdate("delete from playLog where songId=="+id);
              }
            }
            con.close();
            if(getCacheSize() > Settings.maxCache) {
              Main.log("CacheManager: Still too big after cleaning up; Consider increasing the cache size"); 
            } else {
              Main.log("CacheManager: After cleanup: " +getPercentageUsed() + "% used");
            }
          }
        wait();
        } catch (java.sql.SQLException e) {
          e.printStackTrace();
        } catch(Exception e) {e.printStackTrace();}
      }
  }
  private static long getCacheSize() {
    long folderSize = 0;
    File[] filelist = new File(Settings.cacheFolder).listFiles();
    int i;
    for(i=0;i<filelist.length;i++) {
      folderSize += filelist[i].length();
    }
    return folderSize;
  }
  public static Float getPercentageUsed() {
    Float percentage = ((float)getCacheSize() / (float)Settings.maxCache) *  (float)100.0;
    return percentage;
  }
}