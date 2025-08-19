package edu.runtimeanalysis.models;

/**
 * Parameter for algorithms where input size varies but position within data affects performance.
 * Generates multiple data series for Beginning, Middle, and End positions.
 * Used for search operations where implementation quality affects position-dependent performance.
 */
public class InputSizeWithPositionsParameter implements AnalysisParameter {
    private static final Integer[] DEFAULT_SIZES = {
        50, 75, 100, 150, 200, 300, 400, 500, 650, 800, 
        1000, 1300, 1600, 2000, 2500, 3200, 4000, 5000, 6500, 8000, 
        10000, 13000, 16000, 20000, 25000
    };
    
    @Override
    public String getName() {
        return "Input Size (with Positions)";
    }
    
    @Override
    public String getDescription() {
        return "Select input sizes to test. Performance will be measured at Beginning, Middle, and End positions.";
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
    
    /**
     * Get positions for testing within an array of given size
     */
    public static class Position {
        public static final String BEGINNING = "Beginning";
        public static final String MIDDLE = "Middle"; 
        public static final String END = "End";
        
        public static int getIndex(String position, int arraySize) {
            switch (position) {
                case BEGINNING:
                    return 0;
                case MIDDLE:
                    return arraySize / 2;
                case END:
                    return arraySize - 1;
                default:
                    throw new IllegalArgumentException("Unknown position: " + position);
            }
        }
        
        public static String[] getAllPositions() {
            return new String[]{BEGINNING, MIDDLE, END};
        }
    }
}