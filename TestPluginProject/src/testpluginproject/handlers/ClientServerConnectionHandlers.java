package testpluginproject.handlers;

/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import testpluginproject.utils.Utils;

public class ClientServerConnectionHandlers {
	
	private static final String SERVER_HOST = "lurch.csc.ncsu.edu";
	private static final int SERVER_PORT = 5002;
	
	private Socket clientSocket;
	private ObjectOutputStream objectOutputStream;
	private DataInputStream dataInputStream;
	private DataOutputStream dataOutputStream;
	
	public ClientServerConnectionHandlers() {
		
	}

	public String [] connectToServer() {
		// TODO Auto-generated method stub	
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstanceStrong();
			random.setSeed(42);
			keyPairGenerator.initialize(1024,random);
			KeyPair keypair = keyPairGenerator.generateKeyPair();
			PublicKey publicKey = keypair.getPublic();
			PrivateKey privateKey = keypair.getPrivate();
			
			System.out.println("Public key: "+publicKey);
			String base64PublicKey = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
			String pem = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n" +
                    "-----END PUBLIC KEY-----";
			System.out.println("Public key after ps1: "+pem);
			System.out.println("Public key: with base64:"+base64PublicKey);
			clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT),5002);
			objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			objectOutputStream.writeObject(pem);
			objectOutputStream.flush();
			
			// getting back the response from the server
			dataInputStream = new DataInputStream(clientSocket.getInputStream());

			int messageLength = 128;  // Define the fixed length of each message
			byte[] encryptedMessage = new byte[messageLength];
			dataInputStream.read(encryptedMessage);
			byte [] IV1 = new byte[16];
			dataInputStream.read(IV1);
			byte[] clientID = new byte[16];
			dataInputStream.read(clientID);
			
			
			
			System.out.println("Client ID: "+bytesToHex(clientID));
			System.out.println("CLient IV: "+bytesToHex(IV1));
			byte [] IV2 = new byte[16];
			dataInputStream.read(IV2);
			
			byte[] clientSecret = new byte[16];
			dataInputStream.read(clientSecret);
			System.out.println("Client Secret: "+bytesToHex(clientSecret));
			System.out.println("CLient IV: "+bytesToHex(IV2));


			System.out.println("Recieve encrypted Data.");
			//Decryption of AES key
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding","BC");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			//cipher.
			byte [] key = cipher.doFinal(encryptedMessage);
			//SecretKey aesKey = new SecretKeySpec(key, "AES");
			
			String decryptedMessage = new String(key, "UTF-8");
			System.out.println("decrypted the AES symmetric key: "+decryptedMessage);
			
			SecretKey secretKey = new SecretKeySpec(key, "AES");
			
			Cipher cipherK = Cipher.getInstance("AES/CBC/NoPadding");
			cipherK.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV1));
			byte [] keyK = cipherK.doFinal(clientID);
			
			String decryptedClientID = new String(keyK, "UTF-8");
			System.out.println("decrypted the ClientID: "+decryptedClientID);
			
			Cipher cipherCS = Cipher.getInstance("AES/CBC/NoPadding");
			cipherCS.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV2));
			byte [] keyCS = cipherCS.doFinal(clientSecret);
			
			String decryptedClientSecret = new String(keyCS, "UTF-8");
			System.out.println("decrypted the Client Secret: "+decryptedClientSecret);
			String [] listStr = new String[3];
			listStr[0] = decryptedMessage;
			listStr[1] = decryptedClientID;
			listStr[2] = decryptedClientSecret;
			return listStr;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error Occuring due to "+e.getMessage());
			//e.printStackTrace();
		}
		return null;
	}
	
	public static String bytesToHex(byte[] bytes) {
	    StringBuilder result = new StringBuilder();
	    for (byte b : bytes) {
	        result.append(String.format("\\x%02x", b & 0xFF));
	    }
	    return result.toString();
	}
	public void sendencryptedMessage(String fileName, String aeskey,String clientSecret,String ClientId) {
		try {
			// Decode the secret key string from Base64
			
			System.out.println("AES key in file sending:"+aeskey);
			System.out.println("clientSecret key in file sending:"+clientSecret);
			System.out.println("ClientId key in file sending:"+ClientId);
			byte [] fileData = Files.readAllBytes(Path.of(fileName));
	        byte[] aesKey = aeskey.getBytes();
	        byte[] encryptedFile, iv1, iv2, encryptedClientSecret;
	       
	        	SecretKey secretKey = new SecretKeySpec(aesKey, "AES");
	        	
	        	Cipher fileCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        	fileCipher.init(Cipher.ENCRYPT_MODE, secretKey);
	        	
	        	
	        	encryptedFile = fileCipher.doFinal(fileData);
	        	iv1 = fileCipher.getIV();
	        	
	        	String enc_file = Base64.getEncoder().encodeToString(encryptedFile);
	        	
	        	Cipher clientSecretCipher = Cipher.getInstance("AES/CBC/NoPadding");
	            clientSecretCipher.init(Cipher.ENCRYPT_MODE, secretKey);

	            // Encrypt the client secret
	            encryptedClientSecret = clientSecretCipher.doFinal(clientSecret.getBytes());
	            iv2 = clientSecretCipher.getIV();
	            
	            
		        URL url = new URL("http://lurch.csc.ncsu.edu:5001");
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        
		     // Set request method to POST
		        connection.setRequestMethod("POST");
		        
		     // Set the request headers
		        connection.setRequestProperty("Client-Id", ClientId);
		        connection.setRequestProperty("Client-Secret", Base64.getEncoder().encodeToString(encryptedClientSecret));
		        connection.setRequestProperty("iv2", Base64.getEncoder().encodeToString(iv2));
		        connection.setRequestProperty("iv1", Base64.getEncoder().encodeToString(iv1));
		        connection.setRequestProperty("Content-Length", String.valueOf(encryptedFile.length));
		        connection.setRequestProperty("username", Utils.getUsernameFromPref());
		        String []fileNameThing = fileName.split("/");
		        String fn = fileNameThing[fileNameThing.length-1];
		        connection.setRequestProperty("filename", "client" + fn);

		        // Enable input/output streams for sending and receiving data
		        connection.setDoOutput(true);
		        connection.getOutputStream().write(enc_file.getBytes());

		        // Send the HTTP request
		        int responseCode = connection.getResponseCode();
		        
		        // Print the response code
		        System.out.println("Response Code: " + responseCode);
		        
		        connection.disconnect();
		        /**File delete code added in the successfully**/
		        Path path = FileSystems.getDefault().getPath(fileName);

		        try {
		            // Use Files.delete to delete the file
		        	if(responseCode==200) {
		        		Files.delete(path);
			            System.out.println("File deleted successfully.");
		        	}
		        	else {
		        		System.out.println("Need to send the file again later.");
		        	}
		        		
		            
		        } catch (IOException e) {
		            // Handle the exception if the file cannot be deleted
		            System.out.println("Unable to delete the file: " + e.getMessage());
		        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception happen to send a message as encrypted one."+e.getMessage());
		}
		
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public DataInputStream getDataInputStream() {
		return dataInputStream;
	}

	public void setDataInputStream(DataInputStream dataInputStream) {
		this.dataInputStream = dataInputStream;
	}

	public DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	public void setDataOutputStream(DataOutputStream dataOutputStream) {
		this.dataOutputStream = dataOutputStream;
	}
	
	
}
