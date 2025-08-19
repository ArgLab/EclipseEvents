package edu.runtimeanalysis.models;

/**
 * Parameter for position-based analysis - positions within a fixed-size structure
 */
public class PositionIndexParameter implements AnalysisParameter {
    private final int structureSize;
    private final Integer[] positions;
    
    public PositionIndexParameter(int structureSize) {
        this.structureSize = structureSize;
        // Generate positions: beginning, quarters, end
        this.positions = new Integer[] {
            0, 
            structureSize / 4, 
            structureSize / 2, 
            (structureSize * 3) / 4, 
            structureSize - 1
        };
    }
    
    @Override
    public String getName() {
        return "Position Index";
    }
    
    @Override
    public String getDescription() {
        return "Index position within structure of size " + structureSize;
    }
    
    @Override
    public Class<?> getValueType() {
        return Integer.class;
    }
    
    @Override
    public Object[] getDefaultValues() {
        return positions;
    }
    
    @Override
    public boolean isValidValue(Object value) {
        return value instanceof Integer && (Integer) value >= 0 && (Integer) value < structureSize;
    }
    
    public int getStructureSize() {
        return structureSize;
    }
}