package edu.runtimeanalysis.core;

/**
 * Callback interface for reporting analysis progress
 */
public interface AnalysisProgressCallback {
    
    /**
     * Called to update the progress of the analysis
     * 
     * @param currentStep Current step being executed (0-based)
     * @param totalSteps Total number of steps in the analysis
     * @param stepDescription Description of the current step
     */
    void updateProgress(int currentStep, int totalSteps, String stepDescription);
    
    /**
     * Called when analysis starts
     * 
     * @param totalSteps Total number of steps that will be executed
     */
    void onAnalysisStart(int totalSteps);
    
    /**
     * Called when analysis completes
     * 
     * @param success Whether the analysis completed successfully
     */
    void onAnalysisComplete(boolean success);
}