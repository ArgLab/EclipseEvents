package edu.runtimeanalysis.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.MethodSignature;

/**
 * Test class for HTMLReportGenerator
 */
public class HTMLReportGeneratorTest {
    
    @TempDir
    File tempDir;
    
    @Test
    public void testGenerateReport() throws Exception {
        // Create test algorithm configurations
        AlgorithmConfig bubbleSort = new AlgorithmConfig(
            "Bubble Sort",
            "edu.test.BubbleSorter",
            "Test Category",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class, "public void sort(E[] data)")
        );
        
        AlgorithmConfig insertionSort = new AlgorithmConfig(
            "Insertion Sort",
            "edu.test.InsertionSorter", 
            "Test Category",
            new MethodSignature("sort", new Class[]{Comparable[].class}, void.class, "public void sort(E[] data)")
        );
        
        // Create test execution results (now in microseconds)
        List<ExecutionResult> bubbleResults = new ArrayList<>();
        bubbleResults.add(new ExecutionResult(100, 5000, true));     // 5000μs
        bubbleResults.add(new ExecutionResult(1000, 100000, true));  // 100000μs
        bubbleResults.add(new ExecutionResult(10000, -1, false, "Timeout"));
        
        List<ExecutionResult> insertionResults = new ArrayList<>();
        insertionResults.add(new ExecutionResult(100, 2000, true));     // 2000μs
        insertionResults.add(new ExecutionResult(1000, 50000, true));   // 50000μs
        insertionResults.add(new ExecutionResult(10000, 1500000, true)); // 1500000μs
        
        // Create results map
        Map<AlgorithmConfig, List<ExecutionResult>> algorithmResults = new HashMap<>();
        algorithmResults.put(bubbleSort, bubbleResults);
        algorithmResults.put(insertionSort, insertionResults);
        
        // Generate report
        String outputPath = new File(tempDir, "test_report.html").getAbsolutePath();
        
        assertDoesNotThrow(() -> {
            HTMLReportGenerator.generateReport(algorithmResults, outputPath);
        });
        
        // Verify HTML file was created
        File htmlFile = new File(outputPath);
        assertTrue(htmlFile.exists(), "HTML report file should be created");
        assertTrue(htmlFile.length() > 0, "HTML report file should not be empty");
        
        // Verify HTML content contains expected elements
        String content = Files.readString(htmlFile.toPath());
        assertTrue(content.contains("Bubble Sort"), "Report should contain Bubble Sort");
        assertTrue(content.contains("Insertion Sort"), "Report should contain Insertion Sort");
        assertTrue(content.contains("Runtime Analysis Report"), "Report should have title");
        assertTrue(content.contains("100"), "Report should contain input size 100");
        assertTrue(content.contains("1000"), "Report should contain input size 1000");
        assertTrue(content.contains("μs"), "Report should show microsecond units");
        assertTrue(content.contains("Timeout"), "Report should show timeout results");
        
        // Verify charts directory was created
        File chartsDir = new File(tempDir, "charts");
        assertTrue(chartsDir.exists(), "Charts directory should be created");
        assertTrue(chartsDir.isDirectory(), "Charts path should be a directory");
        
        // Verify chart images were created
        File[] chartFiles = chartsDir.listFiles((dir, name) -> name.endsWith(".png"));
        assertNotNull(chartFiles, "Chart files should exist");
        assertEquals(2, chartFiles.length, "Should have 2 chart images for 2 algorithms");
        
        // Verify chart images are not empty
        for (File chartFile : chartFiles) {
            assertTrue(chartFile.length() > 0, "Chart image " + chartFile.getName() + " should not be empty");
        }
        
        System.out.println("Test HTML report generated successfully at: " + outputPath);
    }
    
    @Test
    public void testEmptyResults() throws Exception {
        // Test with empty results map
        Map<AlgorithmConfig, List<ExecutionResult>> emptyResults = new HashMap<>();
        String outputPath = new File(tempDir, "empty_report.html").getAbsolutePath();
        
        assertDoesNotThrow(() -> {
            HTMLReportGenerator.generateReport(emptyResults, outputPath);
        });
        
        // Verify HTML file was still created
        File htmlFile = new File(outputPath);
        assertTrue(htmlFile.exists(), "HTML report file should be created even with empty results");
        
        // Verify basic HTML structure
        String content = Files.readString(htmlFile.toPath());
        assertTrue(content.contains("Runtime Analysis Report"), "Report should have title");
        assertTrue(content.contains("Total algorithms analyzed: 0"), "Report should show 0 algorithms");
    }
}