package edu.runtimeanalysis.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.MethodSignature;

/**
 * Test class for progressive analysis updates
 */
public class AnalysisProgressiveUpdateTest {
    
    @Test
    public void testProgressiveResultsCallback() throws Exception {
        // Create test algorithm configuration
        AlgorithmConfig testConfig = new AlgorithmConfig(
            "Test Algorithm",
            "edu.test.TestSorter",
            "Test Category",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class, "public void sort(E[] data)")
        );
        
        // Track callbacks
        List<String> progressUpdates = new ArrayList<>();
        List<ExecutionResult> receivedResults = new ArrayList<>();
        List<Boolean> runningStates = new ArrayList<>();
        
        // Create results callback to track progressive updates
        AnalysisResultsCallback resultsCallback = new AnalysisResultsCallback() {
            @Override
            public void onAnalysisStarted(String algorithmName, int totalInputSizes) {
                progressUpdates.add("STARTED: " + algorithmName + " with " + totalInputSizes + " sizes");
            }
            
            @Override
            public void onResultAvailable(String algorithmName, List<ExecutionResult> allResults, 
                                         ExecutionResult newResult, boolean isRunning) {
                progressUpdates.add("RESULT: " + newResult.getInputSize() + " -> " + 
                                  (newResult.isSuccess() ? "SUCCESS" : "FAILED") + 
                                  " (running: " + isRunning + ")");
                receivedResults.add(newResult);
                runningStates.add(isRunning);
            }
            
            @Override
            public void onAnalysisCompleted(String algorithmName, List<ExecutionResult> finalResults, boolean success) {
                progressUpdates.add("COMPLETED: " + algorithmName + " with " + finalResults.size() + 
                                  " results (success: " + success + ")");
            }
        };
        
        // Create analysis engine and set callback
        AnalysisEngine engine = new AnalysisEngine();
        engine.setResultsCallback(resultsCallback);
        
        // This test will fail when trying to find the implementation, but we can test the callback setup
        int[] inputSizes = {100, 1000, 10000};
        
        try {
            engine.runAnalysis(testConfig, inputSizes);
            fail("Expected RuntimeException for missing implementation");
        } catch (RuntimeException e) {
            // Expected - the algorithm doesn't exist
            assertTrue(e.getMessage().contains("Cannot find your implementation"));
        }
        
        // Verify callbacks were called correctly
        assertFalse(progressUpdates.isEmpty(), "Should have received callback updates");
        assertTrue(progressUpdates.get(0).contains("STARTED"), "First update should be analysis started");
        assertTrue(progressUpdates.get(progressUpdates.size() - 1).contains("COMPLETED"), 
                  "Last update should be analysis completed");
        
        System.out.println("Progress updates received:");
        for (String update : progressUpdates) {
            System.out.println("  " + update);
        }
    }
    
    @Test
    public void testCallbackInterfaceContract() {
        // Test that the callback interface works as expected
        AnalysisResultsCallback callback = new AnalysisResultsCallback() {
            @Override
            public void onAnalysisStarted(String algorithmName, int totalInputSizes) {
                assertEquals("Test Algorithm", algorithmName);
                assertEquals(3, totalInputSizes);
            }
            
            @Override
            public void onResultAvailable(String algorithmName, List<ExecutionResult> allResults, 
                                         ExecutionResult newResult, boolean isRunning) {
                assertNotNull(algorithmName);
                assertNotNull(allResults);
                assertNotNull(newResult);
            }
            
            @Override
            public void onAnalysisCompleted(String algorithmName, List<ExecutionResult> finalResults, boolean success) {
                assertNotNull(algorithmName);
                assertNotNull(finalResults);
            }
        };
        
        // Test method calls
        assertDoesNotThrow(() -> {
            callback.onAnalysisStarted("Test Algorithm", 3);
            callback.onResultAvailable("Test", new ArrayList<>(), new ExecutionResult(100, 50, true), true);
            callback.onAnalysisCompleted("Test", new ArrayList<>(), true);
        });
    }
}