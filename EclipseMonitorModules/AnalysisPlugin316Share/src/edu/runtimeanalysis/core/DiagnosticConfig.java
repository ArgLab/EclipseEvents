package edu.runtimeanalysis.core;

/**
 * Configuration class for controlling diagnostic tools availability.
 * Provides centralized control over whether diagnostic features are enabled.
 */
public class DiagnosticConfig {
    
    /**
     * Master switch for all diagnostic tools.
     * Set to false to completely disable diagnostic menu items and functionality.
     * Set to true to enable diagnostic tools for debugging and testing.
     */
    public static final boolean DIAGNOSTICS_ENABLED = false;
    
    /**
     * Controls the "Debug: Run All Algorithms" feature.
     * This tool runs analysis on all registered algorithms with short timeout.
     * Useful for comprehensive testing across all workshops.
     */
    public static final boolean DEBUG_RUN_ALL_ENABLED = DIAGNOSTICS_ENABLED && true;
    
    /**
     * Controls the "Diagnostic: Test All Methods" feature.
     * This tool provides detailed failure analysis and generates diagnostic reports.
     * Useful for identifying which algorithms have implementation issues.
     */
    public static final boolean DIAGNOSTIC_ANALYSIS_ENABLED = DIAGNOSTICS_ENABLED && true;
    
    /**
     * Checks if diagnostic tools should be visible in the UI.
     * @return true if diagnostic tools should be shown, false otherwise
     */
    public static boolean areDiagnosticsEnabled() {
        return DIAGNOSTICS_ENABLED;
    }
    
    /**
     * Checks if the Debug Run All tool should be available.
     * @return true if Debug Run All should be enabled
     */
    public static boolean isDebugRunAllEnabled() {
        return DEBUG_RUN_ALL_ENABLED;
    }
    
    /**
     * Checks if the Diagnostic Analysis tool should be available.
     * @return true if Diagnostic Analysis should be enabled
     */
    public static boolean isDiagnosticAnalysisEnabled() {
        return DIAGNOSTIC_ANALYSIS_ENABLED;
    }
}