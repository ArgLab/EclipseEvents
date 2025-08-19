// File: AlgorithmConfig.java
package edu.runtimeanalysis.models;

/**
 * Configuration for an algorithm to be analyzed
 */
public class AlgorithmConfig {
    private final String name;
    private final String className;
    private final String category;
    private final MethodSignature methodSignature;
    private final AnalysisMode analysisMode;
    private final AnalysisParameter analysisParameter;
    private final String expectedComplexity;
    private final String setupInstructions;
    private final String[] requiredMethods;
    
    // Old constructor for backward compatibility
    public AlgorithmConfig(String name, String className, String category, MethodSignature methodSignature) {
        this(name, className, category, methodSignature, AnalysisMode.TRADITIONAL_SCALING, 
             new InputSizeParameter(), "Unknown", null, null);
    }
    
    // Constructor with dependency support
    public AlgorithmConfig(String name, String className, String category, MethodSignature methodSignature,
                          AnalysisMode analysisMode, AnalysisParameter analysisParameter, 
                          String expectedComplexity, String setupInstructions, String[] requiredMethods) {
        this.name = name;
        this.className = className;
        this.category = category;
        this.methodSignature = methodSignature;
        this.analysisMode = analysisMode;
        this.analysisParameter = analysisParameter;
        this.expectedComplexity = expectedComplexity;
        this.setupInstructions = setupInstructions;
        this.requiredMethods = requiredMethods != null ? requiredMethods : new String[0];
    }
    
    // Legacy constructor for existing code
    public AlgorithmConfig(String name, String className, String category, MethodSignature methodSignature,
                          AnalysisMode analysisMode, AnalysisParameter analysisParameter, 
                          String expectedComplexity, String setupInstructions) {
        this(name, className, category, methodSignature, analysisMode, analysisParameter, 
             expectedComplexity, setupInstructions, null);
    }
    
    public String getName() {
        return name;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getCategory() {
        return category;
    }
    
    public MethodSignature getMethodSignature() {
        return methodSignature;
    }
    
    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }
    
    public AnalysisParameter getAnalysisParameter() {
        return analysisParameter;
    }
    
    public String getExpectedComplexity() {
        return expectedComplexity;
    }
    
    public String getSetupInstructions() {
        return setupInstructions;
    }
    
    public String[] getRequiredMethods() {
        return requiredMethods;
    }
    
    public boolean hasRequiredMethods() {
        return requiredMethods != null && requiredMethods.length > 0;
    }
}


