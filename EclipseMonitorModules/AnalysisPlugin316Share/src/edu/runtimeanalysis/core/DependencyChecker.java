package edu.runtimeanalysis.core;

import java.lang.reflect.Method;
import edu.runtimeanalysis.models.AlgorithmConfig;

/**
 * Basic dependency checker for high-risk methods that require setup
 */
public class DependencyChecker {
    
    public static class DependencyResult {
        private final boolean success;
        private final String errorMessage;
        
        public DependencyResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public static DependencyResult success() {
            return new DependencyResult(true, null);
        }
        
        public static DependencyResult failure(String message) {
            return new DependencyResult(false, message);
        }
    }
    
    /**
     * Check if required methods are implemented and working for basic functionality
     */
    public static DependencyResult checkDependencies(Class<?> algorithmClass, AlgorithmConfig config) {
        if (!config.hasRequiredMethods()) {
            return DependencyResult.success();
        }
        
        try {
            String className = algorithmClass.getName();
            String[] required = config.getRequiredMethods();
            
            for (String methodName : required) {
                DependencyResult result = testRequiredMethod(algorithmClass, methodName, className);
                if (!result.isSuccess()) {
                    return DependencyResult.failure(String.format(
                        "Cannot test %s because the required method '%s' is not working properly.\n\n" +
                        "Please fix the %s method first.",
                        config.getName(), methodName, methodName
                    ));
                }
            }
            
            return DependencyResult.success();
            
        } catch (Exception e) {
            return DependencyResult.failure(String.format(
                "Error checking dependencies for %s.\n\n" +
                "If this error persists, contact jtbacher@ncsu.edu",
                config.getName()
            ));
        }
    }
    
    /**
     * Test a specific required method with basic functionality
     */
    private static DependencyResult testRequiredMethod(Class<?> algorithmClass, String methodName, String className) {
        try {
            Object instance = algorithmClass.getDeclaredConstructor().newInstance();
            
            if (methodName.equals("add") && (className.contains("List"))) {
                return testListAdd(algorithmClass, instance);
            } else if (methodName.equals("push") && className.contains("Stack")) {
                return testStackPush(algorithmClass, instance);
            } else if (methodName.equals("enqueue") && className.contains("Queue")) {
                return testQueueEnqueue(algorithmClass, instance);
            } else if (methodName.equals("put") && className.contains("Map")) {
                return testMapPut(algorithmClass, instance);
            } else if (methodName.equals("add") && className.contains("Set")) {
                return testSetAdd(algorithmClass, instance);
            } else {
                // For other methods, just check if they exist
                algorithmClass.getMethod(methodName);
                return DependencyResult.success();
            }
            
        } catch (NoSuchMethodException e) {
            return DependencyResult.failure(methodName + "() method not implemented");
        } catch (Exception e) {
            return DependencyResult.failure(methodName + "() method is not working correctly");
        }
    }
    
    private static DependencyResult testListAdd(Class<?> clazz, Object instance) {
        try {
            Method addMethod = clazz.getMethod("add", int.class, Object.class);
            Method sizeMethod = clazz.getMethod("size");
            
            // Test basic add functionality
            int initialSize = (Integer) sizeMethod.invoke(instance);
            addMethod.invoke(instance, 0, "test-element");
            int newSize = (Integer) sizeMethod.invoke(instance);
            
            if (newSize != initialSize + 1) {
                return DependencyResult.failure("add() method is not working correctly");
            }
            
            return DependencyResult.success();
        } catch (Exception e) {
            return DependencyResult.failure("add() method is not working correctly");
        }
    }
    
    private static DependencyResult testStackPush(Class<?> clazz, Object instance) {
        try {
            Method pushMethod = clazz.getMethod("push", Object.class);
            Method sizeMethod = clazz.getMethod("size");
            
            int initialSize = (Integer) sizeMethod.invoke(instance);
            pushMethod.invoke(instance, "test-element");
            int newSize = (Integer) sizeMethod.invoke(instance);
            
            if (newSize != initialSize + 1) {
                return DependencyResult.failure("push() method is not working correctly");
            }
            
            return DependencyResult.success();
        } catch (Exception e) {
            return DependencyResult.failure("push() method is not working correctly");
        }
    }
    
    private static DependencyResult testQueueEnqueue(Class<?> clazz, Object instance) {
        try {
            Method enqueueMethod = clazz.getMethod("enqueue", Object.class);
            Method sizeMethod = clazz.getMethod("size");
            
            int initialSize = (Integer) sizeMethod.invoke(instance);
            enqueueMethod.invoke(instance, "test-element");
            int newSize = (Integer) sizeMethod.invoke(instance);
            
            if (newSize != initialSize + 1) {
                return DependencyResult.failure("enqueue() method is not working correctly");
            }
            
            return DependencyResult.success();
        } catch (Exception e) {
            return DependencyResult.failure("enqueue() method is not working correctly");
        }
    }
    
    private static DependencyResult testMapPut(Class<?> clazz, Object instance) {
        try {
            Method putMethod = clazz.getMethod("put", Object.class, Object.class);
            Method sizeMethod = clazz.getMethod("size");
            
            int initialSize = (Integer) sizeMethod.invoke(instance);
            putMethod.invoke(instance, "test-key", "test-value");
            int newSize = (Integer) sizeMethod.invoke(instance);
            
            if (newSize != initialSize + 1) {
                return DependencyResult.failure("put() method is not working correctly");
            }
            
            return DependencyResult.success();
        } catch (Exception e) {
            return DependencyResult.failure("put() method is not working correctly");
        }
    }
    
    private static DependencyResult testSetAdd(Class<?> clazz, Object instance) {
        try {
            Method addMethod = clazz.getMethod("add", Object.class);
            Method sizeMethod = clazz.getMethod("size");
            
            int initialSize = (Integer) sizeMethod.invoke(instance);
            addMethod.invoke(instance, "test-element");
            int newSize = (Integer) sizeMethod.invoke(instance);
            
            if (newSize != initialSize + 1) {
                return DependencyResult.failure("add() method is not working correctly");
            }
            
            return DependencyResult.success();
        } catch (Exception e) {
            return DependencyResult.failure("add() method is not working correctly");
        }
    }
}