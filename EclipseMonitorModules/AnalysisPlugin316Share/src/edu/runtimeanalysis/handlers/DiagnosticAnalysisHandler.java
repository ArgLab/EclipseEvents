package edu.runtimeanalysis.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.runtimeanalysis.core.AnalysisEngine;
import edu.runtimeanalysis.core.AnalysisProgressCallback;
import edu.runtimeanalysis.core.AnalysisResultsCallback;
import edu.runtimeanalysis.core.DiagnosticConfig;
import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AlgorithmRegistry;

/**
 * Handler for running diagnostic analysis on all methods to identify failures
 */
public class DiagnosticAnalysisHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        // Check if diagnostic tools are enabled
        if (!DiagnosticConfig.isDiagnosticAnalysisEnabled()) {
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Feature Disabled", 
                "Diagnostic analysis tools are currently disabled.\n\n" +
                "To enable diagnostic tools:\n" +
                "1. Open DiagnosticConfig.java\n" +
                "2. Set DIAGNOSTICS_ENABLED = true\n" +
                "3. Uncomment diagnostic commands in plugin.xml\n" +
                "4. Restart Eclipse or refresh the plugin");
            return null;
        }
        
        // Run diagnostic analysis in background thread
        Thread diagnosticThread = new Thread(() -> {
            try {
                runDiagnosticAnalysis(event);
            } catch (Exception e) {
                e.printStackTrace();
                // Show error dialog on UI thread
                HandlerUtil.getActiveShell(event).getDisplay().asyncExec(() -> {
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), 
                        "Diagnostic Analysis Error", 
                        "Failed to run diagnostic analysis: " + e.getMessage());
                });
            }
        });
        diagnosticThread.start();
        
        return null;
    }
    
    private void runDiagnosticAnalysis(ExecutionEvent event) throws Exception {
        // Create output file with timestamp in user's home directory
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "diagnostic-analysis-" + timestamp + ".txt";
        
        // Try multiple locations to avoid access denied issues
        File outputFile = null;
        String[] possiblePaths = {
            System.getProperty("user.home") + File.separator + fileName,  // User home directory
            System.getProperty("java.io.tmpdir") + File.separator + fileName,  // Temp directory
            System.getProperty("user.dir") + File.separator + fileName   // Current working directory
        };
        
        for (String path : possiblePaths) {
            try {
                File testFile = new File(path);
                // Test if we can write to this location
                if (testFile.getParentFile().canWrite() || testFile.getParentFile().mkdirs()) {
                    outputFile = testFile;
                    break;
                }
            } catch (Exception e) {
                // Try next location
            }
        }
        
        if (outputFile == null) {
            throw new Exception("Unable to find writable directory for diagnostic output file");
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write to both console and file
            logBoth("Starting comprehensive diagnostic analysis", writer);
            logBoth("Output file: " + outputFile.getAbsolutePath(), writer);
            
            // Get all algorithms from registry
            List<AlgorithmConfig> allAlgorithms = AlgorithmRegistry.getAllAlgorithms();
            logBoth("Testing " + allAlgorithms.size() + " algorithms", writer);
        
            // Results tracking
            List<String> successfulMethods = new ArrayList<>();
            List<String> failingMethods = new ArrayList<>();
            List<String> emptyGraphMethods = new ArrayList<>();
            List<String> partialFailureMethods = new ArrayList<>();
            Map<String, String> failureReasons = new HashMap<>();
            
            int totalTested = 0;
            
            for (AlgorithmConfig config : allAlgorithms) {
                totalTested++;
                String methodName = config.getName();
                
                logBoth("Testing " + totalTested + "/" + allAlgorithms.size() + ": " + methodName, writer);
            
            try {
                // Create analysis engine instance
                AnalysisEngine engine = new AnalysisEngine();
                
                // Run analysis with default settings (no custom callbacks needed for diagnostics)
                List<ExecutionResult> results = engine.runAnalysis(config);
                
                // Analyze results and capture detailed error information
                AnalysisResult analysisResult = analyzeResults(results, methodName);
                
                // Capture additional debug information for failing methods
                if (analysisResult.status != ResultStatus.SUCCESS) {
                    StringBuilder debugInfo = new StringBuilder();
                    if (results != null && !results.isEmpty()) {
                        debugInfo.append(" | First few results: ");
                        for (int i = 0; i < Math.min(3, results.size()); i++) {
                            ExecutionResult result = results.get(i);
                            debugInfo.append(String.format("[Size:%d, Time:%d, Success:%s, Timeout:%s]", 
                                result.getInputSize(), result.getExecutionTime(), 
                                result.isSuccess(), result.isTimedOut()));
                            if (i < Math.min(2, results.size() - 1)) debugInfo.append(", ");
                        }
                        
                        // Add error messages from failed results
                        debugInfo.append(" | Error messages: ");
                        for (int i = 0; i < Math.min(2, results.size()); i++) {
                            ExecutionResult result = results.get(i);
                            if (!result.isSuccess() && result.getError() != null) {
                                debugInfo.append("'").append(result.getError()).append("'");
                                if (i < Math.min(1, results.size() - 1)) debugInfo.append(", ");
                                break; // Only show first error message to avoid clutter
                            }
                        }
                    }
                    analysisResult.reason += debugInfo.toString();
                }
                
                switch (analysisResult.status) {
                    case SUCCESS:
                        successfulMethods.add(methodName);
                        logBoth("✓ SUCCESS: " + methodName + 
                            " (" + analysisResult.validDataPoints + " valid points)", writer);
                        break;
                    case EMPTY_GRAPH:
                        emptyGraphMethods.add(methodName);
                        failureReasons.put(methodName, analysisResult.reason);
                        logBoth("○ EMPTY: " + methodName + " - " + analysisResult.reason, writer);
                        break;
                    case PARTIAL_FAILURE:
                        partialFailureMethods.add(methodName);
                        failureReasons.put(methodName, analysisResult.reason);
                        logBoth("△ PARTIAL: " + methodName + " - " + analysisResult.reason, writer);
                        break;
                    case TOTAL_FAILURE:
                        failingMethods.add(methodName);
                        failureReasons.put(methodName, analysisResult.reason);
                        logBoth("✗ FAILED: " + methodName + " - " + analysisResult.reason, writer);
                        break;
                }
                
            } catch (Exception e) {
                failingMethods.add(methodName);
                
                // Capture detailed exception information
                String exceptionDetails = "Exception: " + e.getClass().getSimpleName();
                if (e.getMessage() != null) {
                    exceptionDetails += " - " + e.getMessage();
                }
                
                // Add stack trace snippet for more context (first few lines)
                if (e.getStackTrace() != null && e.getStackTrace().length > 0) {
                    exceptionDetails += " | Stack: ";
                    for (int i = 0; i < Math.min(2, e.getStackTrace().length); i++) {
                        StackTraceElement element = e.getStackTrace()[i];
                        exceptionDetails += element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber();
                        if (i < Math.min(1, e.getStackTrace().length - 1)) exceptionDetails += " -> ";
                    }
                }
                
                failureReasons.put(methodName, exceptionDetails);
                logBoth("✗ EXCEPTION: " + methodName + " - " + exceptionDetails, writer);
            }
        }
        
        // Print comprehensive summary
        printDiagnosticSummary(totalTested, successfulMethods, failingMethods, 
            emptyGraphMethods, partialFailureMethods, failureReasons, writer);
        
        // Create final variables for lambda access
        final String finalFilePath = outputFile.getAbsolutePath();
        final int finalTotalTested = allAlgorithms.size();
        final int finalSuccessful = successfulMethods.size();
        final int finalPartial = partialFailureMethods.size();
        final int finalEmpty = emptyGraphMethods.size();
        final int finalFailed = failingMethods.size();
        
        // Show completion message with file location
        HandlerUtil.getActiveShell(event).getDisplay().asyncExec(() -> {
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), 
                "Diagnostic Analysis Complete", 
                "Diagnostic analysis completed!\n\n" +
                "Results written to: " + finalFilePath + "\n" +
                "Total methods tested: " + finalTotalTested + "\n" +
                "✓ Successful: " + finalSuccessful + "\n" +
                "△ Partial failures: " + finalPartial + "\n" +
                "○ Empty graphs: " + finalEmpty + "\n" +
                "✗ Total failures: " + finalFailed);
        });
        
        } catch (IOException e) {
            throw new Exception("Failed to create diagnostic output file: " + e.getMessage(), e);
        }
    }
    
    private AnalysisResult analyzeResults(List<ExecutionResult> results, String methodName) {
        if (results == null || results.isEmpty()) {
            return new AnalysisResult(ResultStatus.TOTAL_FAILURE, "No results returned", 0);
        }
        
        int totalResults = results.size();
        int successCount = 0;
        int failureCount = 0;
        int timeoutCount = 0;
        int negativeTimeCount = 0;
        
        // Capture details about timing patterns
        double minTime = Double.MAX_VALUE;
        double maxTime = Double.MIN_VALUE;
        boolean hasValidTimes = false;
        
        for (ExecutionResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                // Check for suspicious timing values
                long executionTime = result.getExecutionTime();
                if (executionTime < 0) {
                    negativeTimeCount++;
                } else {
                    hasValidTimes = true;
                    minTime = Math.min(minTime, (double) executionTime);
                    maxTime = Math.max(maxTime, (double) executionTime);
                }
            } else if (result.isTimedOut()) {
                timeoutCount++;
            } else {
                failureCount++;
            }
        }
        
        // Build detailed reason string
        StringBuilder reasonBuilder = new StringBuilder();
        
        // Classify the result
        if (successCount == 0) {
            if (failureCount > 0) {
                reasonBuilder.append(failureCount).append(" failures, ").append(timeoutCount).append(" timeouts");
                return new AnalysisResult(ResultStatus.TOTAL_FAILURE, reasonBuilder.toString(), 0);
            } else {
                reasonBuilder.append("Only timeouts (").append(timeoutCount).append(")");
                return new AnalysisResult(ResultStatus.EMPTY_GRAPH, reasonBuilder.toString(), 0);
            }
        } else if (successCount == totalResults) {
            if (negativeTimeCount > 0) {
                reasonBuilder.append(negativeTimeCount).append(" negative times out of ").append(successCount).append(" successes");
                if (hasValidTimes) {
                    reasonBuilder.append(String.format(" | Valid times: %.1f-%.1fμs", minTime, maxTime));
                }
                return new AnalysisResult(ResultStatus.PARTIAL_FAILURE, reasonBuilder.toString(), successCount);
            } else {
                reasonBuilder.append("All ").append(successCount).append(" data points valid");
                if (hasValidTimes) {
                    reasonBuilder.append(String.format(" | Times: %.1f-%.1fμs", minTime, maxTime));
                }
                return new AnalysisResult(ResultStatus.SUCCESS, reasonBuilder.toString(), successCount);
            }
        } else {
            // Mixed results
            reasonBuilder.append(successCount).append(" successes, ").append(failureCount).append(" failures, ").append(timeoutCount).append(" timeouts");
            if (hasValidTimes) {
                reasonBuilder.append(String.format(" | Valid times: %.1f-%.1fμs", minTime, maxTime));
            }
            return new AnalysisResult(ResultStatus.PARTIAL_FAILURE, reasonBuilder.toString(), successCount);
        }
    }
    
    private void printDiagnosticSummary(int totalTested, List<String> successful, List<String> failed, 
            List<String> empty, List<String> partial, Map<String, String> reasons, PrintWriter writer) {
        
        logBoth("\n" + createRepeatedString("=", 80), writer);
        logBoth("DIAGNOSTIC ANALYSIS SUMMARY", writer);
        logBoth(createRepeatedString("=", 80), writer);
        logBoth("Total methods tested: " + totalTested, writer);
        logBoth("✓ Fully successful: " + successful.size() + " (" + String.format("%.1f%%", 100.0 * successful.size() / totalTested) + ")", writer);
        logBoth("△ Partial failures: " + partial.size() + " (" + String.format("%.1f%%", 100.0 * partial.size() / totalTested) + ")", writer);
        logBoth("○ Empty graphs: " + empty.size() + " (" + String.format("%.1f%%", 100.0 * empty.size() / totalTested) + ")", writer);
        logBoth("✗ Total failures: " + failed.size() + " (" + String.format("%.1f%%", 100.0 * failed.size() / totalTested) + ")", writer);
        
        if (!empty.isEmpty()) {
            logBoth("\n" + createRepeatedString("-", 50), writer);
            logBoth("EMPTY GRAPH METHODS (" + empty.size() + "):", writer);
            logBoth(createRepeatedString("-", 50), writer);
            for (String method : empty) {
                logBoth("○ " + method + " - " + reasons.get(method), writer);
            }
        }
        
        if (!partial.isEmpty()) {
            logBoth("\n" + createRepeatedString("-", 50), writer);
            logBoth("PARTIAL FAILURE METHODS (" + partial.size() + "):", writer);
            logBoth(createRepeatedString("-", 50), writer);
            for (String method : partial) {
                logBoth("△ " + method + " - " + reasons.get(method), writer);
            }
        }
        
        if (!failed.isEmpty()) {
            logBoth("\n" + createRepeatedString("-", 50), writer);
            logBoth("TOTAL FAILURE METHODS (" + failed.size() + "):", writer);
            logBoth(createRepeatedString("-", 50), writer);
            for (String method : failed) {
                logBoth("✗ " + method + " - " + reasons.get(method), writer);
            }
        }
        
        if (!successful.isEmpty()) {
            logBoth("\n" + createRepeatedString("-", 50), writer);
            logBoth("SUCCESSFUL METHODS (" + successful.size() + "):", writer);
            logBoth(createRepeatedString("-", 50), writer);
            for (String method : successful) {
                logBoth("✓ " + method, writer);
            }
        }
        
        logBoth("\n" + createRepeatedString("=", 80), writer);
        logBoth("DIAGNOSTIC ANALYSIS COMPLETE", writer);
        logBoth(createRepeatedString("=", 80), writer);
    }
    
    // Helper method for creating repeated strings (Java 8 compatible)
    private String createRepeatedString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    // Helper method to log to both console and file
    private void logBoth(String message, PrintWriter writer) {
        System.out.println(message);  // Console output
        writer.println(message);      // File output
        writer.flush();               // Ensure immediate write to file
    }
    
    // Result classification
    private enum ResultStatus {
        SUCCESS,         // All data points valid
        PARTIAL_FAILURE, // Some valid data points, some issues
        EMPTY_GRAPH,     // No valid data points (all timeouts/failures)
        TOTAL_FAILURE    // Analysis failed completely
    }
    
    private static class AnalysisResult {
        final ResultStatus status;
        String reason;
        final int validDataPoints;
        
        AnalysisResult(ResultStatus status, String reason, int validDataPoints) {
            this.status = status;
            this.reason = reason;
            this.validDataPoints = validDataPoints;
        }
    }
}