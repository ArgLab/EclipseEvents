package edu.runtimeanalysis.models;

/**
 * Represents a single test scenario with pre-populated structure and method arguments
 */
public class TestScenario {
    private final Object prePopulatedStructure;  // Pre-built data structure
    private Object[] methodArguments;      // Arguments for the method call
    private final String scenarioDescription;    // "Position 0 of 1000", "Load factor 0.75"
    private final double xAxisValue;             // What to plot on X-axis
    private final boolean decrementer;
    
    public TestScenario(Object prePopulatedStructure, Object[] methodArguments, 
                       String scenarioDescription, double xAxisValue) {
        this(prePopulatedStructure, methodArguments, scenarioDescription, xAxisValue, false);
    }
    
    public TestScenario(Object prePopulatedStructure, Object[] methodArguments, 
	            String scenarioDescription, double xAxisValue, boolean decrementer) {
		this.prePopulatedStructure = prePopulatedStructure;
		this.methodArguments = methodArguments;
		this.scenarioDescription = scenarioDescription;
		this.xAxisValue = xAxisValue;
		this.decrementer = decrementer;
	}
    
    public Object getPrePopulatedStructure() {
        return prePopulatedStructure;
    }
    
    public Object[] getMethodArguments() {
        if (decrementer) {
            // Create a shallow copy of the array
            Object[] copy = methodArguments.clone();

            // Assuming methodArguments[0] is an Integer
            if (methodArguments.length > 0 && methodArguments[0] instanceof Integer) {
                int value = (Integer) methodArguments[0]; // unbox
                copy[0] = value - 1;                      // modify the copy, not original
            } else {
                throw new IllegalStateException("Expected first argument to be an Integer.");
            }

            return copy;
        }
        return methodArguments;
    }

    
    public String getScenarioDescription() {
        return scenarioDescription;
    }
    
    public double getXAxisValue() {
        return xAxisValue;
    }
    
    @Override
    public String toString() {
        return scenarioDescription;
    }
}