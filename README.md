Simple-VPN
==========

This is a demo implementation of a simple VPN to send data from one computer to another over a protected channel. The channel establishes a shared secret key using the Diffie Hellman key exchange. This shared secret key is then used to encrypt all further communication, thus resulting in a secure VPN connection, and the public network it uses becomes a protected channel. 

The encryption protocols used here are PBE with MD5 and DES. This is done to keep the implementation simple. As mentioned above, this is simply a demo, we are assuming that the encryption algorithm used is secure. 

The Java AWT and Swing libraries are used to create the user interface, and the Java security and crypto libraries to implement the protocol and key exchange. 

This was a class project at UBC and fellow students Audun Indergaard, Dhruv Raturi, and Denis Abalakov have also contributed to this project. 
