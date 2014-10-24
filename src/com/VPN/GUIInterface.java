package com.EECE412A3;



public interface GUIInterface
{
   public void printf( String s ); //Prints to screen
   public void connectionReady(); //Connection is ready for messages
   public void connectionClosed(); //Connection is terminated
}