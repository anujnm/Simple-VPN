package com.EECE412A3;

public interface CryptoInterface {

	//boolean return type signifies if call succeeded in performing expected work.
	
	public boolean startServer( int port, String sharedKey ); //Start server and wait for client connections
	public boolean startClient( String host, int port, String sharedKey ); //Start client and do handshake. Expected call GUIInterface.connectionReady()
	public void endConnection(); //End connection
	public void closeServer(); //shutdown server
	public boolean sendMessage( String s ); //sends a message
	public void acceptConnection();
	public void receiveMessages();
}
