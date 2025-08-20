package edu.runtimeanalysis.models;

/**
 * Defines different modes for algorithm analysis, each optimized for specific types of algorithms
 */
public enum AnalysisMode {
    /**
     * Traditional scaling analysis - varies input array/collection size from small to large.
     * Used for: Sorting algorithms, bulk operations, array processing
     */
    TRADITIONAL_SCALING("Input Size", "Size of input array/collection"),
    
    /**
     * Position-based analysis - varies input size, shows 3 lines for Beginning/Middle/End positions.
     * Used for: Indexed operations on lists/arrays (get, add, remove at index)
     */
    POSITION_BASED("Input Size", "Array size with position-dependent performance analysis"),
    
    /**
     * Load factor analysis - pre-populate structure to different load factors, test operations.
     * Used for: Hash-based data structures, resizing operations
     */
    LOAD_FACTOR("Load Factor", "Ratio of entries to buckets in hash structure"),
    
    /**
     * Structure size analysis - pre-populate structure with N elements, test single operation.
     * Used for: Operations that depend on existing structure size
     */
    STRUCTURE_SIZE("Structure Size", "Number of existing elements in structure"),
    
    /**
     * Constant time verification - test operation on dramatically different sized structures.
     * Used for: Operations claimed to be O(1)
     */
    CONSTANT_TIME("Structure Size", "Number of existing elements (expecting constant time)"),
    
    /**
     * Tree depth analysis - create trees with controlled depths/balance, measure operations.
     * Used for: Tree operations
     */
    DEPTH_ANALYSIS("Tree Depth", "Depth or balance characteristics of tree structure"),
    
    /**
     * Input size with position analysis - varies input size, tests multiple positions.
     * Shows 3 lines for Beginning, Middle, End positions on same chart.
     * Used for: Search operations where position may affect performance
     */
    INPUT_SIZE_WITH_POSITIONS("Input Size", "Array size with position-dependent performance analysis"),
    
    /**
     * Input size with collision analysis - varies input size, tests collision scenarios.
     * Shows 3 lines for Low, Medium, High collision rates on same chart.
     * Used for: Hash-based operations with collision-dependent performance
     */
    INPUT_SIZE_WITH_COLLISIONS("Input Size", "Array size with collision scenario analysis");
    
    private final String xAxisLabel;
    private final String description;
    
    AnalysisMode(String xAxisLabel, String description) {
        this.xAxisLabel = xAxisLabel;
        this.description = description;
    }
    
    public String getXAxisLabel() {
        return xAxisLabel;
    }
    
    public String getDescription() {
        return description;
    }
}