package edu.runtimeanalysis.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test class for timeout functionality
 */
public class TimeoutFunctionalityTest {
    
    @Test
    public void testExecutionResultTimeoutTracking() {
        // Test normal result
        ExecutionResult normal = new ExecutionResult(100, 500, true);
        assertFalse(normal.isTimedOut());
        assertTrue(normal.isSuccess());
        
        // Test timeout result
        ExecutionResult timedOut = new ExecutionResult(1000, 15000, false, "Timeout occurred", true);
        assertTrue(timedOut.isTimedOut());
        assertFalse(timedOut.isSuccess());
        assertEquals("Timeout occurred", timedOut.getError());
        assertEquals(15000, timedOut.getExecutionTime());
    }
    
    @Test
    public void testAnalysisEngineTimeoutConfiguration() {
        AnalysisEngine engine = new AnalysisEngine();
        
        // Test default timeout (15 seconds)
        // Can't easily test actual timeout without running analysis,
        // but we can test the timeout configuration
        
        assertDoesNotThrow(() -> {
            engine.setTimeoutSeconds(30);
        });
        
        assertDoesNotThrow(() -> {
            engine.setTimeoutSeconds(1);
        });
        
        // Test invalid timeout
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setTimeoutSeconds(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setTimeoutSeconds(-5);
        });
    }
    
    @Test
    public void testTimeoutResultsInChart() {
        // Test that timeout results can be processed by the view logic
        List<ExecutionResult> results = new ArrayList<>();
        results.add(new ExecutionResult(100, 50, true));  // Normal result
        results.add(new ExecutionResult(1000, 15000, false, "Timeout", true));  // Timeout result
        results.add(new ExecutionResult(10000, 200, true));  // Normal result
        
        // Count results that should be displayed (successful + timed out)
        int displayableResults = 0;
        for (ExecutionResult result : results) {
            if (result.isSuccess() || result.isTimedOut()) {
                displayableResults++;
            }
        }
        
        assertEquals(3, displayableResults, "All results (successful and timed out) should be displayable");
        
        // Verify timeout result properties
        ExecutionResult timeoutResult = results.get(1);
        assertTrue(timeoutResult.isTimedOut());
        assertFalse(timeoutResult.isSuccess());
        assertEquals(15000, timeoutResult.getExecutionTime()); // Should show timeout duration
    }
    
    @Test
    public void testTimeoutResultCreation() {
        // Test the specific timeout result creation pattern used in AnalysisEngine
        long timeoutMs = 15000; // 15 seconds
        int inputSize = 10000;
        long timeoutTimeScaled = timeoutMs * 1000; // Convert milliseconds to microseconds
        
        ExecutionResult timeoutResult = new ExecutionResult(
            inputSize, 
            timeoutTimeScaled, 
            false, 
            String.format("Execution timeout (%ds) for input size %d. This might indicate an infinite loop or very inefficient algorithm.", 
                        timeoutMs/1000, inputSize), 
            true
        );
        
        assertTrue(timeoutResult.isTimedOut());
        assertFalse(timeoutResult.isSuccess());
        assertEquals(inputSize, timeoutResult.getInputSize());
        assertEquals(timeoutTimeScaled, timeoutResult.getExecutionTime());
        assertTrue(timeoutResult.getError().contains("Execution timeout (15s)"));
        assertTrue(timeoutResult.getError().contains("input size 10000"));
        
        // Test display time conversion (what the view would do)
        double displayTime = timeoutResult.getExecutionTime(); // Already in microseconds
        assertEquals(15000000.0, displayTime); // Should be 15 seconds in microseconds
    }
}