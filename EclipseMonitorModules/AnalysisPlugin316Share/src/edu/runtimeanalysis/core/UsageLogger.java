package edu.runtimeanalysis.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.runtimeanalysis.Activator;

/**
 * Logs user interactions and analysis results
 */
public class UsageLogger {
    
    private static final String LOG_FILE_NAME = "runtime_analysis_log.json";
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private final String workspaceHash;
    
    public UsageLogger() {
        // Initialize log file in plugin state location
        IPath stateLocation = Platform.getStateLocation(Activator.getDefault().getBundle());
        logFile = new File(stateLocation.toFile(), LOG_FILE_NAME);
        
        // Setup date format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Generate workspace hash for anonymization
        workspaceHash = generateWorkspaceHash();
        
        // Ensure log file exists
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                // Write initial array bracket
                try (FileWriter writer = new FileWriter(logFile)) {
                    writer.write("[\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void logAction(String action, String details) {
        JSONObject entry = new JSONObject();
        entry.put("timestamp", dateFormat.format(new Date()));
        entry.put("studentId", workspaceHash);
        entry.put("action", action);
        
        if (details != null) {
            JSONObject detailsObj = new JSONObject();
            String[] parts = details.split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    detailsObj.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            entry.put("details", detailsObj);
        }
        
        writeLogEntry(entry);
    }
    
    public void logAnalysisResults(String algorithmName, List<ExecutionResult> results) {
        JSONObject entry = new JSONObject();
        entry.put("timestamp", dateFormat.format(new Date()));
        entry.put("studentId", workspaceHash);
        entry.put("action", "ANALYSIS_COMPLETED");
        
        JSONObject details = new JSONObject();
        details.put("algorithm", algorithmName);
        
        JSONArray resultsArray = new JSONArray();
        for (ExecutionResult result : results) {
            JSONObject resultObj = new JSONObject();
            resultObj.put("size", result.getInputSize());
            resultObj.put("time_ms", result.getExecutionTime());
            resultObj.put("success", result.isSuccess());
            if (!result.isSuccess() && result.getError() != null) {
                resultObj.put("error", result.getError());
            }
            resultsArray.put(resultObj);
        }
        
        details.put("results", resultsArray);
        entry.put("details", details);
        
        writeLogEntry(entry);
    }
    
    private void writeLogEntry(JSONObject entry) {
        try {
            // Read existing content
            String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
            
            // Handle first entry vs subsequent entries
            if (content.trim().equals("[")) {
                // First entry
                content = "[\n  " + entry.toString(2) + "\n";
            } else {
                // Remove closing bracket and add comma
                int lastBracket = content.lastIndexOf(']');
                if (lastBracket > 0) {
                    content = content.substring(0, lastBracket);
                }
                content = content.trim() + ",\n  " + entry.toString(2) + "\n";
            }
            
            // Write back with closing bracket
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(content + "]");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String generateWorkspaceHash() {
        try {
            String workspacePath = Platform.getLocation().toString();
            return Integer.toHexString(workspacePath.hashCode());
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    public void close() {
        // Ensure file is properly closed
        logAction("LOGGER_CLOSED", null);
    }
    
    public File getLogFile() {
        return logFile;
    }
}