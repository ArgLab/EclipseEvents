package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static final String MINER_DB_NAME = "s26_miner"; // ⚠️ verify

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

//    public static String detectActiveProjectName() {
//        try {
//            IWorkbenchWindow win =
//                PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//            if (win == null) return null;
//
//            IWorkbenchPage page = win.getActivePage();
//            if (page != null && page.getActiveEditor() != null) {
//                IEditorInput input = page.getActiveEditor().getEditorInput();
//                IFile file = input.getAdapter(IFile.class);
//                if (file != null) {
//                    return file.getProject().getName();
//                }
//            }
//
//            for (IProject p :
//                ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
//                if (p.isOpen()) return p.getName();
//            }
//        } catch (Exception ignored) {
//        	System.out.println("Exception Happened due to "+ignored.toString());
//        }
//        return null;
//    }
    
    //GitHub Repository Name Detection
    
    public static String detectActiveGitRepoName() {
        try {
            IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (win == null) return null;

            IWorkbenchPage page = win.getActivePage();
            if (page != null && page.getActiveEditor() != null) {
                IEditorInput input = page.getActiveEditor().getEditorInput();
                IFile file = input.getAdapter(IFile.class);
                if (file != null) {
                    IProject project = file.getProject();
                    File gitDir = findGitDirectory(project);
                    if (gitDir != null) {
                        File configFile = new File(gitDir, "config");
                        return parseRepoNameFromGitConfig(configFile);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception happened: " + e.toString());
        }
        return null;
    }

    private static File findGitDirectory(IProject project) {
        File dir = project.getLocation().toFile();
        
        // Search up to 5 levels up for .git directory
        for (int i = 0; i < 5 && dir != null; i++) {
            File gitDir = new File(dir, ".git");
            if (gitDir.exists() && gitDir.isDirectory()) {
                return gitDir;
            }
            dir = dir.getParentFile();
        }
        return null;
    }

    private static String parseRepoNameFromGitConfig(File configFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("url =")) {
                    String url = line.substring(5).trim();
                    return extractRepoNameFromUrl(url);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading git config: " + e.toString());
        }
        return null;
    }

    private static String extractRepoNameFromUrl(String url) {
        // Remove .git suffix
        url = url.replaceAll("\\.git$", "");
        // Get last part after / or :
        String[] parts = url.split("[/:]");
        return parts[parts.length - 1];
    }
}