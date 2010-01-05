package org.joushou.FiveProxy;


import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.Arrays;
import java.lang.Long;
import java.io.IOException;

public class webServer {
	
	static Vector threads = new Vector();
	static int maxThreads = 10;
  static int workingThreads = 0;

	public static void startServer() {
		for (int i = 0; i < maxThreads; i++) {
			Worker w = new Worker();
			w.start();
			threads.addElement(w);
		}
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(Settings.listenPort);
		} catch (IOException e) {
			System.out.println("Couldn't listen on port "+ Settings.listenPort);
			e.printStackTrace();
			System.exit(-1);
		}
	  Caching.init();
		System.out.println("Waiting for requests...");
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				
				Worker w = null;
				synchronized(threads) {
					if(threads.isEmpty()) {
						Worker ws = new Worker();
						ws.socketOpenTime = System.currentTimeMillis();
						ws.setSocket(clientSocket);
						ws.start();
					} else {
						w = (Worker) threads.elementAt(0);
						threads.removeElementAt(0);
				    w.socketOpenTime = System.currentTimeMillis();
						w.setSocket(clientSocket);
					}
				}
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(-1);
			}
		}
	}
}