package edu.runtimeanalysis.handlers;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.runtimeanalysis.Activator;
import edu.runtimeanalysis.core.AnalysisEngine;
import edu.runtimeanalysis.core.AnalysisProgressCallback;
import edu.runtimeanalysis.core.DiagnosticConfig;
import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.export.ReportExportEngine;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AlgorithmRegistry;

/**
 * Debug handler that runs analysis for ALL algorithms in ALL workshops and exports a report
 */
public class DebugRunAllAnalysisHandler extends AbstractHandler {
    
    private static final int DEBUG_TIMEOUT_SECONDS = 5;
    private boolean isRunning = false;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        
        // Check if diagnostic tools are enabled
        if (!DiagnosticConfig.isDebugRunAllEnabled()) {
            MessageDialog.openInformation(shell, "Feature Disabled", 
                "Debug analysis tools are currently disabled.\n\n" +
                "To enable diagnostic tools:\n" +
                "1. Open DiagnosticConfig.java\n" +
                "2. Set DIAGNOSTICS_ENABLED = true\n" +
                "3. Uncomment diagnostic commands in plugin.xml\n" +
                "4. Restart Eclipse or refresh the plugin");
            return null;
        }
        
        if (isRunning) {
            MessageDialog.openInformation(shell, "Debug Analysis", 
                "Debug analysis is already running. Please wait for it to complete.");
            return null;
        }
        
        // Confirm with user
        boolean proceed = MessageDialog.openQuestion(shell, "Debug Analysis - Run All", 
            "This will run analysis for ALL algorithms in ALL workshops with a 5-second timeout.\n" +
            "This may take several minutes and will export a comprehensive report.\n\n" +
            "Do you want to proceed?");
        
        if (!proceed) {
            return null;
        }
        
        // Log the action
        Activator.getDefault().getUsageLogger().logAction("DEBUG_ALL_ANALYSIS_INITIATED", null);
        
        // Run analysis in separate thread
        new Thread(() -> {
            runDebugAnalysis(shell);
        }).start();
        
        return null;
    }
    
    private void runDebugAnalysis(Shell shell) {
        isRunning = true;
        
        try {
            // Get all algorithms from all workshops
            List<AlgorithmConfig> allAlgorithms = AlgorithmRegistry.getAllAlgorithms();
            String[] allWorkshops = AlgorithmRegistry.getCategories();
            
            System.out.println("[DebugRunAllAnalysisHandler] Starting debug analysis for " + 
                             allAlgorithms.size() + " algorithms across " + allWorkshops.length + " workshops");
            
            // Results storage
            Map<String, List<ExecutionResult>> allResults = new HashMap<>();
            List<String> successfulAlgorithms = new ArrayList<>();
            List<String> failedAlgorithms = new ArrayList<>();
            
            // Progress tracking
            int totalAlgorithms = allAlgorithms.size();
            int currentAlgorithm = 0;
            
            // Show initial progress dialog
            shell.getDisplay().asyncExec(() -> {
                MessageDialog.openInformation(shell, "Debug Analysis Started", 
                    "Debug analysis started for " + totalAlgorithms + " algorithms.\n" +
                    "Check the console for progress updates.\n" +
                    "A report will be generated when complete.");
            });
            
            for (AlgorithmConfig config : allAlgorithms) {
                currentAlgorithm++;
                String algorithmName = config.getName();
                
                System.out.println(String.format("[DebugRunAllAnalysisHandler] (%d/%d) Testing: %s", 
                                 currentAlgorithm, totalAlgorithms, algorithmName));
                
                try {
                    // Create analysis engine with debug timeout
                    AnalysisEngine engine = new AnalysisEngine();
                    engine.setTimeoutSeconds(DEBUG_TIMEOUT_SECONDS);
                    
                    // Create simple progress callback for console output
                    AnalysisProgressCallback progressCallback = new AnalysisProgressCallback() {
                        @Override
                        public void onAnalysisStart(int totalSteps) {
                            // Silent for debug mode
                        }
                        
                        @Override
                        public void updateProgress(int currentStep, int totalSteps, String stepDescription) {
                            // Silent for debug mode to avoid spam
                        }
                        
                        @Override
                        public void onAnalysisComplete(boolean success) {
                            // Silent for debug mode
                        }
                    };
                    
                    engine.setProgressCallback(progressCallback);
                    
                    // Run analysis (assumes implementations exist)
                    List<ExecutionResult> results = engine.runAnalysis(config);
                    
                    // Store results
                    allResults.put(algorithmName, results);
                    successfulAlgorithms.add(algorithmName);
                    
                    int successCount = (int) results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
                    System.out.println(String.format("[DebugRunAllAnalysisHandler] ✓ %s: %d data points collected", 
                                     algorithmName, successCount));
                    
                } catch (Exception e) {
                    failedAlgorithms.add(algorithmName + ": " + e.getMessage());
                    System.out.println(String.format("[DebugRunAllAnalysisHandler] ✗ %s: %s", 
                                     algorithmName, e.getMessage()));
                }
            }
            
            // Generate report
            System.out.println("[DebugRunAllAnalysisHandler] Generating comprehensive debug report...");
            generateDebugReport(allResults, successfulAlgorithms, failedAlgorithms, shell);
            
            // Show completion dialog
            shell.getDisplay().asyncExec(() -> {
                String message = String.format(
                    "Debug analysis completed!\n\n" +
                    "✓ Successful: %d algorithms\n" +
                    "✗ Failed: %d algorithms\n\n" +
                    "Comprehensive report has been exported.",
                    successfulAlgorithms.size(),
                    failedAlgorithms.size()
                );
                MessageDialog.openInformation(shell, "Debug Analysis Complete", message);
            });
            
            // Log completion
            Activator.getDefault().getUsageLogger().logAction("DEBUG_ALL_ANALYSIS_COMPLETED", 
                "successful=" + successfulAlgorithms.size() + ",failed=" + failedAlgorithms.size());
            
        } catch (Exception e) {
            System.err.println("[DebugRunAllAnalysisHandler] Critical error in debug analysis: " + e.getMessage());
            e.printStackTrace();
            
            shell.getDisplay().asyncExec(() -> {
                MessageDialog.openError(shell, "Debug Analysis Error", 
                    "Critical error during debug analysis:\n" + e.getMessage());
            });
            
        } finally {
            isRunning = false;
        }
    }
    
    private void generateDebugReport(Map<String, List<ExecutionResult>> allResults, 
                                   List<String> successfulAlgorithms, 
                                   List<String> failedAlgorithms, 
                                   Shell shell) {
        try {
            ReportExportEngine exportEngine = new ReportExportEngine();
            
            // Create comprehensive debug data
            Map<String, Object> debugData = new HashMap<>();
            debugData.put("results", allResults);
            debugData.put("successful", successfulAlgorithms);
            debugData.put("failed", failedAlgorithms);
            debugData.put("timeout", DEBUG_TIMEOUT_SECONDS);
            debugData.put("totalAlgorithms", successfulAlgorithms.size() + failedAlgorithms.size());
            
            // Export comprehensive report
            boolean success = exportEngine.exportDebugReport(debugData);
            
            if (success) {
                System.out.println("[DebugRunAllAnalysisHandler] Debug report exported successfully");
            } else {
                System.out.println("[DebugRunAllAnalysisHandler] Failed to export debug report");
            }
            
        } catch (Exception e) {
            System.err.println("[DebugRunAllAnalysisHandler] Error generating debug report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}