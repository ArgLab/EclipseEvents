package edu.runtimeanalysis.models;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Manages reference lines for Big-O complexity functions on runtime analysis charts
 */
public class ReferenceLineManager {
    
    private final XYSeriesCollection dataset;
    private final XYPlot plot;
    private final Set<BigOhFunction> enabledFunctions;
    private double scalingFactor = 1.0;
    private int minInputSize = 1;
    private int maxInputSize = 10000;
    
    // Distinct colors for reference lines
    private static final Color[] REFERENCE_COLORS = {
        Color.RED,
        Color.BLUE, 
        Color.GREEN,
        Color.ORANGE,
        Color.MAGENTA,
        Color.CYAN,
        new Color(128, 0, 128), // Purple
        new Color(139, 69, 19)  // Brown
    };
    
    public ReferenceLineManager(XYSeriesCollection dataset, XYPlot plot) {
        this.dataset = dataset;
        this.plot = plot;
        this.enabledFunctions = new HashSet<>();
    }
    
    /**
     * Enable a Big-O reference line
     */
    public void enableFunction(BigOhFunction function) {
        enabledFunctions.add(function);
        updateReferenceLines();
    }
    
    /**
     * Disable a Big-O reference line
     */
    public void disableFunction(BigOhFunction function) {
        enabledFunctions.remove(function);
        removeReferenceLine(function);
    }
    
    /**
     * Check if a function is currently enabled
     */
    public boolean isFunctionEnabled(BigOhFunction function) {
        return enabledFunctions.contains(function);
    }
    
    /**
     * Get all currently enabled functions
     */
    public Set<BigOhFunction> getEnabledFunctions() {
        return new HashSet<>(enabledFunctions);
    }
    
    /**
     * Update scaling factor based on actual runtime data
     */
    public void updateScaling(List<Double> actualTimes, List<Integer> inputSizes) {
        if (actualTimes.isEmpty() || inputSizes.isEmpty()) {
            return;
        }
        
        // Clear cached data since we have new actual data
        cachedValidTimes = null;
        
        // Find the best scaling factor by trying multiple complexity functions
        double bestScalingFactor = findBestScalingFactor(actualTimes, inputSizes);
        
        if (bestScalingFactor > 0) {
            scalingFactor = bestScalingFactor;
            updateReferenceLines();
        }
    }
    
    /**
     * Find the scaling factor that puts reference lines closest to actual data
     */
    private double findBestScalingFactor(List<Double> actualTimes, List<Integer> inputSizes) {
        // Calculate median actual time to use as reference point
        List<Double> validTimes = new ArrayList<>();
        List<Integer> validSizes = new ArrayList<>();
        
        for (int i = 0; i < Math.min(actualTimes.size(), inputSizes.size()); i++) {
            double time = actualTimes.get(i);
            int size = inputSizes.get(i);
            if (time > 0 && size > 0) {
                validTimes.add(time);
                validSizes.add(size);
            }
        }
        
        if (validTimes.isEmpty()) return 1.0;
        
        // Use middle data point for scaling to avoid outliers
        int midIndex = validTimes.size() / 2;
        double referenceTime = validTimes.get(midIndex);
        int referenceSize = validSizes.get(midIndex);
        
        // Try different complexity functions and find the best fit
        double bestFactor = 1.0;
        double minError = Double.MAX_VALUE;
        
        BigOhFunction[] candidates = {
            BigOhFunction.CONSTANT, BigOhFunction.LOGARITHMIC, BigOhFunction.SQRT,
            BigOhFunction.LINEAR, BigOhFunction.LINEARITHMIC, BigOhFunction.QUADRATIC
        };
        
        for (BigOhFunction func : candidates) {
            double theoreticalValue = func.calculate(referenceSize);
            if (theoreticalValue > 0) {
                double factor = referenceTime / theoreticalValue;
                
                // Calculate total error for this scaling factor
                double totalError = 0;
                for (int i = 0; i < validTimes.size(); i++) {
                    double actualTime = validTimes.get(i);
                    double scaledTheoretical = func.calculate(validSizes.get(i)) * factor;
                    if (scaledTheoretical > 0) {
                        // Use relative error to handle different magnitudes
                        double relativeError = Math.abs(actualTime - scaledTheoretical) / actualTime;
                        totalError += relativeError;
                    }
                }
                
                if (totalError < minError) {
                    minError = totalError;
                    bestFactor = factor;
                }
            }
        }
        
        return bestFactor;
    }
    
    /**
     * Get adjusted scaling factor for specific complexity functions
     * This helps make reference lines more intuitive for students
     */
    private double getAdjustedScaling(BigOhFunction function) {
        // For constant time operations, use a direct approach based on actual data
        if (function == BigOhFunction.CONSTANT) {
            return getConstantLineScaling();
        }
        
        double baseScaling = scalingFactor;
        
        // Get smart scaling that ensures visibility even for mismatched complexities
        double smartScaling = getSmartScaling(function, baseScaling);
        
        // Apply function-specific adjustments to the smart scaling
        switch (function) {
            case LOGARITHMIC:
                return smartScaling * 1.2;
                
            case SQRT:
                return smartScaling * 1.1;
                
            case LINEAR:
                return smartScaling;
                
            case LINEARITHMIC:
                return smartScaling * 0.95;
                
            case QUADRATIC:
                return smartScaling * 0.8;
                
            case CUBIC:
                return smartScaling * 0.6;
                
            case EXPONENTIAL:
                return smartScaling * 1.5;
                
            default:
                return smartScaling;
        }
    }
    
    /**
     * Calculate smart scaling that ensures reference line visibility
     * even when complexity is mismatched to actual data
     */
    private double getSmartScaling(BigOhFunction function, double baseScaling) {
        List<Double> validTimes = getValidActualTimes();
        
        if (validTimes.isEmpty()) {
            return baseScaling;
        }
        
        // Get actual data range
        double minActualTime = validTimes.stream().mapToDouble(Double::doubleValue).min().orElse(1.0);
        double maxActualTime = validTimes.stream().mapToDouble(Double::doubleValue).max().orElse(1000.0);
        double actualRange = maxActualTime / minActualTime;
        
        // Get input size range from chart data
        double minInputSize = getMinInputSize();
        double maxInputSize = getMaxInputSize();
        
        if (minInputSize <= 0 || maxInputSize <= minInputSize) {
            return baseScaling; // Fallback if we can't determine input range
        }
        
        // Calculate what this function's theoretical range would be
        double theoreticalMin = function.calculate(minInputSize);
        double theoreticalMax = function.calculate(maxInputSize);
        
        if (theoreticalMin <= 0 || theoreticalMax <= theoreticalMin) {
            return baseScaling; // Fallback for invalid theoretical values
        }
        
        double theoreticalRange = theoreticalMax / theoreticalMin;
        
        // For visibility, we want the reference line to span a reasonable portion 
        // of the actual data range. Calculate scaling to achieve this.
        
        // Target: reference line should span roughly 0.3x to 3x of actual data range
        double targetMidpoint = Math.sqrt(minActualTime * maxActualTime); // Geometric mean
        double theoreticalMidpoint = Math.sqrt(theoreticalMin * theoreticalMax);
        
        if (theoreticalMidpoint > 0) {
            double scaling = targetMidpoint / theoreticalMidpoint;
            
            // Clamp scaling to reasonable bounds to prevent extreme cases
            double minScaling = baseScaling * 0.01; // At least 1% of base scaling
            double maxScaling = baseScaling * 100;  // At most 100x base scaling
            
            scaling = Math.max(minScaling, Math.min(maxScaling, scaling));
            
            return scaling;
        }
        
        return baseScaling;
    }
    
    /**
     * Get minimum input size from actual chart data
     */
    private double getMinInputSize() {
        double minSize = Double.MAX_VALUE;
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            if (!isReferenceLineSeries(seriesKey)) {
                XYSeries series = dataset.getSeries(i);
                for (int j = 0; j < series.getItemCount(); j++) {
                    double x = series.getX(j).doubleValue();
                    if (x > 0) {
                        minSize = Math.min(minSize, x);
                    }
                }
            }
        }
        return minSize == Double.MAX_VALUE ? this.minInputSize : minSize;
    }
    
    /**
     * Get maximum input size from actual chart data
     */
    private double getMaxInputSize() {
        double maxSize = Double.MIN_VALUE;
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            if (!isReferenceLineSeries(seriesKey)) {
                XYSeries series = dataset.getSeries(i);
                for (int j = 0; j < series.getItemCount(); j++) {
                    double x = series.getX(j).doubleValue();
                    if (x > 0) {
                        maxSize = Math.max(maxSize, x);
                    }
                }
            }
        }
        return maxSize == Double.MIN_VALUE ? this.maxInputSize : maxSize;
    }
    
    /**
     * Check if a series key represents a reference line
     */
    private boolean isReferenceLineSeries(String seriesKey) {
        for (BigOhFunction func : BigOhFunction.values()) {
            if (seriesKey.equals(func.getNotation())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate appropriate scaling for constant time reference line
     * Uses actual data statistics to position the line meaningfully
     */
    private double getConstantLineScaling() {
        List<Double> validTimes = getValidActualTimes();
        
        if (validTimes.isEmpty()) {
            return scalingFactor * 0.5; // Fallback to old behavior
        }
        
        // For constant time operations, use median of actual times
        // This positions the O(1) line right through the middle of the data
        validTimes.sort(Double::compareTo);
        double median;
        int size = validTimes.size();
        
        if (size % 2 == 0) {
            median = (validTimes.get(size / 2 - 1) + validTimes.get(size / 2)) / 2.0;
        } else {
            median = validTimes.get(size / 2);
        }
        
        // Return the median directly as the scaling factor for O(1) = 1
        return median;
    }
    
    // Cached valid times to avoid recomputation
    private List<Double> cachedValidTimes = null;
    
    /**
     * Get list of valid actual runtime data
     */
    private List<Double> getValidActualTimes() {
        if (cachedValidTimes != null) {
            return cachedValidTimes;
        }
        
        cachedValidTimes = new ArrayList<>();
        
        // Extract actual data from chart series
        // Look for non-reference series (student data)
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
                    double yValue = series.getY(j).doubleValue();
                    if (yValue > 0 && !Double.isInfinite(yValue) && !Double.isNaN(yValue)) {
                        cachedValidTimes.add(yValue);
                    }
                }
            }
        }
        
        return cachedValidTimes;
    }
    
    /**
     * Set input size range for reference lines
     */
    public void setInputSizeRange(int minSize, int maxSize) {
        this.minInputSize = Math.max(1, minSize);
        this.maxInputSize = Math.max(this.minInputSize, maxSize);
        updateReferenceLines();
    }
    
    /**
     * Update all enabled reference lines
     */
    private void updateReferenceLines() {
        // Remove existing reference lines
        removeAllReferenceLines();
        
        // Add enabled reference lines
        int colorIndex = 0;
        
        for (BigOhFunction function : enabledFunctions) {
            XYSeries series = createReferenceSeries(function);
            dataset.addSeries(series);
            
            // Find the actual series index after adding
            int actualSeriesIndex = findSeriesIndex(function.getNotation());
            if (actualSeriesIndex >= 0) {
                styleReferenceSeries(actualSeriesIndex, colorIndex);
                colorIndex++;
            }
        }
        
        // Force chart repaint to ensure styling is applied
        if (plot.getRenderer() instanceof XYLineAndShapeRenderer) {
            ((XYLineAndShapeRenderer) plot.getRenderer()).setDefaultEntityRadius(3);
            // Trigger chart update
        }
    }
    
    /**
     * Find the series index for a given series key
     */
    private int findSeriesIndex(String seriesKey) {
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (dataset.getSeriesKey(i).equals(seriesKey)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Apply consistent styling to reference line series
     */
    private void styleReferenceSeries(int seriesIndex, int colorIndex) {
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        if (renderer == null) return;
        
        Color color = REFERENCE_COLORS[colorIndex % REFERENCE_COLORS.length];
        
        // Apply reference line styling
        renderer.setSeriesPaint(seriesIndex, color);
        renderer.setSeriesStroke(seriesIndex, new BasicStroke(
            1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1.0f, new float[]{5.0f, 5.0f}, 0.0f)); // Dotted line
        renderer.setSeriesLinesVisible(seriesIndex, true);
        renderer.setSeriesShapesVisible(seriesIndex, false); // No shapes for reference lines
        
        System.out.println("[ReferenceLineManager] Styled reference series " + seriesIndex + 
            " (" + dataset.getSeriesKey(seriesIndex) + ") with color index " + colorIndex);
    }
    
    /**
     * Create a data series for a Big-O reference function
     */
    private XYSeries createReferenceSeries(BigOhFunction function) {
        XYSeries series = new XYSeries(function.getNotation(), false);
        
        // Generate points across the input size range
        List<Integer> sampleSizes = generateSampleSizes();
        
        for (int size : sampleSizes) {
            double theoreticalValue = function.calculate(size) * getAdjustedScaling(function);
            if (theoreticalValue > 0 && !Double.isInfinite(theoreticalValue) && !Double.isNaN(theoreticalValue)) {
                series.add(size, theoreticalValue);
            }
        }
        
        return series;
    }
    
    /**
     * Generate sample input sizes for reference lines
     */
    private List<Integer> generateSampleSizes() {
        List<Integer> sizes = new ArrayList<>();
        
        // Use logarithmic distribution for better coverage
        double logMin = Math.log(minInputSize);
        double logMax = Math.log(maxInputSize);
        int numPoints = 50;
        
        for (int i = 0; i < numPoints; i++) {
            double logSize = logMin + (logMax - logMin) * i / (numPoints - 1);
            int size = (int) Math.round(Math.exp(logSize));
            if (!sizes.contains(size) && size >= minInputSize && size <= maxInputSize) {
                sizes.add(size);
            }
        }
        
        return sizes;
    }
    
    /**
     * Remove a specific reference line
     */
    private void removeReferenceLine(BigOhFunction function) {
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (dataset.getSeriesKey(i).equals(function.getNotation())) {
                dataset.removeSeries(i);
                break;
            }
        }
    }
    
    /**
     * Remove all reference lines
     */
    private void removeAllReferenceLines() {
        List<String> referenceKeys = new ArrayList<>();
        for (BigOhFunction function : BigOhFunction.values()) {
            referenceKeys.add(function.getNotation());
        }
        
        // Remove series in reverse order to avoid index shifting
        for (int i = dataset.getSeriesCount() - 1; i >= 0; i--) {
            String seriesKey = (String) dataset.getSeriesKey(i);
            if (referenceKeys.contains(seriesKey)) {
                dataset.removeSeries(i);
            }
        }
    }
    
    /**
     * Re-apply styling to all reference lines
     * Call this after adding student data to prevent style conflicts
     */
    public void reapplyReferenceLineStyles() {
        int colorIndex = 0;
        for (BigOhFunction function : enabledFunctions) {
            int seriesIndex = findSeriesIndex(function.getNotation());
            if (seriesIndex >= 0) {
                styleReferenceSeries(seriesIndex, colorIndex);
                colorIndex++;
            } else {
                // Reference line series missing - this can happen during chart updates
                System.out.println("[ReferenceLineManager] WARNING: Reference line " + 
                    function.getNotation() + " is enabled but series not found. Refreshing...");
                // Re-create the missing reference line
                updateReferenceLines();
                return; // Exit early since updateReferenceLines will handle all styling
            }
        }
    }
    
    /**
     * Force refresh of all reference lines - use this to fix display issues
     */
    public void refreshAllReferenceLines() {
        System.out.println("[ReferenceLineManager] Force refreshing all reference lines");
        updateReferenceLines();
    }
    
    /**
     * Clear all enabled functions
     */
    public void clearAll() {
        enabledFunctions.clear();
        removeAllReferenceLines();
    }
}