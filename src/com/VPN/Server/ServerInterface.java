package com.EECE412A3.Server;

public interface ServerInterface {

	//boolean return type signifies if call succeeded in performing expected work.
	public boolean startServer( int port, String sharedKey ); //Start server and wait for client connections
	public void endConnection(); //End connection
	public void closeServer(); //shutdown server
	public boolean sendMessage( String s ); //sends a message
	public void acceptConnection(); //blocking call to accept connections to server.
	public void receiveMessages(); //infinite blocking loop for receiving messages.
}
