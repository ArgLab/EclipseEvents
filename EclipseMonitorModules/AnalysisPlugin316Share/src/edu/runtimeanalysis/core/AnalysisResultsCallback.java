package edu.runtimeanalysis.core;

import java.util.List;

/**
 * Callback interface for receiving progressive analysis results
 */
public interface AnalysisResultsCallback {
    
    /**
     * Called when a new result is available (after each input size completes)
     * 
     * @param algorithmName Name of the algorithm being analyzed
     * @param allResults All results collected so far (including the new one)
     * @param newResult The newly completed result
     * @param isRunning Whether the analysis is still running (true) or complete (false)
     */
    void onResultAvailable(String algorithmName, List<ExecutionResult> allResults, 
                          ExecutionResult newResult, boolean isRunning);
    
    /**
     * Called when the analysis starts
     * 
     * @param algorithmName Name of the algorithm being analyzed
     * @param totalInputSizes Total number of input sizes that will be tested
     */
    void onAnalysisStarted(String algorithmName, int totalInputSizes);
    
    /**
     * Called when the analysis completes (successfully or with error)
     * 
     * @param algorithmName Name of the algorithm that was analyzed
     * @param finalResults All results from the completed analysis
     * @param success Whether the analysis completed successfully
     */
    void onAnalysisCompleted(String algorithmName, List<ExecutionResult> finalResults, boolean success);
}