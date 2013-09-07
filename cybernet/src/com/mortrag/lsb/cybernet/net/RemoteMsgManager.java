package com.mortrag.lsb.cybernet.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mortrag.lsb.cybernet.Level_GlobalChat;

public class RemoteMsgManager {
	// --------------------------------------------------------------------------------------------
	// GENERAL
	// --------------------------------------------------------------------------------------------

	// Settings
	private static final String TAG = "RemoteMsgManager";
	public static final int CYBERNET_SERVER_PORT = 25565; // Minecraft! :-)
	public static final String MAX_IP = "67.170.49.252";
	private static final int SOCKET_WAIT_TIME_MS = 100;

	// Commands
	public static final String CLIENT_SEND_CMD = "/cs";
	public static final String CLIENT_EOF_CMD = "/eof";

	public static final String SERVER_START_CMD = "/server";
	public static final String SERVER_SEND_CMD = "/ss";
	public static final String SERVER_EXIT_CMD = "/exit"; 
	// NOTE: Manually closing client not yet implemente.d

	public RemoteMsgManager() {
		clientReceived = new StringBuilder();
		serverReceived = new StringBuilder();
		serverToSend = new StringBuilder();
	}

	// --------------------------------------------------------------------------------------------
	// Client
	// --------------------------------------------------------------------------------------------

	private boolean shutdownClient = false;	
	private Socket clientSocket = null;
	private StringBuilder clientReceived = null;

	private class ClientListensToServer extends Thread {
		private final String TAG = RemoteMsgManager.TAG + ":" + "ClientListensToServer";

		@Override
		public void run() {
			if (clientSocket == null || !clientSocket.isConnected()) {
				Gdx.app.log(TAG, "Can't start " + TAG + " when not connected! Exiting.");
				return;
			}
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				Gdx.app.log(TAG, "Client ready to receive ACKs from Server.");
				boolean serverClosed = false;
				while(!getShutdownClient() && clientSocket != null &&
						clientSocket.isConnected() && !serverClosed) {
					String msg = reader.readLine();
					if (msg == null) {
						serverClosed = true;
					} else {
						Gdx.app.log(TAG, "Got msg from server: " + msg);
						clientReceived.append(msg + Level_GlobalChat.NL);
					}
				}
			} catch (IOException e) {
				Gdx.app.log(TAG, "Client got IOException reading from server.", e);
			} catch (Exception e) {
				Gdx.app.log(TAG, "Client got unexpected exception.", e);
			} finally {
				if (clientSocket != null) {
					clientSocket.dispose();
					clientSocket = null;
				}
				clientHasBeenShutDown();
			}
		}
	}

	public synchronized String getClientText() {
		if (clientReceived.length() > 0) {
			String ret = clientReceived.toString();
			clientReceived.delete(0, clientReceived.length());
			return ret;
		} else {
			return null;
		}
	}

	public void connectToServer(String ip) {
		if (clientSocket != null && clientSocket.isConnected()) {
			Gdx.app.log(TAG, "Client is already connected!");
		}
		// try to connect
		try {
			clientSocket = Gdx.net.newClientSocket(Net.Protocol.TCP,
					ip, CYBERNET_SERVER_PORT, null);
			Gdx.app.log(TAG, "Client connected to server.");
			// Start listening to ensure we get responses.
			new ClientListensToServer().start();			
		} catch (GdxRuntimeException e) {
			Gdx.app.log(TAG, "Client couldn't connect to server");
			if (clientSocket != null) {
				clientSocket.dispose();
				clientSocket = null;
			}
		}
	}

	public void sendMsgToServer(String ip, String raw_msg) {
		// If we're not connected, connect.
		if (clientSocket == null || !clientSocket.isConnected()) {
			connectToServer(ip);
		}

		// If we're still not connected, there's probably a problem.
		if (clientSocket == null || !clientSocket.isConnected()) {
			Gdx.app.log(TAG, "Couldn't send message as couldn't connect to server");
			return;
		}
		// Try to write a message.
		String msg = raw_msg + Level_GlobalChat.NL; // add newline so we can use readline(...)
		try {
			clientSocket.getOutputStream().write(msg.getBytes());
			Gdx.app.log(TAG, "Client sent msg to server: " + raw_msg);			
		} catch (GdxRuntimeException e){
			Gdx.app.log(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Gdx.app.log(TAG, e.getMessage(), e);
		}
	}	


	private synchronized boolean getShutdownClient() {
		return shutdownClient;
	}

	public synchronized void shutdownClient() {
		Gdx.app.log(TAG, "Shutting down client");
		shutdownClient = true;
	}		

	private synchronized void clientHasBeenShutDown() {
		Gdx.app.log(TAG, "Client has been shut down");		
		shutdownClient = false;
	}		

	// --------------------------------------------------------------------------------------------
	// Server
	// --------------------------------------------------------------------------------------------	

	private boolean serverRunning = false;
	private boolean shutdownServer = false;
	private ServerSocket serverSocket = null;	
	public StringBuilder serverToSend = null;
	public StringBuilder serverReceived = null;

	public String getServerToSend() {
		synchronized (serverToSend) {
			if (serverToSend.length() > 0) {
				String ret = serverToSend.toString();
				serverToSend.delete(0, serverToSend.length());
				return ret;
			} else {
				return null;
			}
		}
	}

	public synchronized String getServerText() {
		if (serverReceived.length() > 0) {
			String ret = serverReceived.toString();
			serverReceived.delete(0, serverReceived.length());
			return ret;
		} else {
			return null;
		}
	}	
	
	public void addToServerToSend(String s) {
		synchronized (serverToSend) {
			serverToSend.append(s);
		}
	}

	public void setupServer() {
		if (serverRunning) {
			Gdx.app.log(TAG, "Server already running--not starting.");
			return;
		}
		Gdx.app.log(TAG, "Setting up server...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				serverSocket = null;
				try {
					serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, CYBERNET_SERVER_PORT,
							null);
					while(!getShutdownServer()) {
						Socket serverChannel = null;
						Gdx.app.log(TAG, "Server waiting for incoming connections...");
						serverChannel = serverSocket.accept(null);
						Gdx.app.log(TAG, "Server got connection from client");
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(
									serverChannel.getInputStream()));
							boolean exit = false;							
							while (!exit && !getShutdownServer()) {

								// Check sending / send.
								String toSend = getServerToSend();
								if (toSend != null) {
									serverSend(serverChannel, toSend);
								}

								// Check receipt / receive.
								synchronized (serverChannel) {
									if (!reader.ready()) {
										// If not ready, wait so we can check shutdown again.								
										try {
											serverChannel.wait(SOCKET_WAIT_TIME_MS);
										} catch (InterruptedException e) {
											// Do nothing
										}
									} else {
										// Ready to read a msg; do it.
										String msg = reader.readLine();
										if (msg == null) {
											// if this happens, reader can be "ready" only to have
											// readLine() return null for EOF. Good to know.
											Gdx.app.log(TAG, "Server readline() got null. Exiting");
											exit = true;
										}
										if (msg.indexOf(CLIENT_EOF_CMD) > -1) {
											// Client told us to stop.
											Gdx.app.log(TAG,
													"Server got exit command from client. Exiting");									
											exit = true;
										} else {
											// "Normal" case: got msg!
											Gdx.app.log(TAG, "Server got msg: " + msg);
											serverReceived.append(msg + Level_GlobalChat.NL);
											//serverAck(serverChannel, msg);									
										}
									}
								}
							}
							// Connection ended
							// ----------------
						} catch (IOException e) {
							Gdx.app.log(TAG, "Problem with current connection.", e);
						} finally {
							if (serverChannel != null) {
								Gdx.app.log(TAG, "Disposing of sever channel (socket).");
								serverChannel.dispose();
							}							
						}
					}

					// Server shutdown
					// ---------------
				} catch (GdxRuntimeException e) {
					Gdx.app.log(TAG,
							"GdxRuntimeExceptoin means likely problem with seversocket",
							e);
				} catch (NullPointerException e) {
					Gdx.app.log(TAG,
							"ServerSocket probably not created correctly; NullPointerException",
							e);
				} finally {
					if (serverSocket != null) {			
						Gdx.app.log(TAG, "Disposing of server acceptor socket entirely.");
						serverSocket.dispose();
					}
				}			
				// Server has shut down--turn the flag off so that future servers can be shut down.
				serverHasBeenShutDown();
			}
		}).start();
		serverRunning = true;
	}

	public void serverSend(Socket serverChannel, String msg) {
		if (serverChannel == null || !serverChannel.isConnected()) {
			Gdx.app.log(TAG, "Server couldn't send msg ("+ msg + "); not connected.");
			return;
		} 
		try {
			String msg_w_newline = msg + Level_GlobalChat.NL;
			serverChannel.getOutputStream().write(msg_w_newline.getBytes());
			Gdx.app.log(TAG, "Server sent msg (" + msg + ").");			
		} catch (IOException e) {
			Gdx.app.log(TAG, "Server encountered problem sending msg (" + msg + ").", e);
		}
	}

	public void serverAck(Socket serverChannel, String receivedMsg) {
		serverSend(serverChannel, "ACK: " + receivedMsg);
	}	

	// TODO look up synchronized methods to confirm or deny suspicions.
	// TODO make these Booleans and lock on them, not synrhconized methods.
	private synchronized boolean getShutdownServer() {
		return shutdownServer;
	}

	public synchronized void shutdownServer() {
		Gdx.app.log(TAG, "Shutting down server");
		shutdownServer = true;
	}	

	private synchronized void serverHasBeenShutDown() {
		Gdx.app.log(TAG, "Server has been shut down");	
		shutdownServer = false;
		serverRunning = false;
	}

	// --------------------------------------------------------------------------------------------
	// Cooper thread :-)
	// --------------------------------------------------------------------------------------------	
	public void getMsgsFromMax() {
		new GetMsgsFromMax().start();
	}

	public class GetMsgsFromMax extends Thread {
		private final String TAG = RemoteMsgManager.TAG + ":" + "GetMsgsFromMax";
		@Override
		public void run() {
			synchronized (this) {
				while (true) {
					// NEED to enforce invariant that socket gets set to null when closed.
					if (clientSocket == null) {
						connectToServer(RemoteMsgManager.MAX_IP);
					}
					try {
						this.wait(1000); // just check every second
					} catch (InterruptedException e) {
						// It's OK if we're interrupted; just continue.
					}
				}
			}
		}
	}
}
