
package com.EECE412A3.Server;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
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
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.EECE412A3.CryptoInterface;
import com.EECE412A3.DiffieHellmanHelper;
import com.EECE412A3.GUIInterface;
import com.EECE412A3.Helpers;

public class Server implements CryptoInterface {
	
	private GUIInterface m_gui;
	
	private static final String SERVER_IDENTIFICATION = "server"; 
	
	private static final byte[] SALT = {(byte)0xa0, (byte)0x4e, (byte)0x2b, (byte)0x92, (byte)0x4a, (byte)0xd6, (byte)0x59, (byte)0x86};
	
	private static final int ITERATION_COUNT = 25;
	private Socket connectionSocket = null;
	private ServerSocket serverSocket =null;
	private String sharedSecretKey;
	private Cipher communicationEncryptionCipher;
	private Cipher communicationDecryptionCipher;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String clientId;
	
	public Server(GUIInterface gui)
	{
		m_gui = gui;
	}
	
	@Override
	public void closeServer()
	{		
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
				byte[] clientInput = readFromClient(inputStream);
				m_gui.printf("Receiving encrypted bytes: " + Helpers.ByteToString(clientInput));
				String clientInputDecrypted = new String(communicationDecryptionCipher.doFinal(clientInput));
				m_gui.printf( clientId + " > " + clientInputDecrypted);
			}
			catch(Exception e )
			{
				
			}
		}
	}

	@Override
	public boolean sendMessage(String s) 
	{
		try
		{
			m_gui.printf("Sending Message: " + s );
			byte[] bytes = s.getBytes();
			m_gui.printf("Bytes: " + Helpers.ByteToString(bytes) );
			byte[] encrypted = communicationEncryptionCipher.doFinal(bytes);
			m_gui.printf("Sending Encrypted bytes: " + Helpers.ByteToString(encrypted) );
			writeToClient(outputStream, encrypted);
			m_gui.printf(SERVER_IDENTIFICATION + " > " + s);
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
		throw new RuntimeException("Not Implemented");
	}
	
	/*protected void startConnection()
	{
		m_gui.connectionReady();
	}*/

	@Override
	public boolean startServer(int port, String sharedKey) {
		
		try
		{
			sharedSecretKey = sharedKey;
			serverSocket = new ServerSocket(port);
		}
		catch(IOException e)
		{
			m_gui.printf("Server Error: " + e.getMessage() );
			return false;
		}
		
		return true;
	}
	
	@Override
	public void acceptConnection()
	{
		
		try
		{
			if( serverSocket != null )
				connectionSocket = serverSocket.accept();
			
			if(connectionSocket == null ) m_gui.connectionClosed();
			
			 inputStream = connectionSocket.getInputStream();
			outputStream = connectionSocket.getOutputStream();
			//BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
			
			DiffieHellmanHelper diffieHellmanHelper = new DiffieHellmanHelper();
			
			// Receive identification data from client.
			byte[] identificationData = readFromClient(inputStream);
			String identificationString = new String(identificationData);
			m_gui.printf("Receiving handshake message: " + identificationString);
			
			String[] identificationArray = identificationString.split(",");;
			clientId = identificationArray[0];
			String clientChallenge = identificationArray[1];
			m_gui.printf(clientId + " has connected");
			m_gui.printf("Client challenge" + clientChallenge);
			
			// Generate Diffie-Hellman keypair, and send data to client to continue handshake. 
			DHParameterSpec diffieHellmanParameters = diffieHellmanHelper.generateDiffieHellmanParameters();
	        m_gui.printf("Generating Diffie Hellman key pair for server");
	        KeyPair serverKeyPair = diffieHellmanHelper.createKeyPair(diffieHellmanParameters);
	        
	        byte[] serverPublicKeyEncoded = serverKeyPair.getPublic().getEncoded();
	        
	        String serverChallenge = java.util.UUID.randomUUID().toString();
	        m_gui.printf("Sending server challenge: " + serverChallenge);
	        byte[] encryptedServerIdentification = encryptHandshake(sharedSecretKey, SERVER_IDENTIFICATION.getBytes(), Cipher.ENCRYPT_MODE);
	        writeToClient(outputStream, serverChallenge.getBytes());
	        m_gui.printf("Sending id: " + SERVER_IDENTIFICATION);
	        writeToClient(outputStream, encryptedServerIdentification);
	        m_gui.printf("Sending client challenge: " + clientChallenge);
	        writeToClient(outputStream, encryptHandshake(sharedSecretKey, clientChallenge.getBytes(), Cipher.ENCRYPT_MODE));
	        m_gui.printf("Sending server session Key Pair: " + serverKeyPair);
	        writeToClient(outputStream, encryptHandshake(sharedSecretKey, serverPublicKeyEncoded, Cipher.ENCRYPT_MODE));
	
	        byte[] identificationData2 = readFromClient(inputStream);
	        byte[] serverChallengeData = readFromClient(inputStream);
	        byte[] clientPublicKeyBytesEncoded = readFromClient(inputStream);
	        
	        String clientId2 = new String(encryptHandshake(sharedSecretKey, identificationData2, Cipher.DECRYPT_MODE));
	        if (!clientId2.equals(clientId)) {
	        	m_gui.printf("Incorrect client has connected, terminating program");
	        	m_gui.connectionClosed();
	        }
	        String serverChallengeReturned = new String(encryptHandshake(sharedSecretKey, serverChallengeData, Cipher.DECRYPT_MODE));
	        if (!serverChallengeReturned.equals(serverChallenge)) {
	        	m_gui.printf("Incorrect server challenge returned, terminating program");
	        	m_gui.connectionClosed();
	        }
	        byte[] unencryptedEncodedPublicKeyBytes = encryptHandshake(sharedSecretKey, clientPublicKeyBytesEncoded, Cipher.DECRYPT_MODE);
	        PublicKey clientPublicKey = diffieHellmanHelper.getUnencodedPublicKey(unencryptedEncodedPublicKeyBytes);
			diffieHellmanHelper.getKeyAgreement().doPhase(clientPublicKey, true);
			
			byte[] serverSharedKey = diffieHellmanHelper.getKeyAgreement().generateSecret();
			m_gui.printf("The shared key is: ");
			m_gui.printf(DatatypeConverter.printHexBinary(serverSharedKey));
			
			// Handshake is done, now encrypt all user input in DES format using shared secret key.
			diffieHellmanHelper.getKeyAgreement().doPhase(clientPublicKey, true);
			SecretKey serverDESKey = diffieHellmanHelper.getKeyAgreement().generateSecret("DES");
			communicationEncryptionCipher = getCommunicationCipher(Cipher.ENCRYPT_MODE, serverDESKey);
			communicationDecryptionCipher = getCommunicationCipher(Cipher.DECRYPT_MODE, serverDESKey);
			m_gui.connectionReady();
				
		}
		catch(Exception e )
		{
			m_gui.connectionClosed();;
		}
	}
	
	/*public static void main(String args[]) throws Exception {
		
		String userInput;
		while (true) {
			byte[] clientInput = readFromClient(inputStream);
			String clientInputDecrypted = new String(communicationDecryptionCipher.doFinal(clientInput));
			System.out.println(clientId + " sent: " + clientInputDecrypted);
			System.out.println(">>");
			userInput = userInputReader.readLine();
			writeToClient(outputStream, communicationEncryptionCipher.doFinal(userInput.getBytes()));
		}
	
	}*/
	
	private byte[] readFromClient(InputStream inputStream) throws IOException {
		byte[] lenMetaData = new byte[4];
		inputStream.read(lenMetaData, 0, 4);
		ByteBuffer metaDataBuffer = ByteBuffer.wrap(lenMetaData);
		int lengthMetaDataInt = metaDataBuffer.getInt();
		byte[] dataBuffer = new byte[lengthMetaDataInt];
		inputStream.read(dataBuffer);
		return dataBuffer;
	}
	
	private void writeToClient(OutputStream outputStream, byte[] dataBuffer) throws IOException {
		ByteBuffer metaDataBuffer = ByteBuffer.allocate(4);
		metaDataBuffer.putInt(dataBuffer.length);
		outputStream.write(metaDataBuffer.array());
		outputStream.write(dataBuffer);
		outputStream.flush();
	}
	
	// Taken from http://docs.oracle.com/javase/1.4.2/docs/guide/security/jce/JCERefGuide.html#PBEEx
	private byte[] encryptHandshake(String sharedSecretKey, byte[] plainText, int encryption_mode) throws InvalidKeySpecException, NoSuchAlgorithmException, 
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		PBEParameterSpec handshakeParamSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);
		PBEKeySpec handshakeKeySpec = new PBEKeySpec(sharedSecretKey.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbeKey = keyFactory.generateSecret(handshakeKeySpec);
		Cipher handshakeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		handshakeCipher.init(encryption_mode, pbeKey, handshakeParamSpec);
		return handshakeCipher.doFinal(plainText);
	}

	private Cipher getCommunicationCipher(int encryption_mode, SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher serverCipherForEncryption = Cipher.getInstance("DES/ECB/PKCS5Padding");
		serverCipherForEncryption.init(encryption_mode, secretKey);
		return serverCipherForEncryption;
	}
}
