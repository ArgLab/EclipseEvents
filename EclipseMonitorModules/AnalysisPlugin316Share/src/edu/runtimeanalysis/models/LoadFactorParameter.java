package edu.runtimeanalysis.models;

/**
 * Parameter for load factor analysis - ratio of entries to buckets in hash structures
 */
public class LoadFactorParameter implements AnalysisParameter {
    private static final Double[] DEFAULT_LOAD_FACTORS = {0.25, 0.5, 0.75, 0.9, 1.5};
    private final int buckets;
    
    public LoadFactorParameter(int buckets) {
        this.buckets = buckets;
    }
    
    @Override
    public String getName() {
        return "Load Factor";
    }
    
    @Override
    public String getDescription() {
        return "Ratio of entries to buckets (buckets = " + buckets + ")";
    }
    
    @Override
    public Class<?> getValueType() {
        return Double.class;
    }
    
    @Override
    public Object[] getDefaultValues() {
        return DEFAULT_LOAD_FACTORS;
    }
    
    @Override
    public boolean isValidValue(Object value) {
        return value instanceof Double && (Double) value > 0;
    }
    
    public int getBuckets() {
        return buckets;
    }
    
    /**
     * Calculate number of entries for given load factor
     */
    public int getEntriesForLoadFactor(double loadFactor) {
        return (int) Math.round(buckets * loadFactor);
    }
}