package edu.runtimeanalysis.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.runtimeanalysis.core.AnalysisEngine;
import edu.runtimeanalysis.core.AnalysisProgressCallback;
import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AnalysisMode;

/**
 * Coordinates running multiple algorithm analyses for report export
 */
public class ReportExportEngine {
    
    private AnalysisProgressCallback progressCallback;
    
    /**
     * Set a progress callback to receive updates during export
     * 
     * @param callback The progress callback, or null to disable progress reporting
     */
    public void setProgressCallback(AnalysisProgressCallback callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Runs analysis for multiple algorithms and generates an HTML report
     * 
     * @param algorithms List of algorithms to analyze
     * @param inputSizes Array of input sizes to test
     * @param timeoutSeconds Timeout in seconds for each algorithm run
     * @param outputPath Path where the HTML report should be saved
     * @throws Exception if analysis or export fails
     */
    public void generateReport(List<AlgorithmConfig> algorithms, int[] inputSizes, int timeoutSeconds, String outputPath) throws Exception {
        
        // Calculate total steps for progress reporting
        int totalSteps = algorithms.size() * (2 + inputSizes.length * (3 + 5)); // Same calculation as AnalysisEngine
        int currentStep = 0;
        
        if (progressCallback != null) {
            progressCallback.onAnalysisStart(totalSteps);
        }
        
        // Map to store results for each algorithm
        Map<AlgorithmConfig, List<ExecutionResult>> algorithmResults = new LinkedHashMap<>();
        
        // Run analysis for each algorithm
        for (int i = 0; i < algorithms.size(); i++) {
            AlgorithmConfig algorithm = algorithms.get(i);
            
            if (progressCallback != null) {
                progressCallback.updateProgress(currentStep, totalSteps, 
                    String.format("Analyzing algorithm %d/%d: %s", i + 1, algorithms.size(), algorithm.getName()));
            }
            
            // Create a sub-progress callback that maps to our overall progress
            final int algorithmStartStep = currentStep;
            final int stepsPerAlgorithm = 2 + inputSizes.length * (3 + 5);
            
            try {
                // Create analysis engine for this algorithm
                AnalysisEngine engine = new AnalysisEngine();
                engine.setTimeoutSeconds(timeoutSeconds);
                
                AnalysisProgressCallback subCallback = new AnalysisProgressCallback() {
                    @Override
                    public void onAnalysisStart(int totalSteps) {
                        // Already handled by parent
                    }
                    
                    @Override
                    public void updateProgress(int currentStep, int totalSteps, String stepDescription) {
                        if (progressCallback != null) {
                            int overallStep = algorithmStartStep + currentStep;
                            progressCallback.updateProgress(overallStep, ReportExportEngine.this.getTotalSteps(algorithms, inputSizes), 
                                String.format("[%s] %s", algorithm.getName(), stepDescription));
                        }
                    }
                    
                    @Override
                    public void onAnalysisComplete(boolean success) {
                        // Will be handled after all algorithms complete
                    }
                };
                
                engine.setProgressCallback(subCallback);
                
                // For export reports, always use the new analysis method with algorithm-specific parameters
                // This ensures each algorithm uses its appropriate parameter type (position, load factor, etc.)
                List<ExecutionResult> results = engine.runAnalysis(algorithm);
                algorithmResults.put(algorithm, results);
                
                // Update progress
                currentStep += stepsPerAlgorithm;
                
                System.out.println("[ReportExportEngine] Completed analysis for: " + algorithm.getName() + 
                                 " (" + (i + 1) + "/" + algorithms.size() + ")");
                
            } catch (Exception e) {
                System.err.println("[ReportExportEngine] Failed to analyze " + algorithm.getName() + ": " + e.getMessage());
                
                // Create empty results to show the failure in the report
                List<ExecutionResult> failedResults = new ArrayList<>();
                for (int size : inputSizes) {
                    failedResults.add(new ExecutionResult(size, -1, false, "Analysis failed: " + e.getMessage()));
                }
                algorithmResults.put(algorithm, failedResults);
                
                // Still update progress
                currentStep += stepsPerAlgorithm;
            }
        }
        
        // Generate the HTML report
        if (progressCallback != null) {
            progressCallback.updateProgress(currentStep, totalSteps, "Generating HTML report...");
        }
        
        try {
            HTMLReportGenerator.generateReport(algorithmResults, outputPath);
            
            if (progressCallback != null) {
                progressCallback.updateProgress(totalSteps, totalSteps, "Report generation complete");
                progressCallback.onAnalysisComplete(true);
            }
            
            System.out.println("[ReportExportEngine] Report generation completed successfully: " + outputPath);
            
        } catch (Exception e) {
            System.err.println("[ReportExportEngine] Failed to generate HTML report: " + e.getMessage());
            
            if (progressCallback != null) {
                progressCallback.onAnalysisComplete(false);
            }
            
            throw new Exception("Failed to generate HTML report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exports a comprehensive debug report with all algorithm results
     * 
     * @param debugData Map containing debug results and metadata
     * @return true if export was successful, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean exportDebugReport(Map<String, Object> debugData) {
        try {
            Map<String, List<ExecutionResult>> allResults = (Map<String, List<ExecutionResult>>) debugData.get("results");
            List<String> successful = (List<String>) debugData.get("successful");
            List<String> failed = (List<String>) debugData.get("failed");
            int timeout = (Integer) debugData.get("timeout");
            int totalAlgorithms = (Integer) debugData.get("totalAlgorithms");
            
            // Generate timestamp for filename
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String outputPath = System.getProperty("user.home") + "/AnalysisPlugin_DebugReport_" + timestamp + ".html";
            
            // Generate comprehensive debug HTML report
            HTMLReportGenerator.generateDebugReport(
                allResults, successful, failed, timeout, totalAlgorithms, outputPath);
            
            System.out.println("[ReportExportEngine] Debug report exported successfully to: " + outputPath);
            return true;
            
        } catch (Exception e) {
            System.err.println("[ReportExportEngine] Failed to export debug report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calculates total steps for progress reporting
     */
    private int getTotalSteps(List<AlgorithmConfig> algorithms, int[] inputSizes) {
        return algorithms.size() * (2 + inputSizes.length * (3 + 5)) + 1; // +1 for HTML report generation
    }
}