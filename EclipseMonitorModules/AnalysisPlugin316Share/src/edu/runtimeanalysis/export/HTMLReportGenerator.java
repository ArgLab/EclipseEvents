package edu.runtimeanalysis.export;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AnalysisMode;

/**
 * Generates HTML reports containing runtime analysis charts
 * 
 * Note: This implementation creates a comprehensive HTML report with embedded chart images.
 * The HTML report can be opened in a browser and saved/printed as PDF if needed.
 */
public class HTMLReportGenerator {
    
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;
    private static final int CHARTS_PER_ROW = 2;
    
    /**
     * Generates an HTML report with multiple algorithm analysis charts
     * 
     * @param algorithmResults Map of algorithm configurations to their execution results
     * @param outputPath Path where the HTML file should be saved (should end with .html)
     * @throws Exception if report generation fails
     */
    public static void generateReport(Map<AlgorithmConfig, List<ExecutionResult>> algorithmResults, 
                                    String outputPath) throws Exception {
        
        System.out.println("[HTMLReportGenerator] Starting HTML report generation");
        System.out.println("[HTMLReportGenerator] Output path: " + outputPath);
        System.out.println("[HTMLReportGenerator] Number of algorithms: " + algorithmResults.size());
        
        // This approach works without additional PDF library dependencies
        generateHTMLReport(algorithmResults, outputPath);
        
        // TODO: Implement full PDF generation with iText library
        // To add iText dependency:
        // 1. Download iText JAR file
        // 2. Add to lib/ directory
        // 3. Update MANIFEST.MF Bundle-ClassPath
        // 4. Implement direct PDF generation
    }
    
    /**
     * Generates an HTML report that displays the analysis charts
     * This can be opened in a browser and printed/saved as PDF
     */
    private static void generateHTMLReport(Map<AlgorithmConfig, List<ExecutionResult>> algorithmResults, 
                                         String outputPath) throws Exception {
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Runtime Analysis Report</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        .header { text-align: center; margin-bottom: 30px; }\n");
        html.append("        .chart-grid { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; }\n");
        html.append("        .chart-container { border: 1px solid #ccc; padding: 10px; text-align: center; }\n");
        html.append("        .chart-title { font-weight: bold; margin-bottom: 10px; }\n");
        html.append("        .chart-image { max-width: 100%; height: auto; }\n");
        html.append("        .summary { margin-top: 30px; }\n");
        html.append("        .data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }\n");
        html.append("        .data-table th, .data-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("        .data-table th { background-color: #f2f2f2; }\n");
        html.append("        .reflection-section { margin-top: 40px; border: 2px solid #2196F3; border-radius: 8px; padding: 20px; background-color: #f8f9fa; }\n");
        html.append("        .reflection-title { color: #1976D2; margin-bottom: 15px; border-bottom: 2px solid #2196F3; padding-bottom: 10px; }\n");
        html.append("        .reflection-textarea { width: 100%; min-height: 200px; padding: 15px; border: 1px solid #ccc; border-radius: 4px; font-family: Arial, sans-serif; font-size: 14px; line-height: 1.6; resize: vertical; }\n");
        html.append("        .reflection-instructions { margin-bottom: 15px; padding: 10px; background-color: #e3f2fd; border-left: 4px solid #2196F3; font-style: italic; }\n");
        html.append("        @media print {\n");
        html.append("            .chart-container { break-inside: avoid; page-break-inside: avoid; }\n");
        html.append("            .reflection-section { break-inside: avoid; page-break-inside: avoid; }\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>Runtime Analysis Report</h1>\n");
        html.append("        <p>Generated on: ").append(java.time.LocalDateTime.now().toString()).append("</p>\n");
        html.append("    </div>\n");
        
        html.append("    <div class=\"chart-grid\">\n");
        
        // Create charts directory
        String baseDir = new File(outputPath).getParent();
        String chartsDir = baseDir + File.separator + "charts";
        new File(chartsDir).mkdirs();
        
        int chartIndex = 0;
        for (Map.Entry<AlgorithmConfig, List<ExecutionResult>> entry : algorithmResults.entrySet()) {
            AlgorithmConfig config = entry.getKey();
            List<ExecutionResult> results = entry.getValue();
            
            System.out.println("[HTMLReportGenerator] Processing chart for: " + config.getName());
            
            // Generate chart image
            String chartFileName = "chart_" + chartIndex + ".png";
            String chartPath = chartsDir + File.separator + chartFileName;
            JFreeChart chart = createChart(config, results);
            saveChartAsImage(chart, chartPath, CHART_WIDTH, CHART_HEIGHT);
            
            // Add chart to HTML
            html.append("        <div class=\"chart-container\">\n");
            html.append("            <div class=\"chart-title\">").append(config.getName()).append("</div>\n");
            html.append("            <img src=\"charts/").append(chartFileName).append("\" alt=\"").append(config.getName()).append(" Chart\" class=\"chart-image\">\n");
            html.append("            <p><strong>Expected Complexity:</strong> ").append(config.getExpectedComplexity()).append("</p>\n");
            
            // Add data table
            html.append("            <table class=\"data-table\">\n");
            html.append("                <tr><th>Input Size</th><th>Time (μs)</th><th>Status</th></tr>\n");
            
            for (ExecutionResult result : results) {
                html.append("                <tr>\n");
                html.append("                    <td>").append(result.getInputSize()).append("</td>\n");
                if (result.isSuccess() || result.isTimedOut()) {
                    double displayTime = result.getExecutionTime(); // Already in microseconds
                    if (displayTime == 0) {
                        displayTime = 1; // Show as 1 microsecond minimum
                    }
                    html.append("                    <td>").append(String.format("%.1f", displayTime)).append("</td>\n");
                    html.append("                    <td>").append(result.isTimedOut() ? "TIMEOUT" : "SUCCESS").append("</td>\n");
                } else {
                    html.append("                    <td>-</td>\n");
                    html.append("                    <td>ERROR</td>\n");
                }
                html.append("                </tr>\n");
            }
            
            html.append("            </table>\n");
            html.append("        </div>\n");
            
            chartIndex++;
        }
        
        html.append("    </div>\n");
        
        // Add Analysis Reflection section
        html.append("    <div class=\"reflection-section\">\n");
        html.append("        <h2 class=\"reflection-title\">📝 Analysis Reflection</h2>\n");
        html.append("        <div class=\"reflection-instructions\">\n");
        html.append("            <strong>Instructions:</strong> After reviewing your runtime analysis results above, use the space below to reflect on your findings. Consider the following questions:\n");
        html.append("            <ul style=\"margin: 10px 0; padding-left: 20px;\">\n");
        html.append("                <li>Do the measured performance results align with the expected Big-O complexities?</li>\n");
        html.append("                <li>Are there any surprising or unexpected patterns in the data?</li>\n");
        html.append("                <li>How do different algorithm implementations compare in terms of actual performance?</li>\n");
        html.append("                <li>What factors might explain any discrepancies between theoretical and measured performance?</li>\n");
        html.append("                <li>What insights about algorithm efficiency can you draw from these results?</li>\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        html.append("        <textarea class=\"reflection-textarea\" placeholder=\"Write your analysis reflection here...\\n\\nExample:\\n- The bubble sort results clearly show O(n²) behavior as input size increases...\\n- Surprisingly, the insertion sort performed better than expected for small input sizes...\\n- The logarithmic growth pattern in the binary search tree operations confirms the theoretical O(log n) complexity...\"></textarea>\n");
        html.append("    </div>\n");
        
        // Add summary section
        html.append("    <div class=\"summary\">\n");
        html.append("        <h2>Summary</h2>\n");
        html.append("        <ul>\n");
        html.append("            <li>Total algorithms analyzed: ").append(algorithmResults.size()).append("</li>\n");
        html.append("            <li>Charts arranged in ").append(CHARTS_PER_ROW).append("-column grid layout</li>\n");
        html.append("            <li>Time measurements in microseconds (μs) for better precision</li>\n");
        html.append("            <li>Logarithmic scales used for better visualization of complexity differences</li>\n");
        html.append("        </ul>\n");
        html.append("        <p><em>Tip: Use your browser's print function to save this report as PDF</em></p>\n");
        html.append("    </div>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        // Write HTML file
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(html.toString().getBytes("UTF-8"));
        }
        
        System.out.println("[HTMLReportGenerator] HTML report generated successfully: " + outputPath);
    }
    
    /**
     * Creates a JFreeChart for a single algorithm
     */
    private static JFreeChart createChart(AlgorithmConfig config, List<ExecutionResult> results) {
        String xAxisLabel = config.getAnalysisMode().getXAxisLabel();
        
        // Create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        // Check if this is a multi-line analysis
        boolean isMultiLine = isMultiLineAnalysis(config.getAnalysisMode());
        
        if (isMultiLine) {
            // Group results by scenario name for multi-line analysis
            java.util.Map<String, java.util.List<ExecutionResult>> groupedResults = new java.util.HashMap<>();
            
            for (ExecutionResult result : results) {
                if (result.isSuccess()) {
                    String scenarioName = result.getScenarioName();
                    if (scenarioName == null || scenarioName.isEmpty()) {
                        scenarioName = config.getName();
                    }
                    groupedResults.computeIfAbsent(scenarioName, k -> new java.util.ArrayList<>()).add(result);
                }
            }
            
            // Create separate series for each scenario
            for (java.util.Map.Entry<String, java.util.List<ExecutionResult>> entry : groupedResults.entrySet()) {
                String scenarioName = entry.getKey();
                java.util.List<ExecutionResult> scenarioResults = entry.getValue();
                XYSeries series = new XYSeries(scenarioName);
                
                for (ExecutionResult result : scenarioResults) {
                    double displayTime = result.getExecutionTime();
                    if (displayTime == 0) {
                        displayTime = 1;
                    }
                    series.add(result.getXValue(), displayTime);
                }
                
                dataset.addSeries(series);
            }
        } else {
            // Single series for traditional analysis
            XYSeries series = new XYSeries(config.getName());
            
            for (ExecutionResult result : results) {
                if (result.isSuccess()) {
                    double displayTime = result.getExecutionTime(); // Already in microseconds
                    if (displayTime == 0) {
                        displayTime = 1; // Show as 1 microsecond minimum
                    }
                    series.add(result.getXValue(), displayTime); // Use getXValue() instead of getInputSize()
                }
            }
            
            dataset.addSeries(series);
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            config.getName() + " - Runtime Analysis",
            xAxisLabel,
            "Time (μs)",
            dataset,
            PlotOrientation.VERTICAL,
            true, // Show legend
            true, // Show tooltips
            false // No URLs
        );
        
        // Customize the plot
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Set logarithmic axes
        LogAxis xAxis = new LogAxis(xAxisLabel);
        xAxis.setBase(10);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setDomainAxis(xAxis);
        
        LogAxis yAxis = new LogAxis("Time (μs)");
        yAxis.setBase(10);
        yAxis.setAutoRange(true);
        plot.setRangeAxis(yAxis);
        
        // Customize renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
        
        return chart;
    }
    
    /**
     * Determines if an analysis mode should create multiple series (multi-line chart)
     */
    private static boolean isMultiLineAnalysis(AnalysisMode mode) {
        return mode == AnalysisMode.POSITION_BASED || mode == AnalysisMode.INPUT_SIZE_WITH_POSITIONS || mode == AnalysisMode.INPUT_SIZE_WITH_COLLISIONS;
    }
    
    /**
     * Saves a chart as PNG image file
     */
    private static void saveChartAsImage(JFreeChart chart, String filePath, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new java.awt.Rectangle(0, 0, width, height));
        g2.dispose();
        
        ImageIO.write(image, "PNG", new File(filePath));
        System.out.println("[HTMLReportGenerator] Saved chart image: " + filePath);
    }
    
    /**
     * Generates a comprehensive debug report with all algorithm results
     */
    public static void generateDebugReport(Map<String, List<ExecutionResult>> allResults,
                                         List<String> successful, List<String> failed,
                                         int timeout, int totalAlgorithms, String outputPath) throws Exception {
        
        System.out.println("[HTMLReportGenerator] Starting debug report generation");
        System.out.println("[HTMLReportGenerator] Output path: " + outputPath);
        System.out.println("[HTMLReportGenerator] Total algorithms: " + totalAlgorithms);
        System.out.println("[HTMLReportGenerator] Successful: " + successful.size());
        System.out.println("[HTMLReportGenerator] Failed: " + failed.size());
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Debug Analysis Report - All Algorithms</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        .header { text-align: center; margin-bottom: 30px; }\n");
        html.append("        .section { margin-bottom: 30px; }\n");
        html.append("        .stats-grid { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 15px; margin: 20px 0; }\n");
        html.append("        .stat-box { border: 2px solid #ddd; padding: 15px; text-align: center; border-radius: 5px; }\n");
        html.append("        .stat-box.success { border-color: #4CAF50; background-color: #f0fff0; }\n");
        html.append("        .stat-box.failure { border-color: #f44336; background-color: #fff0f0; }\n");
        html.append("        .stat-box.timeout { border-color: #ff9800; background-color: #fff8f0; }\n");
        html.append("        .stat-box.total { border-color: #2196F3; background-color: #f0f8ff; }\n");
        html.append("        .stat-number { font-size: 2em; font-weight: bold; }\n");
        html.append("        .stat-label { font-size: 0.9em; color: #666; }\n");
        html.append("        .algorithm-list { max-height: 300px; overflow-y: auto; }\n");
        html.append("        .data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }\n");
        html.append("        .data-table th, .data-table td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 0.85em; }\n");
        html.append("        .data-table th { background-color: #f2f2f2; }\n");
        html.append("        .success { color: #4CAF50; }\n");
        html.append("        .failure { color: #f44336; }\n");
        html.append("        .timeout { color: #ff9800; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>🔍 Debug Analysis Report</h1>\n");
        html.append("        <h2>Comprehensive Analysis of All Workshop Algorithms</h2>\n");
        html.append("        <p>Generated on: ").append(java.time.LocalDateTime.now().toString()).append("</p>\n");
        html.append("    </div>\n");
        
        // Statistics section
        html.append("    <div class=\"section\">\n");
        html.append("        <h2>📊 Analysis Statistics</h2>\n");
        html.append("        <div class=\"stats-grid\">\n");
        html.append("            <div class=\"stat-box total\">\n");
        html.append("                <div class=\"stat-number\">").append(totalAlgorithms).append("</div>\n");
        html.append("                <div class=\"stat-label\">Total Algorithms</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat-box success\">\n");
        html.append("                <div class=\"stat-number\">").append(successful.size()).append("</div>\n");
        html.append("                <div class=\"stat-label\">Successful</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat-box failure\">\n");
        html.append("                <div class=\"stat-number\">").append(failed.size()).append("</div>\n");
        html.append("                <div class=\"stat-label\">Failed</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat-box timeout\">\n");
        html.append("                <div class=\"stat-number\">").append(timeout).append("s</div>\n");
        html.append("                <div class=\"stat-label\">Timeout Used</div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        
        // Successful algorithms section with charts
        if (!successful.isEmpty()) {
            html.append("    <div class=\"section\">\n");
            html.append("        <h2>✅ Successfully Analyzed Algorithms (").append(successful.size()).append(")</h2>\n");
            
            // Create charts directory
            String baseDir = new File(outputPath).getParent();
            String chartsDir = baseDir + File.separator + "debug_charts";
            new File(chartsDir).mkdirs();
            
            // Generate chart for each successful algorithm
            int chartIndex = 0;
            for (String algorithmName : successful) {
                List<ExecutionResult> results = allResults.get(algorithmName);
                if (results != null && !results.isEmpty()) {
                    long successfulPoints = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
                    double avgTime = results.stream()
                        .filter(ExecutionResult::isSuccess)
                        .mapToDouble(ExecutionResult::getExecutionTime)
                        .average().orElse(0);
                    double minTime = results.stream()
                        .filter(ExecutionResult::isSuccess)
                        .mapToDouble(ExecutionResult::getExecutionTime)
                        .min().orElse(0);
                    double maxTime = results.stream()
                        .filter(ExecutionResult::isSuccess)
                        .mapToDouble(ExecutionResult::getExecutionTime)
                        .max().orElse(0);
                    
                    // Generate chart for this algorithm
                    try {
                        String chartFileName = "debug_chart_" + chartIndex + ".png";
                        String chartPath = chartsDir + File.separator + chartFileName;
                        JFreeChart chart = createDebugChart(algorithmName, results);
                        saveChartAsImage(chart, chartPath, CHART_WIDTH, CHART_HEIGHT);
                        
                        // Add algorithm section with chart and data
                        html.append("        <div class=\"algorithm-section\" style=\"margin-bottom: 30px; border: 1px solid #ddd; padding: 15px; border-radius: 5px;\">\n");
                        html.append("            <h3>").append(algorithmName).append("</h3>\n");
                        html.append("            <div style=\"display: flex; align-items: flex-start; gap: 20px;\">\n");
                        html.append("                <div style=\"flex: 1;\">\n");
                        html.append("                    <img src=\"debug_charts/").append(chartFileName).append("\" alt=\"").append(algorithmName).append(" Chart\" style=\"max-width: 100%; height: auto; border: 1px solid #ccc;\">\n");
                        html.append("                </div>\n");
                        html.append("                <div style=\"flex: 0 0 300px;\">\n");
                        html.append("                    <table class=\"data-table\" style=\"font-size: 0.9em;\">\n");
                        html.append("                        <tr><th>Metric</th><th>Value</th></tr>\n");
                        html.append("                        <tr><td>Data Points</td><td>").append(successfulPoints).append("</td></tr>\n");
                        html.append("                        <tr><td>Avg Time (μs)</td><td>").append(String.format("%.1f", avgTime)).append("</td></tr>\n");
                        html.append("                        <tr><td>Min Time (μs)</td><td>").append(String.format("%.1f", minTime)).append("</td></tr>\n");
                        html.append("                        <tr><td>Max Time (μs)</td><td>").append(String.format("%.1f", maxTime)).append("</td></tr>\n");
                        html.append("                    </table>\n");
                        html.append("                </div>\n");
                        html.append("            </div>\n");
                        html.append("        </div>\n");
                        
                        chartIndex++;
                    } catch (Exception e) {
                        System.out.println("[HTMLReportGenerator] Failed to generate chart for " + algorithmName + ": " + e.getMessage());
                        // Fall back to table-only display
                        html.append("        <div class=\"algorithm-section\">\n");
                        html.append("            <h3>").append(algorithmName).append(" (Chart generation failed)</h3>\n");
                        html.append("            <p>Data Points: ").append(successfulPoints).append(", Avg: ").append(String.format("%.1f", avgTime)).append("μs</p>\n");
                        html.append("        </div>\n");
                    }
                }
            }
            html.append("    </div>\n");
        }
        
        // Failed algorithms section
        if (!failed.isEmpty()) {
            html.append("    <div class=\"section\">\n");
            html.append("        <h2>❌ Failed Algorithms (").append(failed.size()).append(")</h2>\n");
            html.append("        <div class=\"algorithm-list\">\n");
            html.append("            <ul>\n");
            for (String failedAlgorithm : failed) {
                html.append("                <li class=\"failure\">").append(failedAlgorithm).append("</li>\n");
            }
            html.append("            </ul>\n");
            html.append("        </div>\n");
            html.append("    </div>\n");
        }
        
        // All results detailed table
        html.append("    <div class=\"section\">\n");
        html.append("        <h2>📋 Detailed Results</h2>\n");
        html.append("        <table class=\"data-table\">\n");
        html.append("            <tr><th>Algorithm</th><th>Status</th><th>Data Points</th><th>Details</th></tr>\n");
        
        // Add successful algorithms
        for (String algorithmName : successful) {
            List<ExecutionResult> results = allResults.get(algorithmName);
            if (results != null) {
                long successfulPoints = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
                long timeoutPoints = results.stream().mapToLong(r -> r.isTimedOut() ? 1 : 0).sum();
                String details = successfulPoints + " successful";
                if (timeoutPoints > 0) {
                    details += ", " + timeoutPoints + " timeout";
                }
                
                html.append("            <tr>\n");
                html.append("                <td>").append(algorithmName).append("</td>\n");
                html.append("                <td class=\"success\">SUCCESS</td>\n");
                html.append("                <td>").append(results.size()).append("</td>\n");
                html.append("                <td>").append(details).append("</td>\n");
                html.append("            </tr>\n");
            }
        }
        
        // Add failed algorithms
        for (String failedAlgorithm : failed) {
            String[] parts = failedAlgorithm.split(": ", 2);
            String algorithmName = parts[0];
            String errorMessage = parts.length > 1 ? parts[1] : "Unknown error";
            
            html.append("            <tr>\n");
            html.append("                <td>").append(algorithmName).append("</td>\n");
            html.append("                <td class=\"failure\">FAILED</td>\n");
            html.append("                <td>0</td>\n");
            html.append("                <td>").append(errorMessage).append("</td>\n");
            html.append("            </tr>\n");
        }
        
        html.append("        </table>\n");
        html.append("    </div>\n");
        
        // Footer
        html.append("    <div class=\"section\">\n");
        html.append("        <p><em>This debug report tests all algorithms assuming implementations exist.</em></p>\n");
        html.append("        <p><em>Timeout: ").append(timeout).append(" seconds per algorithm</em></p>\n");
        html.append("    </div>\n");
        
        html.append("</body>\n");
        html.append("</html>\n");
        
        // Write HTML file
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(html.toString().getBytes("UTF-8"));
        }
        
        System.out.println("[HTMLReportGenerator] Debug report generated successfully: " + outputPath);
    }
    
    /**
     * Creates a JFreeChart for debug analysis that matches Eclipse view styling
     */
    private static JFreeChart createDebugChart(String algorithmName, List<ExecutionResult> results) {
        // Determine if this is multi-line analysis based on scenario names
        java.util.Set<String> uniqueScenarios = new java.util.HashSet<>();
        for (ExecutionResult result : results) {
            String scenarioName = result.getScenarioName();
            if (scenarioName != null && !scenarioName.isEmpty()) {
                uniqueScenarios.add(scenarioName);
            }
        }
        boolean isMultiLine = uniqueScenarios.size() > 1;
        
        // Create dataset following Eclipse view pattern
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        if (isMultiLine) {
            // Multi-line analysis: group by scenario name (matches RuntimeAnalysisView.updateMultiLineSeries)
            java.util.Map<String, List<ExecutionResult>> groupedResults = new java.util.HashMap<>();
            
            for (ExecutionResult result : results) {
                String scenarioName = result.getScenarioName();
                if (scenarioName == null || scenarioName.isEmpty()) {
                    scenarioName = algorithmName;
                }
                groupedResults.computeIfAbsent(scenarioName, k -> new ArrayList<>()).add(result);
            }
            
            // Create separate series for each scenario
            for (java.util.Map.Entry<String, List<ExecutionResult>> entry : groupedResults.entrySet()) {
                String scenarioName = entry.getKey();
                List<ExecutionResult> scenarioResults = entry.getValue();
                
                XYSeries scenarioSeries = new XYSeries(scenarioName);
                
                for (ExecutionResult result : scenarioResults) {
                    if (result.isSuccess() || result.isTimedOut()) {
                        double displayTime = result.getExecutionTime();
                        if (displayTime <= 0) {
                            displayTime = 1; // Match Eclipse view minimum (1 microsecond)
                        }
                        double xValue = result.getInputSize(); // Use input size as X for multi-line
                        scenarioSeries.add(xValue, displayTime);
                    }
                }
                
                if (scenarioSeries.getItemCount() > 0) {
                    dataset.addSeries(scenarioSeries);
                }
            }
        } else {
            // Single series analysis (matches RuntimeAnalysisView.updateSingleSeries)
            XYSeries algorithmSeries = new XYSeries(algorithmName);
            
            for (ExecutionResult result : results) {
                if (result.isSuccess() || result.isTimedOut()) {
                    double displayTime = result.getExecutionTime();
                    if (displayTime <= 0) {
                        displayTime = 1; // Match Eclipse view minimum
                    }
                    algorithmSeries.add(result.getXValue(), displayTime); // Use getXValue() like Eclipse view
                }
            }
            
            if (algorithmSeries.getItemCount() > 0) {
                dataset.addSeries(algorithmSeries);
            }
        }
        
        // Determine X-axis label (match Eclipse view logic from updateChartAxisLabels)
        String xAxisLabel = "Input Size"; // Default
        if (!results.isEmpty()) {
            // Check the first result for analysis mode hints
            ExecutionResult firstResult = results.get(0);
            double minX = results.stream().mapToDouble(r -> isMultiLine ? r.getInputSize() : r.getXValue()).min().orElse(0);
            double maxX = results.stream().mapToDouble(r -> isMultiLine ? r.getInputSize() : r.getXValue()).max().orElse(100);
            
            if (maxX <= 1.0 && !isMultiLine) {
                xAxisLabel = "Load Factor";
            } else if (maxX < 10 && !isMultiLine) {
                xAxisLabel = "Position Index";
            } else if (isMultiLine) {
                xAxisLabel = "Input Size"; // Multi-line always uses input size
            }
        }
        
        // Create chart (match Eclipse view createChart method)
        JFreeChart chart = ChartFactory.createXYLineChart(
            algorithmName + " - Runtime Analysis", // Match Eclipse view title format
            xAxisLabel,
            "Time (μs)",
            dataset,
            PlotOrientation.VERTICAL,
            true, // Show legend (always true to match Eclipse view)
            true, // Show tooltips
            false // No URLs
        );
        
        // Customize the plot (match Eclipse view styling exactly)
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Set logarithmic axes (exact match to Eclipse view)
        LogAxis xAxis = new LogAxis(xAxisLabel);
        xAxis.setBase(10);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setDomainAxis(xAxis);
        
        LogAxis yAxis = new LogAxis("Time (μs)");
        yAxis.setBase(10);
        yAxis.setAutoRange(true);
        plot.setRangeAxis(yAxis);
        
        // Create timeout-aware renderer (match Eclipse view TimeoutAwareRenderer behavior)
        DebugTimeoutAwareRenderer renderer = new DebugTimeoutAwareRenderer();
        renderer.setTimeoutData(results);
        
        // Style series to match Eclipse view
        Color[] colors = {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA};
        
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesStroke(i, new java.awt.BasicStroke(2.0f)); // Match Eclipse view stroke
            
            // Use same color scheme as Eclipse view
            if (i < colors.length) {
                renderer.setSeriesPaint(i, colors[i]);
            } else {
                renderer.setSeriesPaint(i, Color.BLACK); // Fallback
            }
        }
        
        plot.setRenderer(renderer);
        
        return chart;
    }
    
    /**
     * Timeout-aware renderer for debug charts that matches Eclipse view behavior
     */
    private static class DebugTimeoutAwareRenderer extends XYLineAndShapeRenderer {
        
        private List<ExecutionResult> timeoutData;
        
        public DebugTimeoutAwareRenderer() {
            super(true, true); // Enable lines and shapes by default for all series
        }
        
        public void setTimeoutData(List<ExecutionResult> results) {
            this.timeoutData = results;
        }
        
        @Override
        public java.awt.Paint getItemPaint(int series, int item) {
            // Check if this point timed out (match Eclipse view TimeoutAwareRenderer)
            if (timeoutData != null && item < timeoutData.size()) {
                // Find the corresponding result for this data point
                for (ExecutionResult result : timeoutData) {
                    if (result.isTimedOut()) {
                        // For timeout points, use red color
                        return Color.RED;
                    }
                }
            }
            
            // Default coloring for successful points
            return super.getItemPaint(series, item);
        }
        
        @Override
        public java.awt.Paint getItemFillPaint(int series, int item) {
            // Check if this point timed out (match Eclipse view TimeoutAwareRenderer)
            if (timeoutData != null && item < timeoutData.size()) {
                for (ExecutionResult result : timeoutData) {
                    if (result.isTimedOut()) {
                        return Color.RED;
                    }
                }
            }
            
            return super.getItemFillPaint(series, item);
        }
    }
}