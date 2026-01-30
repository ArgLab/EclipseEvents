package edu.runtimeanalysis.models;

/**
 * Enumeration of common Big-O complexity functions for reference line plotting
 */
public enum BigOhFunction {
    
    // Ordered from lowest to highest complexity
	CONSTANT("O(1)", "Constant", (n) -> 1),
    LOGARITHMIC("O(log n)", "Logarithmic", (n) -> Math.log(n) / Math.log(2)),
    SQRT("O(√n)", "Square Root", (n) -> Math.sqrt(n)),
    LINEAR("O(n)", "Linear", (n) -> n),
    LINEARITHMIC("O(n log n)", "Linearithmic", (n) -> n * Math.log(n) / Math.log(2)),
    QUADRATIC("O(n²)", "Quadratic", (n) -> n * n),
    CUBIC("O(n³)", "Cubic", (n) -> n * n * n),
    EXPONENTIAL("O(2^n)", "Exponential", (n) -> {
        if (n <= 0) return 1;
        // For exponential, we need to scale the input to prevent overflow
        // but still show exponential growth patterns for typical input ranges
        if (n <= 10) return Math.pow(2, n);  // Small inputs: use actual 2^n
        else if (n <= 20) return Math.pow(2, n) / 100.0;  // Medium: scale down
        else return Math.pow(2, Math.min(n/5.0, 30)); // Large: further scaling to prevent overflow
    });
    
    private final String notation;
    private final String description;
    private final ComplexityFunction function;
    
    BigOhFunction(String notation, String description, ComplexityFunction function) {
        this.notation = notation;
        this.description = description;
        this.function = function;
    }
    
    public String getNotation() {
        return notation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double calculate(double n) {
        if (n <= 0) return 1;
        return function.calculate(n);
    }
    
    @FunctionalInterface
    private interface ComplexityFunction {
        double calculate(double n);
    }
}