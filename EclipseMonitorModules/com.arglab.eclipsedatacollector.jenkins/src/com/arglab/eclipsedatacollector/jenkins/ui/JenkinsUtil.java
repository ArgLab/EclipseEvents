package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.eclipse.core.resources.*;
import org.eclipse.ui.*;

public class JenkinsUtil {

    private static final String API_BASE =
        "http://lin-sesmith01.csc.ncsu.edu:8080/api/build-info/latest/";

    private static final String MINER_DB_NAME = "f25_miner"; // ⚠️ verify

    /* ===================== TOKEN ===================== */

    public static String generateUserToken(
            String repositoryName,
            String unityId
    ) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = unityId + repositoryName + MINER_DB_NAME;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /* ===================== API CALL ===================== */

    public static String fetchLatestBuildJson(
            String repositoryName,
            String unityId
    ) throws Exception {

        String token = generateUserToken(repositoryName, unityId);
        if (token == null)
            throw new IllegalStateException("User token generation failed");

        String urlStr = API_BASE + repositoryName + "?unityId=" + unityId;
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-User-Token", token);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("HTTP " + status);
        }

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        reader.close();
        conn.disconnect();

        return sb.toString();
    }

    /* ===================== PROJECT DETECTION ===================== */

    public static String detectActiveProjectName() {
        try {
            IWorkbenchWindow win =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (win == null) return null;

            IWorkbenchPage page = win.getActivePage();
            if (page != null && page.getActiveEditor() != null) {
                IEditorInput input = page.getActiveEditor().getEditorInput();
                IFile file = input.getAdapter(IFile.class);
                if (file != null) {
                    return file.getProject().getName();
                }
            }

            for (IProject p :
                ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if (p.isOpen()) return p.getName();
            }
        } catch (Exception ignored) {
        	System.out.println("Exception Happened due to "+ignored.toString());
        }
        return null;
    }
}