package edu.runtimeanalysis.models;

/**
 * Interface representing a parameter that can be varied during analysis
 */
public interface AnalysisParameter {
    /**
     * Get the display name of this parameter
     */
    String getName();
    
    /**
     * Get the description of this parameter
     */
    String getDescription();
    
    /**
     * Get the data type of this parameter's values
     */
    Class<?> getValueType();
    
    /**
     * Get the default values for this parameter
     */
    Object[] getDefaultValues();
    
    /**
     * Validate if a value is appropriate for this parameter
     */
    boolean isValidValue(Object value);
}