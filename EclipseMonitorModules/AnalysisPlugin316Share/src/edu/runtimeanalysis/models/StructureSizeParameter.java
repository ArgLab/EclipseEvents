package edu.runtimeanalysis.models;

/**
 * Parameter for structure size analysis - size of pre-populated structure
 */
public class StructureSizeParameter implements AnalysisParameter {
    private static final Integer[] DEFAULT_SIZES = {
        10, 25, 50, 75, 100, 150, 200, 300, 400, 500, 
        650, 800, 1000, 1250, 1500, 2000, 2500, 3000, 4000, 5000
    };
    
    @Override
    public String getName() {
        return "Structure Size";
    }
    
    @Override
    public String getDescription() {
        return "Select structure sizes to test. The data structure will be pre-populated with this many elements.";
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