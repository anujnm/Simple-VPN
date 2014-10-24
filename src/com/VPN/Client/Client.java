package com.EECE412A3.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.EECE412A3.CryptoInterface;
import com.EECE412A3.DiffieHellmanHelper;
import com.EECE412A3.GUIInterface;
import com.EECE412A3.Helpers;


public class Client implements ClientInterface {
	
	//Encryption Constants
	private static final byte[] SALT = {(byte) 0xa0, (byte) 0x4e, (byte) 0x2b, (byte) 0x92, (byte) 0x4a, (byte) 0xd6, (byte) 0x59, (byte) 0x86};
	private static final int ITERATION_COUNT = 25;
	
	private static PBEParameterSpec pbeParamSpec;
	
	private static final String SERVER_IDENTIFICATION = "server";
	private static final String CLIENT_IDENTIFICATION = "client";
	
	private GUIInterface m_gui;
	
	//communication streams
	private OutputStream out;
	private InputStream in;
	
	private String sharedSecret; //shared key
	private  SecretKey DESK; //session key
	
	public Client(GUIInterface gui)
	{
		m_gui = gui;
	}
	

	@Override
	public void endConnection() 
	{
	}
	
	@Override
	public void receiveMessages()
	{
		while(true)
		{
			try
			{
				byte[] input = read(in);
				m_gui.printf("Receiving encrypted bytes: " + Helpers.ByteToString(input));
				String modifiedSentence = new String(decryptDES(DESK, input));
				m_gui.printf(SERVER_IDENTIFICATION + " > " + modifiedSentence);
				
			}
			catch(Exception e)
			{}
		}
	}

	@Override
	public boolean sendMessage(String s) 
	{
		try
		{
			m_gui.printf("Sending message: " + s );
			byte[] output = s.getBytes();
			m_gui.printf("Bytes: " + Helpers.ByteToString(output));
			byte[] encrypted = encryptDES(DESK, output);
			m_gui.printf("Sending Encrypted bytes: " + Helpers.ByteToString(encrypted));
			write(out, encrypted);
			m_gui.printf(CLIENT_IDENTIFICATION + " > " + s);
		}
		catch(Exception e )
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean startClient(String host, int port, String sharedKey) 
	{
		sharedSecret = sharedKey;
		try
		{
			Socket clientSocket  = new Socket(host,port);
			
			DiffieHellmanHelper helpme = new DiffieHellmanHelper();
			
			BufferedReader userdata = new BufferedReader(new InputStreamReader(System.in));
			out = clientSocket.getOutputStream();
			in = clientSocket.getInputStream();
			
			//start the handshake
			String clientChallenge = java.util.UUID.randomUUID().toString();
			String phase1 = CLIENT_IDENTIFICATION+","+ clientChallenge;
			
			m_gui.printf("Sending handshake message: " + phase1 );
			
			write(out, phase1.getBytes());
			m_gui.printf("Waiting for Server respond with its challenge...");
			
			//read the server's public key
//			char[] serverPublicKeyEncoded = new char[255];
//			inFromServer.read(serverPublicKeyEncoded);
			byte[] serverChallenge = read(in);
			
			byte[] serverid = encryptHandshake(sharedSecret, read(in), Cipher.DECRYPT_MODE);
			byte[] clientChallengeFromServerBytes = encryptHandshake(sharedSecret, read(in), Cipher.DECRYPT_MODE);
			byte[] serverPublickKeyEnc = encryptHandshake(sharedSecret, read(in), Cipher.DECRYPT_MODE);
			
			m_gui.printf("Challenge From Server: " + new String(serverChallenge));
			//CHECK IF THE VALUES RECIEVED MATCH THE ORIGINAL VLUES!!!!!!!!!
			String clientChallengeFromServer = new String(clientChallengeFromServerBytes);
			
			if (!clientChallengeFromServer.equals(clientChallenge)) {
				m_gui.printf("Server returned incorrect challenge, terminating program");
				return false;
			}
			
			String serverIdentification = new String(serverid);
			m_gui.printf("Sever id: " + serverIdentification);
			if (!serverIdentification.equals(SERVER_IDENTIFICATION)) {
				m_gui.printf("Connected to incorrect server, terminating program");
				
				return false;
			}
			m_gui.printf("Connection to "+ serverIdentification + " has been established. Completing handshake protocol..");
			
			// instantiate a DH public key from the encoded key material and get key parameters.
	        PublicKey serverPubK = helpme.getUnencodedPublicKey(serverPublickKeyEnc);
	        DHParameterSpec DHParams = ((DHPublicKey)serverPubK).getParams();

	        //Generate client key pair using the params from the servers key
	        KeyPair clientKeyPair = helpme.createKeyPair(DHParams);
	        m_gui.printf("sending client session Key: " + clientKeyPair);
	        //encode pubk and send it over to server
	        byte[] clientEncPubk = clientKeyPair.getPublic().getEncoded();
	        m_gui.printf("Encoded pubk(client): " + DatatypeConverter.printHexBinary(clientEncPubk));
	        
	        //Return to the server with client Authentication
	        
	        //client id
	        write(out, encryptHandshake(sharedSecret, "client".getBytes(), Cipher.ENCRYPT_MODE));
	        //return the challenge sent from the server
	        write(out, encryptHandshake(sharedSecret, serverChallenge, Cipher.ENCRYPT_MODE));
	        //send client Public key
	        write(out, encryptHandshake(sharedSecret, clientEncPubk, Cipher.ENCRYPT_MODE));
	        
	        //Set serverPubK in the agreement
	        helpme.keyAgreement.doPhase(serverPubK, true);
	        
	        //generate the secret symmetric key
	        byte[] sharedSecretKey = helpme.keyAgreement.generateSecret();
	        
	        m_gui.printf("shared k(client): " + DatatypeConverter.printHexBinary(sharedSecretKey));
			
	        
	      //since the Key Agreement object was reset we regen it 
	        helpme.keyAgreement.doPhase(serverPubK, true);
	       DESK = helpme.keyAgreement.generateSecret("DES");
	       m_gui.connectionReady();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
		
	public byte[] read(InputStream in) throws IOException{
		byte[] lenPackage = new byte[4];
		in.read(lenPackage, 0, 4);
		ByteBuffer bb = ByteBuffer.wrap(lenPackage);
		int actuallen = bb.getInt();
		byte[] finalbb = new byte[actuallen];
		in.read(finalbb);
		return finalbb;
	}
	
	public void write(OutputStream out, byte[] towrite) throws IOException{
		ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(towrite.length);
        out.write(bb.array());
        out.write(towrite);
        out.flush();
	}
	
	public byte[] encryptDES(SecretKey k,byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
	    // Create PBE Cipher
	    Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    // Initialize PBE Cipher with key and parameters
	    desCipher.init(Cipher.ENCRYPT_MODE, k);
	    // Encrypt the cleartext
	    return desCipher.doFinal(plaintext);
	}
	
	public byte[] decryptDES(SecretKey k, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
	    // Create PBE Cipher
	    Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    // Initialize PBE Cipher with key and parameters
	    desCipher.init(Cipher.DECRYPT_MODE, k);
	    // Encrypt the cleartext
	    return desCipher.doFinal(plaintext);
	}
	
	public byte[] encryptHandshake(String sharedSecretKey, byte[] plainText, int encryption_mode) throws NoSuchAlgorithmException, 
			InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		PBEParameterSpec handshakeParamSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);
		PBEKeySpec handshakeKeySpec = new PBEKeySpec(sharedSecretKey.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbeKey = keyFactory.generateSecret(handshakeKeySpec);
		Cipher handshakeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		handshakeCipher.init(encryption_mode, pbeKey, handshakeParamSpec);
		return handshakeCipher.doFinal(plainText);
	}

}
