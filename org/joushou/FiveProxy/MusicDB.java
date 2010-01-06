package org.joushou.FiveProxy;
 
import java.util.Collection;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
 
 
import com.google.protobuf.CodedInputStream;
import java.sql.SQLException;
 
public class MusicDB {
  public static Connection conn;
  public static int entries = 0;
  public static Connection getConnection() {
    try {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:music.db");     
    } catch (Exception e) {
      e.printStackTrace();
    }
    return conn;
  }
  public static void createTables() {
    Connection con = getConnection();
    try {
      try {
        Statement statement = con.createStatement();
        statement.execute("create table if not exists artists(id INTEGER PRIMARY KEY, syncTime INTEGER, mbid STRING, name STRING, discoveryDate INTEGER)");
        statement.execute("create table if not exists albums(id INTEGER PRIMARY KEY, syncTime INTEGER, artistId INTEGER, mbid STRING, name STRING, discoveryDate INTEGER, releaseDate INTEGER)");
        statement.execute("create table if not exists songs(id INTEGER PRIMARY KEY, syncTime INTEGER, artistId INTEGER, albumId INTEGER, mbid STRING, mimeType STRING, bitrate INTEGER, filesize INTEGER, length INTEGER, title STRING, track INTEGER)");
        statement.execute("create table if not exists playLog(id INTEGER PRIMARY KEY AUTOINCREMENT, songId INTEGER,  time DATE, ip STRING)");
      } finally {
        con.close();
      }
    } catch(java.sql.SQLException e) { e.printStackTrace(); }
  }
  public static void insertSongs(Collection songs) {
    Connection con = getConnection();
    Object[] song = songs.toArray();
    entries = song.length;
    int i = 0;
    try {
      String str = "insert into songs values(?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = con.prepareStatement(str);
      Statement st = con.createStatement();
      try{
        st.execute("delete from songs");
        for (i = 0; i < song.length; i++) {
          Protos.Song s = (Protos.Song) song[i];
          ps.setLong(1,s.getId());
          ps.setLong(2,s.getSyncTime());
          ps.setLong(3,s.getArtistId());
          ps.setLong(4,s.getAlbumId());
          ps.setString(5,s.getMbid());
          ps.setString(6,s.getMimeType());
          ps.setInt(7,s.getBitrate());
          ps.setLong(8,s.getFilesize());
          ps.setInt(9,s.getLength());
          ps.setString(10,s.getTitle());
          ps.setInt(11,s.getTrack());
 
          ps.executeUpdate();
 
        }
      } finally {
        ps.close();
        con.close();
      }
      con.close();
    } catch (java.sql.SQLException e) {
      e.printStackTrace();
    }
  }
  public static void insertArtists(Collection artists) {
    Connection con = getConnection();
    Object[] artist = artists.toArray();
    int i = 0;
    try {
      String str = "insert into artists values(?,?,?,?,?)";
      PreparedStatement ps = con.prepareStatement(str);
      Statement st = con.createStatement();
      try {
        st.execute("delete from artists");
        for (i = 0; i < artist.length; i++) {
          Protos.Artist a = (Protos.Artist) artist[i];
          ps.setLong(1,a.getId());
          ps.setLong(2,a.getSyncTime());
          ps.setString(3,a.getMbid());
          ps.setString(4,a.getName());
          ps.setLong(5,a.getDiscoveryDate());
          ps.executeUpdate();
        }
      } finally {
        ps.close();
        con.close();
      }
    } catch (java.sql.SQLException e) {
      e.printStackTrace();
    }
  }
  public static void insertAlbums(Collection albums) {
    Connection con = getConnection();
    Object[] album = albums.toArray();
    int i = 0;
    try {
      String str = "insert into albums values(?,?,?,?,?,?,?)";
      PreparedStatement ps = con.prepareStatement(str);
      Statement st = con.createStatement();
      try {
        st.execute("delete from albums");
        for (i = 0; i < album.length; i++) {
          Protos.Album a = (Protos.Album) album[i];
 
          ps.setLong(1,a.getId());
          ps.setLong(2,a.getSyncTime());
          ps.setLong(3, a.getArtistId());
          ps.setString(4,a.getMbid());
          ps.setString(5,a.getName());
          ps.setLong(6,a.getDiscoveryDate());
          ps.setLong(7, a.getReleaseDate());
 
          ps.executeUpdate();
        }
      } finally {
        con.close();
      }    
    } catch (java.sql.SQLException e) {
      e.printStackTrace();
    }
  }
  public static void logPlay(int songId, String ip) {
    try {
      Connection con = getConnection();
      String str = "insert into playlog values(null, "+Integer.toString(songId)+",strftime('%s','now'),?)";
      PreparedStatement ps = con.prepareStatement(str);      
      try {
        ps.setString(1,ip);
        ps.executeUpdate();
      } finally {  
        ps.close();
        con.close();
      }
    } catch(java.sql.SQLException e) {e.printStackTrace();}
  }
  public static String getTitleFromId(int id){
    Connection con = getConnection();
    try {
      Statement st = con.createStatement();  
      String songName;
      ResultSet rs = st.executeQuery("select title as songName from songs where `id` == "+id+" LIMIT 1");
      try {
        rs.next();
        songName = rs.getString("songName");
      } finally {
        rs.close();
        con.close();
      }
      return songName;
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
    }
    return "No title found!";
  }
  public static long getSizeFromId(int id){
    Connection con = getConnection();
    try {
      Statement st = con.createStatement();
      ResultSet rs;
      long fileSize;
      rs = st.executeQuery("select fileSize from songs where `id` == "+id+" LIMIT 1");  
      try {
        rs.next(); 
        fileSize = rs.getLong("fileSize");
      } finally {
        rs.close();
        con.close();
      }
      return fileSize;
    } catch (java.sql.SQLException e) {
        e.printStackTrace();
    }
    return 0;
  }
 
  public static String getPlaylistFromArtist(String search) {
    Connection con = getConnection();
    StringBuffer playlist = new StringBuffer();
    try {
      playlist.append("[playlist]\n");
      try {
        String str = "select artists.name, songs.title, songs.length, songs.id from artists join songs where artists.id == songs.artistId AND (artists.name LIKE '%"+search+"%' OR songs.title LIKE '%"+search+"%')";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(str);
        try {
          int i = 0;
          while(rs.next()){
            i++;
            String num = Integer.toString(rs.getRow());
            playlist.append("File" + num + "=http://"+Settings.username+":"+Settings.password+"@joushou.org"+":"+Settings.listenPort+"/songs/" + rs.getInt(4) + "\n" );
            playlist.append("Title" + num + "=" + rs.getString(1) + " - " + rs.getString(2) + "\n");
            playlist.append("Length" + num + "=" + rs.getLong(3) + "\n");
          }
          playlist.append("NumberOfEntries=" + Integer.toString(i) + "\n");
          playlist.append("Version=2\n");
        } finally {
          rs.close();
        }
      } finally {
        con.close();
      }
      return playlist.toString();
    } catch (java.sql.SQLException e) {
      e.printStackTrace();
    }
    return "False";
  }
  public static Object[] getCleanupIds() {
    try {
      Connection con = getConnection();
      Statement st  = con.createStatement();
      String sql = "select songId, count(*) TotalCount from playLog group by songId having (strftime('%s','now') - max(time)) > "+Settings.bufferTime+" "+(Settings.preservedPlaycount != -1 ? "and count(*) < " + Settings.preservedPlaycount : "")+" order by TotalCount asc, max(time) asc";
      ArrayList songIds = new ArrayList();
      try {
        ResultSet rs = st.executeQuery(sql);
        int i = 0;
        while (rs.next()) {
          int id = rs.getInt("songId");
          songIds.add(id);
        }
        Object[] songId = songIds.toArray();
        return songId;
      } finally {
        con.close();
      }
    } catch (java.sql.SQLException e) {e.printStackTrace();}
    return new Integer[1];
  }
  public static void deleteLogById(int id) {
    try {
      Connection con = getConnection();
      String str = "delete from playLog where songId=="+id;
      Statement st = con.createStatement();      
      try {
        st.executeUpdate(str);
      } finally {  
        con.close();
      }
    } catch(java.sql.SQLException e) {e.printStackTrace();}
  }
}