package edu.runtimeanalysis.models;

/**
 * Parameter for hash map analysis using collision rate instead of load factor.
 * More student-friendly than load factor - focuses on collision probability.
 * Generates multiple data series for Low, Medium, and High collision scenarios.
 */
public class CollisionRateParameter implements AnalysisParameter {
    private static final Integer[] DEFAULT_SIZES = {
        200, 300, 400, 500, 650, 800, 1000, 1300, 1600, 2000, 
        2500, 3200, 4000, 5000, 6500, 8000, 10000, 13000, 16000, 20000, 
        25000, 32000, 40000
    };
    private final int baseBuckets;
    
    public CollisionRateParameter(int baseBuckets) {
        this.baseBuckets = baseBuckets;
    }
    
    @Override
    public String getName() {
        return "Input Size (with Collision Scenarios)";
    }
    
    @Override
    public String getDescription() {
        return "Input size with performance tested under Low, Medium, and High collision scenarios";
    }
    
    @Override
    public Class<?> getValueType() {
        return Integer.class;
    }
    
    @Override
    public Object[] getDefaultValues() {
        return DEFAULT_SIZES;
    }
    
    @Override
    public boolean isValidValue(Object value) {
        return value instanceof Integer && (Integer) value > 0;
    }
    
    public int getBaseBuckets() {
        return baseBuckets;
    }
    
    /**
     * Collision scenarios for hash map testing
     */
    public static class CollisionScenario {
        public static final String LOW = "Low Collisions";
        public static final String MEDIUM = "Medium Collisions";
        public static final String HIGH = "High Collisions";
        
        /**
         * Get the number of buckets to use for a given scenario and input size
         */
        public static int getBucketsForScenario(String scenario, int inputSize, int baseBuckets) {
            switch (scenario) {
                case LOW:
                    // Load factor ~0.5 - plenty of space
                    return Math.max(baseBuckets, inputSize * 2);
                case MEDIUM:
                    // Load factor ~1.0 - moderate collisions
                    return Math.max(baseBuckets, inputSize);
                case HIGH:
                    // Load factor ~2.0 - high collision rate
                    return Math.max(baseBuckets, inputSize / 2);
                default:
                    throw new IllegalArgumentException("Unknown collision scenario: " + scenario);
            }
        }
        
        public static String[] getAllScenarios() {
            return new String[]{LOW, MEDIUM, HIGH};
        }
        
        public static String getDescription(String scenario) {
            switch (scenario) {
                case LOW:
                    return "Spacious hash table - minimal collisions";
                case MEDIUM:
                    return "Balanced hash table - some collisions";
                case HIGH:
                    return "Crowded hash table - frequent collisions";
                default:
                    return "Unknown scenario";
            }
        }
    }
}