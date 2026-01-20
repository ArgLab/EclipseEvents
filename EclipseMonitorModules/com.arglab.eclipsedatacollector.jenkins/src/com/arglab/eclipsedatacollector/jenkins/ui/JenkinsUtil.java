package com.arglab.eclipsedatacollector.jenkins.ui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.gson.JsonObject;

public class JenkinsUtil {

<<<<<<< Updated upstream
	 public static String getActiveProjectAndFile() {
	        String projectName = null;
	        String fileName = null;

	        try {
	            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	            if (window != null) {
	                IWorkbenchPage page = window.getActivePage();
	                if (page != null) {
	                    IEditorPart editor = page.getActiveEditor();
	                    if (editor != null) {
	                        IEditorInput input = editor.getEditorInput();
	                        if (input instanceof IFileEditorInput fei) {
	                            IPath path = fei.getFile().getFullPath();
	                            String absPath = path.toFile().getAbsolutePath();
=======
    private static final String API_BASE =
        "http://lin-sesmith01.csc.ncsu.edu:8080/api/build-info/latest/";

    private static final String MINER_DB_NAME = "f25_miner"; // ⚠️ verify
>>>>>>> Stashed changes

	                            // Split by system file separator
	                            String[] parts = absPath.split(
	                                    Matcher.quoteReplacement(System.getProperty("file.separator")));

	                            // project name (2nd element of path if workspace layout is standard)
	                            if (parts.length > 1) {
	                                projectName = parts[1];
	                            }

	                            fileName = fei.getFile().getName();

	                            System.out.println("Active path: " + absPath);
	                            System.out.println("Project: " + projectName);
	                            System.out.println("File: " + fileName);
	                        }
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return projectName;
	    }

	 public static String getUserHash(String repositoryName, String unityId, String minerDbName) {
		// TODO Auto-generated method stub
			try {
	    		// Step 1: Get an instance of the SHA-256 MessageDigest.
	            // This is the standard way to get a cryptographic hash function in Java.
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            
	            String input = unityId + repositoryName + minerDbName;
//	            System.out.println(input);
	            // Step 2: Convert the input string into a byte array.
	            // Using StandardCharsets.UTF_8 ensures consistent hashing across platforms.
	            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

	            // Step 3: Convert the byte array into a hexadecimal string.
	            // This is the standard representation for a hash.
	            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
	            for (byte b : encodedhash) {
	                // Use bitwise AND with 0xFF to handle negative byte values correctly.
	                String hex = Integer.toHexString(0xff & b);
	                if (hex.length() == 1) {
	                    hexString.append('0'); // Pad with a leading zero if needed.
	                }
	                hexString.append(hex);
	            }
	            
	            return hexString.toString();
	            
	    	} catch (NoSuchAlgorithmException e) {
	    		return null;
	    	}
	 }
	 
	 static String encodePath(String s) {
	        // Minimal path segment encoding (replace spaces etc.). Adjust if needed.
	        return s.replace(" ", "%20");
	    }
	 
	 
	 public static String buildRequestBody(String userToken) {
	        // Adjust if your API expects a different shape
	        JsonObject body = new JsonObject();
	        body.addProperty("userToken", userToken);
	        return body.toString();
	    }
	 
	 public static String fetchJsonFromApi(String url, String jsonBody) throws Exception {
	        HttpClient client = HttpClient.newBuilder()
	                .connectTimeout(Duration.ofSeconds(5))
	                .build();

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(url))
	                .timeout(Duration.ofSeconds(20))
	                .header("Content-Type", "application/json")
	                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
	                .build();

	        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
	        int code = resp.statusCode();
	        if (code >= 200 && code < 300) {
	            return resp.body();
	        }
	        throw new RuntimeException("HTTP " + code + ": " + resp.body());
	    }
}
