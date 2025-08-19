package edu.runtimeanalysis.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.runtimeanalysis.Activator;
import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AnalysisMode;
import edu.runtimeanalysis.models.BigOhFunction;
import edu.runtimeanalysis.models.ReferenceLineManager;

/**
 * Main view for displaying runtime analysis results
 */
public class RuntimeAnalysisView extends ViewPart {
    
    public static final String ID = "edu.runtimeanalysis.views.RuntimeAnalysisView";
    
    private Label algorithmLabel;
    private Label statusLabel;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Composite chartComposite;
    private JFreeChart chart;
    private XYSeriesCollection dataset;
    private Button clearButton;
    private Button runAnotherButton;
    private volatile boolean analysisRunning = false;
    private ReferenceLineManager referenceLineManager;
    private Group referenceLineGroup;
    private List<Button> referenceLineButtons;
    private Button logScaleToggle;
    private boolean useLogScale = true; // Default to log scale
    private ChartPanel chartPanel;
    private Frame chartFrame;
    private List<ExecutionResult> currentResults;
    private String currentAlgorithmName;
    private AlgorithmConfig currentAlgorithmConfig;
    private TimeoutAwareRenderer timeoutRenderer;
    private boolean wasJustOpened = false;
    
    @Override
    public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout(1, false);
        parent.setLayout(layout);
        
        // Create header composite
        Composite headerComposite = new Composite(parent, SWT.NONE);
        headerComposite.setLayout(new GridLayout(2, false));
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Algorithm label
        Label algLabel = new Label(headerComposite, SWT.NONE);
        algLabel.setText("Algorithm:");
        algorithmLabel = new Label(headerComposite, SWT.NONE);
        algorithmLabel.setText("No analysis run yet");
        algorithmLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Status label
        Label statLabel = new Label(headerComposite, SWT.NONE);
        statLabel.setText("Status:");
        statusLabel = new Label(headerComposite, SWT.NONE);
        statusLabel.setText("Ready");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Progress bar and label
        Composite progressComposite = new Composite(parent, SWT.NONE);
        progressComposite.setLayout(new GridLayout(2, false));
        progressComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        progressBar = new ProgressBar(progressComposite, SWT.HORIZONTAL);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setSelection(0);
        progressBar.setVisible(false); // Initially hidden
        
        progressLabel = new Label(progressComposite, SWT.NONE);
        progressLabel.setText("");
        progressLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        progressLabel.setVisible(false); // Initially hidden
        
        // Create chart composite
        chartComposite = new Composite(parent, SWT.EMBEDDED | SWT.BORDER);
        chartComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Initialize the chart
        createChart();
        
        // Create reference line selection group
        createReferenceLineControls(parent);
        
        // Create button composite
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, false));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        clearButton = new Button(buttonComposite, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setEnabled(false);
        clearButton.addListener(SWT.Selection, e -> clearChart());
        
        runAnotherButton = new Button(buttonComposite, SWT.PUSH);
        runAnotherButton.setText("Run Another Analysis");
        runAnotherButton.addListener(SWT.Selection, e -> runAnotherAnalysis());
        
        logScaleToggle = new Button(buttonComposite, SWT.CHECK);
        logScaleToggle.setText("Log Scale");
        logScaleToggle.setSelection(useLogScale);
        logScaleToggle.setToolTipText("Toggle between logarithmic and linear axes. Changes will clear current data.");
        logScaleToggle.addListener(SWT.Selection, e -> toggleLogScale());
    }
    
    private void createReferenceLineControls(Composite parent) {
        referenceLineGroup = new Group(parent, SWT.NONE);
        referenceLineGroup.setText("Reference Lines (Big-O)");
        referenceLineGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        rowLayout.wrap = true;
        rowLayout.spacing = 10;
        referenceLineGroup.setLayout(rowLayout);
        
        referenceLineButtons = new ArrayList<>();
        
        // Create checkboxes for each Big-O function
        for (BigOhFunction function : BigOhFunction.values()) {
            Button checkbox = new Button(referenceLineGroup, SWT.CHECK);
            checkbox.setText(function.getNotation());
            checkbox.setToolTipText(function.getDescription());
            checkbox.addListener(SWT.Selection, e -> {
                if (checkbox.getSelection()) {
                    referenceLineManager.enableFunction(function);
                } else {
                    referenceLineManager.disableFunction(function);
                }
            });
            referenceLineButtons.add(checkbox);
        }
    }
    
    private void createChart() {
        createChart("Input Size"); // Default x-axis label
    }
    
    private void createChart(String xAxisLabel) {
        // Create dataset
        dataset = new XYSeriesCollection();
        
        // Generate dynamic title based on current settings
        String chartTitle = generateChartTitle();
        
        // Create chart with logarithmic axes
        chart = ChartFactory.createXYLineChart(
            chartTitle,
            xAxisLabel,
            "Time (ms)",
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
        
        // Set axes based on scale preference
        configureAxes(plot, xAxisLabel);
        
        // Customize renderer with timeout-aware coloring
        timeoutRenderer = new TimeoutAwareRenderer();
        timeoutRenderer.setSeriesLinesVisible(0, true);
        timeoutRenderer.setSeriesShapesVisible(0, true);
        timeoutRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(timeoutRenderer);
        
        // Embed the chart
        chartFrame = SWT_AWT.new_Frame(chartComposite);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setMouseWheelEnabled(true);
        chartFrame.add(chartPanel);
        
        // Initialize reference line manager
        referenceLineManager = new ReferenceLineManager(dataset, plot);
    }
    
    public void updateResults(String algorithmName, List<ExecutionResult> results) {
        updateResults(algorithmName, results, null);
    }
    
    public void updateResults(String algorithmName, List<ExecutionResult> results, AlgorithmConfig algorithmConfig) {
        System.out.println("[RuntimeAnalysisView] Updating results for: " + algorithmName);
        System.out.println("[RuntimeAnalysisView] Total results received: " + results.size());
        
        // Store current results and algorithm config for progressive updates
        this.currentResults = new ArrayList<>(results);
        this.currentAlgorithmName = algorithmName;
        this.currentAlgorithmConfig = algorithmConfig;
        
        // Update chart with appropriate axis labels if algorithm config is provided
        if (algorithmConfig != null && chart != null) {
            updateChartAxisLabels(algorithmConfig);
        }
        
        // Update chart title to reflect current algorithm and settings
        updateChartTitle();
        
        // Check if this is a "Running..." status update
        if (algorithmName.contains("(Running...)")) {
            setAnalysisRunning(true);
            // For running status, update progressively
            updateChart(true);
        } else {
            setAnalysisRunning(false);
            // For completed analysis, do full update
            updateChart(false);
        }
    }
    
    private void updateChartAxisLabels(AlgorithmConfig algorithmConfig) {
        if (chart != null) {
            XYPlot plot = chart.getXYPlot();
            String xAxisLabel = algorithmConfig.getAnalysisMode().getXAxisLabel();
            
            // Update domain (X) axis label based on current scale type
            org.jfree.chart.axis.ValueAxis currentXAxis = plot.getDomainAxis();
            
            if (currentXAxis instanceof LogAxis) {
                LogAxis newXAxis = new LogAxis(xAxisLabel);
                newXAxis.setBase(10);
                newXAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                newXAxis.setAutoRange(true);
                plot.setDomainAxis(newXAxis);
            } else if (currentXAxis instanceof NumberAxis) {
                NumberAxis newXAxis = new NumberAxis(xAxisLabel);
                newXAxis.setAutoRange(true);
                newXAxis.setAutoRangeIncludesZero(false);
                plot.setDomainAxis(newXAxis);
            }
            
            System.out.println("[RuntimeAnalysisView] Updated X-axis label to: " + xAxisLabel);
        }
    }
    
    private void updateChart(boolean isProgressive) {
        Display.getDefault().asyncExec(() -> {
            if (currentAlgorithmName != null) {
                algorithmLabel.setText(currentAlgorithmName);
            }
            
            if (!isProgressive) {
                // Clear existing algorithm data but preserve reference lines
                removeAlgorithmSeries();
            }
            
            // Handle multi-line scenarios (position-based, collision-based) vs single series
            int successCount = 0;
            List<Double> actualTimes = new ArrayList<>();
            List<Integer> inputSizes = new ArrayList<>();
            
            if (currentAlgorithmConfig != null && isMultiLineAnalysis(currentAlgorithmConfig)) {
                // Group results by scenario and create separate series
                successCount = updateMultiLineSeries(isProgressive, actualTimes, inputSizes);
            } else {
                // Create or update single algorithm series (traditional approach)
                XYSeries algorithmSeries = getOrCreateAlgorithmSeries();
                successCount = updateSingleSeries(algorithmSeries, isProgressive, actualTimes, inputSizes);
            }
            
            // Update timeout renderer with current results
            if (timeoutRenderer != null) {
                timeoutRenderer.setTimeoutData(currentResults);
            }
            
            // Update reference line scaling and range
            if (referenceLineManager != null && !actualTimes.isEmpty()) {
                int minSize = inputSizes.stream().mapToInt(Integer::intValue).min().orElse(1);
                int maxSize = inputSizes.stream().mapToInt(Integer::intValue).max().orElse(10000);
                referenceLineManager.setInputSizeRange(minSize, maxSize * 2); // Extend range slightly
                referenceLineManager.updateScaling(actualTimes, inputSizes);
            }
            
            // Update status
            if (isProgressive && analysisRunning) {
                statusLabel.setText(String.format("Analysis Running... (%d data points so far)", successCount));
            } else {
                statusLabel.setText(String.format("Analysis Complete (%d data points collected)", successCount));
            }
            
            // Enable buttons only when analysis is complete
            if (!analysisRunning) {
                clearButton.setEnabled(true);
            }
            
            // Optimize chart repainting
            if (chart != null && chartPanel != null) {
                // Always repaint for progressive updates to show each new data point
                // Only optimize repainting for very frequent updates (more than 10 data points)
                if (!isProgressive || successCount <= 10 || successCount % 2 == 0) {
                    chart.fireChartChanged();
                    chartPanel.repaint();
                    System.out.println("[RuntimeAnalysisView] Updated chart - " + successCount + " data points");
                }
            }
            
            // Apply smart buffering after data is updated
            if (!isProgressive && currentAlgorithmConfig != null) {
                applyAxisBuffering(currentAlgorithmConfig);
            }
            
            // Re-apply reference line styles to prevent conflicts with student data
            if (referenceLineManager != null) {
                referenceLineManager.reapplyReferenceLineStyles();
            }
            
            // Log dataset info
            System.out.println("[RuntimeAnalysisView] Final dataset series count: " + dataset.getSeriesCount());
            System.out.println("[RuntimeAnalysisView] Total successful data points: " + successCount);
            
            // Log the view update
            Activator.getDefault().getUsageLogger().logAction("VIEW_UPDATED", 
                "algorithm=" + currentAlgorithmName + ",points=" + successCount);
        });
    }
    
    private boolean isMultiLineAnalysis(AlgorithmConfig config) {
        AnalysisMode mode = config.getAnalysisMode();
        return mode == AnalysisMode.POSITION_BASED || mode == AnalysisMode.INPUT_SIZE_WITH_POSITIONS || mode == AnalysisMode.INPUT_SIZE_WITH_COLLISIONS;
    }
    
    private int updateMultiLineSeries(boolean isProgressive, List<Double> actualTimes, List<Integer> inputSizes) {
        // Group results by scenario name
        java.util.Map<String, List<ExecutionResult>> groupedResults = new java.util.HashMap<>();
        
        for (ExecutionResult result : currentResults) {
            String scenarioName = extractScenarioName(result);
            groupedResults.computeIfAbsent(scenarioName, k -> new ArrayList<>()).add(result);
        }
        
        int totalSuccessCount = 0;
        
        // Create separate series for each scenario
        for (java.util.Map.Entry<String, List<ExecutionResult>> entry : groupedResults.entrySet()) {
            String scenarioName = entry.getKey();
            List<ExecutionResult> scenarioResults = entry.getValue();
            
            XYSeries scenarioSeries = getOrCreateScenarioSeries(scenarioName);
            
            for (ExecutionResult result : scenarioResults) {
                if (result.isSuccess() || result.isTimedOut()) {
                    double displayTime = result.getExecutionTime() / 1000.0; // Convert from microseconds to milliseconds
                    double xValue = result.getInputSize(); // Use input size as X value for multi-line analysis
                    
                    // For progressive updates, only add new points
                    if (isProgressive) {
                        boolean pointExists = false;
                        for (int i = 0; i < scenarioSeries.getItemCount(); i++) {
                            if (Math.abs(scenarioSeries.getX(i).doubleValue() - xValue) < 0.001) {
                                pointExists = true;
                                break;
                            }
                        }
                        if (!pointExists) {
                            scenarioSeries.add(xValue, displayTime);
                        }
                    } else {
                        scenarioSeries.add(xValue, displayTime);
                    }
                    
                    actualTimes.add(displayTime);
                    inputSizes.add((int) xValue);
                    totalSuccessCount++;
                }
            }
        }
        
        return totalSuccessCount;
    }
    
    private int updateSingleSeries(XYSeries algorithmSeries, boolean isProgressive, List<Double> actualTimes, List<Integer> inputSizes) {
        int successCount = 0;
        
        for (ExecutionResult result : currentResults) {
            System.out.println("[RuntimeAnalysisView] Processing result - Size: " + result.getInputSize() + 
                             ", Time: " + result.getExecutionTime() + ", Success: " + result.isSuccess() + 
                             ", TimedOut: " + result.isTimedOut());
            
            // Include both successful results and timed-out results
            if (result.isSuccess() || result.isTimedOut()) {
                double displayTime = result.getExecutionTime() / 1000.0; // Convert from microseconds to milliseconds
                
                // For progressive updates, only add new points
                if (isProgressive) {
                    // Check if point already exists
                    boolean pointExists = false;
                    for (int i = 0; i < algorithmSeries.getItemCount(); i++) {
                        if (algorithmSeries.getX(i).intValue() == result.getInputSize()) {
                            pointExists = true;
                            break;
                        }
                    }
                    if (!pointExists) {
                        algorithmSeries.add(result.getInputSize(), displayTime);
                    }
                } else {
                    algorithmSeries.add(result.getInputSize(), displayTime);
                }
                
                actualTimes.add(displayTime);
                inputSizes.add(result.getInputSize());
                successCount++;
                
                if (result.isTimedOut()) {
                    System.out.println("[RuntimeAnalysisView] Added TIMEOUT data point: (" + result.getInputSize() + 
                                     ", " + String.format("%.3f", displayTime) + "ms - TIMEOUT)");
                } else if (result.getExecutionTime() == 1) {
                    System.out.println("[RuntimeAnalysisView] Added data point: (" + result.getInputSize() + ", 0.001ms)");
                } else {
                    System.out.println("[RuntimeAnalysisView] Added data point: (" + result.getInputSize() + ", " + String.format("%.3f", displayTime) + "ms)");
                }
            }
        }
        
        return successCount;
    }
    
    private String extractScenarioName(ExecutionResult result) {
        // Use the scenario name from the result if available
        String scenarioName = result.getScenarioName();
        if (scenarioName != null && !scenarioName.isEmpty()) {
            return scenarioName;
        }
        
        // Fallback to algorithm name for single-series analysis
        return currentAlgorithmName != null ? currentAlgorithmName : "Algorithm";
    }
    
    private XYSeries getOrCreateScenarioSeries(String scenarioName) {
        // Look for existing scenario series
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            if (seriesKey.equals(scenarioName)) {
                return dataset.getSeries(i);
            }
        }
        
        // Create new scenario series
        XYSeries scenarioSeries = new XYSeries(scenarioName);
        dataset.addSeries(scenarioSeries);
        
        // Style the scenario series with different colors
        int seriesIndex = dataset.getSeriesCount() - 1;
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesLinesVisible(seriesIndex, true);
        renderer.setSeriesShapesVisible(seriesIndex, true);
        renderer.setSeriesStroke(seriesIndex, new BasicStroke(2.0f));
        
        // Use different colors for different series
        Color[] colors = {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA};
        renderer.setSeriesPaint(seriesIndex, colors[seriesIndex % colors.length]);
        
        System.out.println("[RuntimeAnalysisView] Configured scenario series " + seriesIndex + " (" + scenarioName + ") with lines visible: true");
        
        return scenarioSeries;
    }
    
    private void removeAlgorithmSeries() {
        // Remove non-reference series (algorithm data)
        for (int i = dataset.getSeriesCount() - 1; i >= 0; i--) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            boolean isReference = false;
            for (BigOhFunction function : BigOhFunction.values()) {
                if (function.getNotation().equals(seriesKey)) {
                    isReference = true;
                    break;
                }
            }
            if (!isReference) {
                dataset.removeSeries(i);
            }
        }
    }
    
    private XYSeries getOrCreateAlgorithmSeries() {
        // Look for existing algorithm series
        String algorithmName = currentAlgorithmName != null ? currentAlgorithmName : "Algorithm";
        
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            // If it's not a reference line, it's our algorithm series
            boolean isReference = false;
            for (BigOhFunction function : BigOhFunction.values()) {
                if (function.getNotation().equals(seriesKey)) {
                    isReference = true;
                    break;
                }
            }
            if (!isReference) {
                return dataset.getSeries(i);
            }
        }
        
        // Create new algorithm series
        XYSeries algorithmSeries = new XYSeries(algorithmName);
        dataset.addSeries(algorithmSeries);
        
        // Style the algorithm series
        int seriesIndex = dataset.getSeriesCount() - 1;
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        
        // Ensure lines and shapes are visible for structure size analyses
        renderer.setSeriesLinesVisible(seriesIndex, true);
        renderer.setSeriesShapesVisible(seriesIndex, true);
        renderer.setSeriesStroke(seriesIndex, new BasicStroke(2.0f));
        renderer.setSeriesPaint(seriesIndex, Color.BLACK);
        
        System.out.println("[RuntimeAnalysisView] Configured series " + seriesIndex + " with lines visible: true");
        
        return algorithmSeries;
    }
    
    /**
     * Configure chart axes based on current scale preference
     */
    private void configureAxes(XYPlot plot, String xAxisLabel) {
        if (useLogScale) {
            // Set logarithmic axes
            LogAxis xAxis = new LogAxis(xAxisLabel);
            xAxis.setBase(10);
            xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            plot.setDomainAxis(xAxis);
            
            LogAxis yAxis = new LogAxis("Time (ms)");
            yAxis.setBase(10);
            yAxis.setAutoRange(true);
            plot.setRangeAxis(yAxis);
        } else {
            // Set linear axes
            NumberAxis xAxis = new NumberAxis(xAxisLabel);
            xAxis.setAutoRange(true);
            xAxis.setAutoRangeIncludesZero(false);
            plot.setDomainAxis(xAxis);
            
            NumberAxis yAxis = new NumberAxis("Time (ms)");
            yAxis.setAutoRange(true);
            yAxis.setAutoRangeIncludesZero(false);
            plot.setRangeAxis(yAxis);
        }
    }
    
    /**
     * Toggle between logarithmic and linear scale
     */
    private void toggleLogScale() {
        boolean newScaleSetting = logScaleToggle.getSelection();
        
        // If there's existing data, warn the user
        if (dataset != null && dataset.getSeriesCount() > 0) {
            boolean proceed = MessageDialog.openQuestion(
                getSite().getShell(),
                "Change Scale Type",
                "Changing the scale type will clear the current chart data.\n\n" +
                "Do you want to continue?"
            );
            
            if (!proceed) {
                // Revert the checkbox state
                logScaleToggle.setSelection(useLogScale);
                return;
            }
        }
        
        // Update scale setting
        useLogScale = newScaleSetting;
        
        // Clear current data and recreate chart
        clearChart();
        recreateChart();
        
        System.out.println("[RuntimeAnalysisView] Switched to " + (useLogScale ? "logarithmic" : "linear") + " scale");
    }
    
    /**
     * Recreate the chart with current settings
     */
    private void recreateChart() {
        if (chart != null) {
            String currentXAxisLabel = "Input Size";
            if (currentAlgorithmConfig != null) {
                currentXAxisLabel = currentAlgorithmConfig.getAnalysisMode().getXAxisLabel();
            }
            
            // Recreate chart
            createChart(currentXAxisLabel);
            
            // Recreate reference line manager
            XYPlot plot = chart.getXYPlot();
            referenceLineManager = new ReferenceLineManager(dataset, plot);
            
            // Reset reference line checkboxes (they'll need to be re-selected)
            for (Button checkbox : referenceLineButtons) {
                checkbox.setSelection(false);
            }
            
            // Recreate chart panel
            if (chartPanel != null && chartFrame != null) {
                chartFrame.remove(chartPanel);
                chartPanel = new ChartPanel(chart);
                chartPanel.setPreferredSize(new Dimension(800, 600));
                chartFrame.add(chartPanel);
                chartFrame.validate();
            }
        }
    }
    
    /**
     * Generate dynamic chart title based on current settings and algorithm
     */
    private String generateChartTitle() {
        StringBuilder title = new StringBuilder("Runtime Analysis");
        
        // Add scale mode information
        String scaleMode = useLogScale ? "Log-Log Plot" : "Linear Plot";
        title.append(" - ").append(scaleMode);
        
        // Add algorithm name if available
        if (currentAlgorithmName != null && !currentAlgorithmName.isEmpty()) {
            // Clean up algorithm name (remove "Running..." suffix if present)
            String cleanAlgorithmName = currentAlgorithmName.replace(" (Running...)", "").trim();
            if (!cleanAlgorithmName.isEmpty()) {
                title.append(" - ").append(cleanAlgorithmName);
            }
        }
        
        // Analysis mode information removed to avoid revealing learning objectives
        
        return title.toString();
    }
    
    /**
     * Get user-friendly description of analysis mode
     */
    private String getAnalysisModeDescription(AnalysisMode mode) {
        switch (mode) {
            case TRADITIONAL_SCALING:
                return "Scaling Analysis";
            case POSITION_BASED:
            case INPUT_SIZE_WITH_POSITIONS:
                return "Position-Based Analysis";
            case INPUT_SIZE_WITH_COLLISIONS:
                return "Collision Analysis";
            case LOAD_FACTOR:
                return "Load Factor Analysis";
            case STRUCTURE_SIZE:
                return "Structure Size Analysis";
            case CONSTANT_TIME:
                return "Constant Time Analysis";
            default:
                return "";
        }
    }
    
    /**
     * Update chart title when settings or algorithm change
     */
    private void updateChartTitle() {
        if (chart != null) {
            String newTitle = generateChartTitle();
            chart.setTitle(newTitle);
            System.out.println("[RuntimeAnalysisView] Updated chart title to: " + newTitle);
        }
    }
    
    private void clearChart() {
        dataset.removeAllSeries();
        algorithmLabel.setText("No analysis run yet");
        statusLabel.setText("Ready");
        clearButton.setEnabled(false);
        
        // Hide progress bar
        hideProgress();
        
        // Clear reference lines
        if (referenceLineManager != null) {
            referenceLineManager.clearAll();
        }
        
        // Uncheck all reference line buttons
        if (referenceLineButtons != null) {
            for (Button button : referenceLineButtons) {
                if (!button.isDisposed()) {
                    button.setSelection(false);
                }
            }
        }
        
        currentResults = null;
        currentAlgorithmName = null;
        
        Activator.getDefault().getUsageLogger().logAction("CLEAR_CLICKED", null);
    }
    
    private void runAnotherAnalysis() {
        // Trigger another analysis through the handler
        try {
            getSite().getWorkbenchWindow().getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .showView(ID);
            
            // Execute the run analysis command
            getSite().getService(org.eclipse.ui.commands.ICommandService.class)
                .getCommand("edu.runtimeanalysis.commands.runAnalysis")
                .executeWithChecks(new org.eclipse.core.commands.ExecutionEvent());
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setFocus() {
        if (chartComposite != null && !chartComposite.isDisposed()) {
            chartComposite.setFocus();
        }
    }
    
    /**
     * Apply smart axis buffering to improve visualization
     * Particularly important for constant-time algorithms to show flat behavior
     */
    private void applyAxisBuffering(AlgorithmConfig config) {
        if (chart == null || dataset.getSeriesCount() == 0) {
            return;
        }
        
        XYPlot plot = chart.getXYPlot();
        
        // Check axis types - could be LogAxis or NumberAxis depending on current setting
        org.jfree.chart.axis.ValueAxis yAxis = plot.getRangeAxis();
        org.jfree.chart.axis.ValueAxis xAxis = plot.getDomainAxis();
        
        // Only apply buffering for linear (NumberAxis) scales
        // Log scales handle their own range optimization
        if (!(yAxis instanceof NumberAxis) || !(xAxis instanceof NumberAxis)) {
            System.out.println("[RuntimeAnalysisView] Skipping axis buffering - currently using logarithmic axes");
            return;
        }
        
        // Cast to NumberAxis now that we've verified the type
        NumberAxis yNumberAxis = (NumberAxis) yAxis;
        NumberAxis xNumberAxis = (NumberAxis) xAxis;
        
        // Calculate data bounds
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        
        // Find actual data bounds (skip reference lines)
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            
            // Skip reference lines
            boolean isReferenceLine = false;
            for (BigOhFunction func : BigOhFunction.values()) {
                if (seriesKey.equals(func.getNotation())) {
                    isReferenceLine = true;
                    break;
                }
            }
            
            if (!isReferenceLine) {
                XYSeries series = dataset.getSeries(i);
                for (int j = 0; j < series.getItemCount(); j++) {
                    double x = series.getX(j).doubleValue();
                    double y = series.getY(j).doubleValue();
                    
                    if (y > 0 && !Double.isInfinite(y) && !Double.isNaN(y)) {
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                    if (x > 0 && !Double.isInfinite(x) && !Double.isNaN(x)) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                    }
                }
            }
        }
        
        if (minY != Double.MAX_VALUE && maxY != Double.MIN_VALUE) {
            // Determine buffer size based on algorithm type
            double yBufferFactor = getBufferFactor(config);
            
            // For linear scale, work directly with values
            double range = maxY - minY;
            double bufferedMinY = minY - (range * yBufferFactor);
            double bufferedMaxY = maxY + (range * yBufferFactor);
            
            // Ensure minimum range for constant-time operations
            if (range < 0.001) { // Very small range, probably constant-time (less than 0.001ms)
                double center = (minY + maxY) / 2.0;
                double minRange = Math.max(0.001, center * 0.1); // At least 0.001ms or 10% of center
                bufferedMinY = center - minRange;
                bufferedMaxY = center + minRange;
            }
            
            // Don't go below zero for timing data
            bufferedMinY = Math.max(0, bufferedMinY);
            
            yNumberAxis.setRange(bufferedMinY, bufferedMaxY);
            
            System.out.println(String.format("[RuntimeAnalysisView] Applied Y-axis buffering (linear): %.3f-%.3f ms (buffer factor: %.2f)", 
                bufferedMinY, bufferedMaxY, yBufferFactor));
        }
        
        // Apply X-axis buffering as well
        if (minX != Double.MAX_VALUE && maxX != Double.MIN_VALUE) {
            double xBufferFactor = 0.1; // 10% buffer on X-axis
            
            // For linear scale, work directly with values
            double rangeX = maxX - minX;
            double bufferedMinX = minX - (rangeX * xBufferFactor);
            double bufferedMaxX = maxX + (rangeX * xBufferFactor);
            
            // Don't go below zero for input sizes
            bufferedMinX = Math.max(0, bufferedMinX);
            
            xNumberAxis.setRange(bufferedMinX, bufferedMaxX);
        }
    }
    
    /**
     * Determine appropriate buffer factor based on algorithm complexity
     */
    private double getBufferFactor(AlgorithmConfig config) {
        if (config == null) {
            return 0.2; // Default 20% buffer
        }
        
        // Check if algorithm is expected to be constant time
        String expectedComplexity = config.getExpectedComplexity();
        if (expectedComplexity != null && expectedComplexity.contains("O(1)")) {
            return 0.5; // 50% buffer for constant-time to show flatness clearly
        }
        
        // Check analysis mode for constant time operations
        if (config.getAnalysisMode() == AnalysisMode.CONSTANT_TIME) {
            return 0.5; // 50% buffer for constant-time analysis mode
        }
        
        // For other algorithms, use smaller buffer
        return 0.25; // 25% buffer for non-constant algorithms
    }
    
    /**
     * Sets the analysis running state
     * @param running true if analysis is running, false otherwise
     */
    public void setAnalysisRunning(boolean running) {
        this.analysisRunning = running;
        
        Display.getDefault().asyncExec(() -> {
            if (running) {
                statusLabel.setText("Analysis Running...");
                runAnotherButton.setEnabled(false);
            } else {
                runAnotherButton.setEnabled(true);
            }
        });
    }
    
    /**
     * @return true if analysis is currently running
     */
    public boolean isAnalysisRunning() {
        return analysisRunning;
    }
    
    /**
     * Updates the progress bar and label
     * 
     * @param currentStep Current step (0-based)
     * @param totalSteps Total number of steps
     * @param stepDescription Description of current step
     */
    public void updateProgress(int currentStep, int totalSteps, String stepDescription) {
        Display.getDefault().asyncExec(() -> {
            if (progressBar != null && !progressBar.isDisposed()) {
                int percentage = totalSteps > 0 ? (currentStep * 100 / totalSteps) : 0;
                progressBar.setSelection(percentage);
                progressBar.setVisible(true);
            }
            
            if (progressLabel != null && !progressLabel.isDisposed()) {
                progressLabel.setText(stepDescription);
                progressLabel.setVisible(true);
            }
        });
    }
    
    /**
     * Shows the progress bar when analysis starts
     * 
     * @param totalSteps Total number of steps in the analysis
     */
    public void showProgress(int totalSteps) {
        Display.getDefault().asyncExec(() -> {
            if (progressBar != null && !progressBar.isDisposed()) {
                progressBar.setMaximum(100);
                progressBar.setSelection(0);
                progressBar.setVisible(true);
            }
            
            if (progressLabel != null && !progressLabel.isDisposed()) {
                progressLabel.setText("Starting analysis...");
                progressLabel.setVisible(true);
            }
        });
    }
    
    /**
     * Hides the progress bar when analysis completes
     */
    public void hideProgress() {
        Display.getDefault().asyncExec(() -> {
            if (progressBar != null && !progressBar.isDisposed()) {
                progressBar.setVisible(false);
            }
            
            if (progressLabel != null && !progressLabel.isDisposed()) {
                progressLabel.setVisible(false);
            }
        });
    }
    
    /**
     * Custom renderer that colors timeout points red
     */
    private class TimeoutAwareRenderer extends XYLineAndShapeRenderer {
        
        private List<ExecutionResult> timeoutData;
        
        public TimeoutAwareRenderer() {
            super(true, true); // Enable lines and shapes by default for all series
        }
        
        public void setTimeoutData(List<ExecutionResult> results) {
            this.timeoutData = results;
        }
        
        @Override
        public java.awt.Paint getItemPaint(int series, int item) {
            // For algorithm series (series 0), check if this point timed out
            if (series == 0 && timeoutData != null && item < timeoutData.size()) {
                ExecutionResult result = timeoutData.get(item);
                if (result.isTimedOut()) {
                    return Color.RED; // Color timeout points red
                }
            }
            
            // Default coloring for successful points and reference lines
            return super.getItemPaint(series, item);
        }
        
        @Override
        public java.awt.Paint getItemFillPaint(int series, int item) {
            // For algorithm series (series 0), check if this point timed out
            if (series == 0 && timeoutData != null && item < timeoutData.size()) {
                ExecutionResult result = timeoutData.get(item);
                if (result.isTimedOut()) {
                    return Color.RED; // Color timeout points red
                }
            }
            
            // Default coloring for successful points and reference lines
            return super.getItemFillPaint(series, item);
        }
    }
    
    /**
     * Marks this view as just opened and shows helpful instructions
     */
    public void markAsJustOpened() {
        wasJustOpened = true;
        showViewInstructionsPopup();
    }
    
    /**
     * Shows helpful instructions popup for using the view when it's not easily visible
     */
    private void showViewInstructionsPopup() {
        Display.getDefault().asyncExec(() -> {
            MessageDialog.openInformation(
                getSite().getShell(),
                "Runtime Analysis View Opened",
                "📊 The Runtime Analysis view has been opened to show your results!\n\n" +
                "💡 Tip: If you can't see the view clearly:\n" +
                "   • Drag the separator above the view to make it larger\n" +
                "   • Double-click the 'Runtime Analysis' tab to maximize the view\n" +
                "   • Use Window → Show View → Other → Algorithm Analysis → Runtime Analysis to reopen it\n\n" +
                "The analysis will begin shortly and charts will appear in this view."
            );
        });
    }
    
    /**
     * Checks if this view was just opened (not already visible)
     */
    public boolean wasJustOpened() {
        return wasJustOpened;
    }
    
    /**
     * Clears the "just opened" flag
     */
    public void clearJustOpenedFlag() {
        wasJustOpened = false;
    }
}