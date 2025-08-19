// File: ExecutionResult.java
package edu.runtimeanalysis.core;

/**
 * Result of a single algorithm execution
 */
public class ExecutionResult {
    private final double xValue;         // X-axis value (could be size, position, load factor, etc.)
    private final long executionTime;
    private final boolean success;
    private final String error;
    private final boolean timedOut;
    private final String scenarioName;   // Optional scenario name for multi-line analysis
    
    // Backward compatibility constructors using int inputSize
    public ExecutionResult(int inputSize, long executionTime, boolean success) {
        this((double) inputSize, executionTime, success, null, false, null);
    }
    
    public ExecutionResult(int inputSize, long executionTime, boolean success, String error) {
        this((double) inputSize, executionTime, success, error, false, null);
    }
    
    public ExecutionResult(int inputSize, long executionTime, boolean success, String error, boolean timedOut) {
        this((double) inputSize, executionTime, success, error, timedOut, null);
    }
    
    // New constructors using double xValue
    public ExecutionResult(double xValue, long executionTime, boolean success) {
        this(xValue, executionTime, success, null, false, null);
    }
    
    public ExecutionResult(double xValue, long executionTime, boolean success, String error) {
        this(xValue, executionTime, success, error, false, null);
    }
    
    public ExecutionResult(double xValue, long executionTime, boolean success, String error, boolean timedOut) {
        this(xValue, executionTime, success, error, timedOut, null);
    }
    
    // Full constructor with scenario name
    public ExecutionResult(double xValue, long executionTime, boolean success, String error, boolean timedOut, String scenarioName) {
        this.xValue = xValue;
        this.executionTime = executionTime;
        this.success = success;
        this.error = error;
        this.timedOut = timedOut;
        this.scenarioName = scenarioName;
    }
    
    public double getXValue() {
        return xValue;
    }
    
    // Backward compatibility
    public int getInputSize() {
        return (int) xValue;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
    
    public boolean isTimedOut() {
        return timedOut;
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
}