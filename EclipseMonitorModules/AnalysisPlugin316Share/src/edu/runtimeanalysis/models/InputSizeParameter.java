package edu.runtimeanalysis.models;

/**
 * Parameter for traditional input size scaling
 */
public class InputSizeParameter implements AnalysisParameter {
    private static final Integer[] DEFAULT_SIZES = {
    	    500, 650, 800, 1000, 1300, 1600, 2000, 2500, 3200, 4000,
    	    5000, 6300, 8000, 10000, 12600, 16000, 20000, 25000, 31500, 40000
    	};
    
    @Override
    public String getName() {
        return "Input Size";
    }
    
    @Override
    public String getDescription() {
        return "Select input sizes to test algorithm performance. Each size will be run separately.";
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
}