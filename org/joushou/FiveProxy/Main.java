package org.joushou.FiveProxy;

import java.lang.String;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.io.FileNotFoundException;

import com.google.protobuf.CodedInputStream;

public class Main
{
  public static Caching mCaching = new Caching();
  
	static class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            System.err.println("Feeding username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(Settings.username, Settings.password.toCharArray()));
        }
    }

  public static void log(String log) {
    try {
      File logfile = new File(Settings.logFile);
      FileOutputStream fos = new FileOutputStream(logfile, true); // true: Append
      Date now = new Date();
      fos.write((now.toString() + ": " + log + "\n").getBytes());
       fos.close();
    } catch (Exception e) {e.printStackTrace();}
  }
	public static HttpURLConnection getData(String cmd) throws Exception {
		String requestUrl = Settings.remoteHost + cmd;
		StringBuffer str = new StringBuffer();
		Authenticator.setDefault(new MyAuthenticator());
		URL url = new URL(requestUrl.toString());
		HttpURLConnection in = null;
		in = (HttpURLConnection)url.openConnection();
		return in;
	}
	
	
	public static Map parseData (InputStream in)
	{
		Map titles = new HashMap();
   

		try {
			CodedInputStream stream = CodedInputStream.newInstance(in);
			int count = stream.readRawLittleEndian32();
			while (count-- > 0)
			{
				int size = stream.readRawLittleEndian32();
				byte[] recordData = stream.readRawBytes(size);
				Protos.Record record = Protos.Record.parseFrom(recordData);
				switch (record.getType())
				{
					case ARTIST:
						Protos.Artist mArtist = record.getArtist();
            titles.put(mArtist.getId(), mArtist);
						break;
				
					case ALBUM:
						Protos.Album mAlbum = record.getAlbum();
            titles.put(mAlbum.getId(), mAlbum);
						break;
				
					case SONG:
						Protos.Song mSong = record.getSong();
						titles.put(mSong.getId(), mSong);
						break;
				
					case PLAYLIST:
						Protos.Playlist mPlaylist = record.getPlaylist();
						titles.put(mPlaylist.getId(), mPlaylist);
						break;
				
					case PLAYLIST_SONG:
						Protos.PlaylistSong mPlaylistSong = record.getPlaylistSong();
						titles.put(mPlaylistSong.getId(), mPlaylistSong);
						break;
				}
			}
			in.close();
		} catch (Exception e) {e.printStackTrace();}
		return titles;
	}
	public static StringBuffer table = new StringBuffer();
	public static void main (String args[])
	{
		try {
			Map songMap = parseData(getData("/feeds/songs").getInputStream());
      Map artistMap = parseData(getData("/feeds/artists").getInputStream());
      Map albumMap = parseData(getData("/feeds/albums").getInputStream());
			Map playlistMap = parseData(getData("/feeds/playlists").getInputStream());
			Map playlistSongMap = parseData(getData("/feeds/playlistSongs").getInputStream());

			Collection songs = songMap.values();
      Collection artists = artistMap.values();
      Collection albums = albumMap.values();
			Collection playlists = playlistMap.values();
			Collection playlistSongs = playlistSongMap.values();

			Object[] song =  songs.toArray();

			int i = 0;
			table.append("<table border><tr><th>Title</th></tr>");
			for (i = 0; i < song.length; i++) {
				Protos.Song s = (Protos.Song) song[i];
				table.append("<tr><td><a href='/songs/"+s.getId()+"' target='_blank'>"+s.getTitle()+"</a></td></tr>");
			}
			table.append("</table>");
			
			MusicDB.createTables();
			MusicDB.insertSongs(songs);
      MusicDB.insertArtists(artists);
      MusicDB.insertAlbums(albums);
		} catch (Exception e) {
			e.printStackTrace();
		}
      mCaching.start();
  		webServer.startServer();
	}
}
