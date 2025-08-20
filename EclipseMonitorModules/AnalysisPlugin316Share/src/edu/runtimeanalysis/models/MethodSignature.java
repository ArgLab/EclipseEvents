// File: MethodSignature.java
package edu.runtimeanalysis.models;

/**
 * Represents a method signature for detection
 */
public class MethodSignature {
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final String displaySignature;
    
    public MethodSignature(String methodName, Class<?>[] parameterTypes, 
                          Class<?> returnType, String displaySignature) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.displaySignature = displaySignature;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public String getDisplaySignature() {
        return displaySignature;
    }
}