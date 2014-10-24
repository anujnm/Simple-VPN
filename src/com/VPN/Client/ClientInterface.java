package com.EECE412A3.Client;

public interface ClientInterface {

	//boolean return type signifies if call succeeded in performing expected work.
	public boolean startClient( String host, int port, String sharedKey ); //Start client and do handshake. Expected call GUIInterface.connectionReady()
	public void endConnection(); //End connection
	public boolean sendMessage( String s ); //sends a message
	public void receiveMessages(); //infinite blocking loop for receiving messages
}
