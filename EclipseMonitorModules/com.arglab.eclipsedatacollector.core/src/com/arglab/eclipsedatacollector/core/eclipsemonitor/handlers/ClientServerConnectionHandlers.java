package com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientServerConnectionHandlers {

	private static final String SERVER_HOST = "https://lurch.csc.ncsu.edu";
	

	public ClientServerConnectionHandlers() {

	}

	public String[] connectToServer() {
		// TODO Auto-generated method stub
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstanceStrong();
			random.setSeed(42);
			keyPairGenerator.initialize(2048, random);
			KeyPair keypair = keyPairGenerator.generateKeyPair();
			PublicKey publicKey = keypair.getPublic();
			PrivateKey privateKey = keypair.getPrivate();

			System.out.println("Public key: " + publicKey);
			String base64PublicKey = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
			String pem = "-----BEGIN PUBLIC KEY-----\n" + Base64.getEncoder().encodeToString(publicKey.getEncoded())
					+ "\n" + "-----END PUBLIC KEY-----";
			System.out.println("Public key after ps1: " + pem);
			System.out.println("Public key: with base64:" + base64PublicKey);

			@SuppressWarnings("deprecation")
			URL url = new URL(SERVER_HOST + "/register");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");

			String jsonPayload = "{\"public_key\":\"" + base64PublicKey + "\"}";
			try (OutputStream os = connection.getOutputStream()) {
				os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
			}

			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				throw new RuntimeException("Failed to register: HTTP code " + responseCode);
			}

			InputStream responseStream = connection.getInputStream();
			String response = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
			connection.disconnect();
			
			// Parse JSON response
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

			// Extract fields
			String encAes   = jsonObject.get("enc_aes").getAsString();
			String clientID      = jsonObject.get("cid").getAsString();
			String cs       = jsonObject.get("cs").getAsString();
			String IV1    = jsonObject.get("cs_iv").getAsString();
	        
			System.out.println("Client ID: " + clientID);
			System.out.println("CLient IV: " + IV1);
			

			System.out.println("Recieve encrypted Data.");
			// Decryption of AES key
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
	        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
	        byte[] aesKey = rsaCipher.doFinal(Base64.getDecoder().decode(encAes));

	        // 5. Decrypt client secret with AES (same)...
	        SecretKey secretKey = new SecretKeySpec(aesKey, "AES");
	        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        aesCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(Base64.getDecoder().decode(IV1)));
	        byte[] decryptedCsBytes = aesCipher.doFinal(Base64.getDecoder().decode(cs));
	        String clientSecret = new String(decryptedCsBytes, StandardCharsets.UTF_8);
	        String AesEncoded = Base64.getEncoder().encodeToString(aesKey);
	        return new String[]{ Base64.getEncoder().encodeToString(aesKey), clientID, clientSecret};
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error Occuring due to " + e.getMessage());
			// e.printStackTrace();
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

	
	public void sendencryptedMessage(String fileName, String aeskeyB64, String clientSecret, String ClientId) {
		try {
			// Decode the secret key string from Base64

			System.out.println("AES key in file sending:" + aeskeyB64);
			System.out.println("clientSecret key in file sending:" + clientSecret);
			System.out.println("ClientId key in file sending:" + ClientId);
			byte[] fileData = Files.readAllBytes(Path.of(fileName));
			byte[] aesKey = Base64.getDecoder().decode(aeskeyB64);
			byte[] encryptedFile, iv1, iv2, encryptedClientSecret;

			SecretKey secretKey = new SecretKeySpec(aesKey, "AES");

			Cipher fileCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			fileCipher.init(Cipher.ENCRYPT_MODE, secretKey);

			encryptedFile = fileCipher.doFinal(fileData);
			iv1 = fileCipher.getIV();

			String enc_file = Base64.getEncoder().encodeToString(encryptedFile);

			Cipher clientSecretCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			clientSecretCipher.init(Cipher.ENCRYPT_MODE, secretKey);

			// Encrypt the client secret
			encryptedClientSecret = clientSecretCipher.doFinal(clientSecret.getBytes());
			iv2 = clientSecretCipher.getIV();

			@SuppressWarnings("deprecation")
			URL url = new URL(SERVER_HOST + "/upload");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			// Set request method to POST
			connection.setRequestMethod("POST");

			// Set the request headers
			connection.setRequestProperty("Client-Id", ClientId);
			connection.setRequestProperty("Client-Secret", Base64.getEncoder().encodeToString(encryptedClientSecret));
			connection.setRequestProperty("Filename", Path.of(fileName).getFileName().toString());
			connection.setRequestProperty("iv2", Base64.getEncoder().encodeToString(iv2));
			connection.setRequestProperty("iv1", Base64.getEncoder().encodeToString(iv1));
			connection.setRequestProperty("Content-Length", String.valueOf(encryptedFile.length));
			connection.setRequestProperty("username", Utils.getUsernameFromPref());

			// Send file content (Base64 encoded)
	        try (OutputStream os = connection.getOutputStream()) {
	            os.write(Base64.getEncoder().encode(encryptedFile));
	        }

	        int responseCode = connection.getResponseCode();
	        System.out.println("Response Code: " + responseCode);
	        if (responseCode == 200) {
	            Files.delete(Path.of(fileName));
	            System.out.println("File uploaded and deleted successfully.");
	        } else {
	            System.out.println("File upload failed: " + connection.getResponseMessage());
	        }


			connection.disconnect();
			/** File delete code added in the successfully **/
			Path path = FileSystems.getDefault().getPath(fileName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception happen to send a message as encrypted one." + e.getMessage());
		}

	}

}
