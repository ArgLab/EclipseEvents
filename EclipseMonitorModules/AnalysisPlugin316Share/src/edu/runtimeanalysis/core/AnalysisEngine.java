package edu.runtimeanalysis.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AnalysisMode;
import edu.runtimeanalysis.models.AnalysisParameter;
import edu.runtimeanalysis.models.CollisionRateParameter;
import edu.runtimeanalysis.models.InputSizeParameter;
import edu.runtimeanalysis.models.InputSizeWithPositionsParameter;
import edu.runtimeanalysis.models.LoadFactorParameter;
import edu.runtimeanalysis.models.MethodSignature;
import edu.runtimeanalysis.models.PositionIndexParameter;
import edu.runtimeanalysis.models.StructureSizeParameter;
import edu.runtimeanalysis.models.TestScenario;

/**
 * Core engine for running algorithm analysis
 */
public class AnalysisEngine {
    
    // Graph-related code removed - Workshops 11 & 12 temporarily disabled
    
    private static final int WARMUP_RUNS = 3;
    private static final int MEASUREMENT_RUNS = 5;
    private long timeoutMs = 15000; // Default 15 seconds
    
    private final ExecutorService executor;
    private final CodeScanner codeScanner;
    private final PerformanceMonitor monitor;
    private AnalysisProgressCallback progressCallback;
    private AnalysisResultsCallback resultsCallback;
    
    private Object[] recentlyGeneratedObjs;
    
    public AnalysisEngine() {
        this.executor = Executors.newSingleThreadExecutor();
        this.codeScanner = new CodeScanner();
        this.monitor = new PerformanceMonitor();
    }
    
    /**
     * Set a progress callback to receive updates during analysis
     * 
     * @param callback The progress callback, or null to disable progress reporting
     */
    public void setProgressCallback(AnalysisProgressCallback callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Set a results callback to receive progressive results during analysis
     * 
     * @param callback The results callback, or null to disable progressive results
     */
    public void setResultsCallback(AnalysisResultsCallback callback) {
        this.resultsCallback = callback;
    }
    
    /**
     * Set the timeout for individual algorithm runs
     * 
     * @param timeoutSeconds Timeout in seconds (must be positive)
     */
    public void setTimeoutSeconds(int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        this.timeoutMs = timeoutSeconds * 1000L;
    }
    
    // New primary method using analysis parameters and scenarios
    public List<ExecutionResult> runAnalysis(AlgorithmConfig config) throws Exception {
        // First find the method using CodeScanner (we need it for class context)
        Method method = findAlgorithmMethod(config);
        
        // Check dependencies for high-risk methods before attempting analysis
        if (config.hasRequiredMethods()) {
            DependencyChecker.DependencyResult depResult = DependencyChecker.checkDependencies(method.getDeclaringClass(), config);
            if (!depResult.isSuccess()) {
                throw new Exception("Dependency Check Failed: " + depResult.getErrorMessage());
            }
        }
        
        // Generate scenarios using the found method for class context
        TestScenario[] scenarios = generateTestScenarios(config, method);
        return runAnalysisWithScenarios(config, scenarios);
    }
    
    // Backward compatibility method
    public List<ExecutionResult> runAnalysis(AlgorithmConfig config, int[] inputSizes) throws Exception {
        System.out.println("[AnalysisEngine] Starting analysis for: " + config.getName());
        List<ExecutionResult> results = new ArrayList<>();
        
        // Calculate total steps: find implementation + create instance + (warmup + measurement) for each input size
        int totalSteps = 2 + inputSizes.length * (WARMUP_RUNS + MEASUREMENT_RUNS);
        int currentStep = 0;
        
        // Report analysis start
        if (progressCallback != null) {
            progressCallback.onAnalysisStart(totalSteps);
        }
        if (resultsCallback != null) {
            resultsCallback.onAnalysisStarted(config.getName(), inputSizes.length);
        }
        
        // Find the implementation
        System.out.println("[AnalysisEngine] Looking for implementation: " + config.getClassName());
        if (progressCallback != null) {
            progressCallback.updateProgress(currentStep++, totalSteps, "Finding algorithm implementation...");
        }
        
        Method method = findImplementation(config);
        if (method == null) {
            System.out.println("[AnalysisEngine] ERROR: Implementation not found!");
            if (progressCallback != null) {
                progressCallback.onAnalysisComplete(false);
            }
            if (resultsCallback != null) {
                resultsCallback.onAnalysisCompleted(config.getName(), new ArrayList<>(), false);
            }
            throw new RuntimeException(
                String.format("Cannot find your implementation of %s. Please confirm it exists in your project. " +
                             "If you continue to have issues, email jtbacher@ncsu.edu", 
                             config.getName())
            );
        }
        System.out.println("[AnalysisEngine] Found method: " + method.getName() + " with parameters: " + java.util.Arrays.toString(method.getParameterTypes()));
        
        // Create instance of the class
        if (progressCallback != null) {
            progressCallback.updateProgress(currentStep++, totalSteps, "Initializing algorithm instance...");
        }
        Object instance = createInstanceSafely(method.getDeclaringClass());
        
        // Run analysis for each input size
        for (int i = 0; i < inputSizes.length; i++) {
            int size = inputSizes[i];
            System.out.println("[AnalysisEngine] Testing input size: " + size);
            
            ExecutionResult result = measureExecution(method, instance, config, size, i + 1, inputSizes.length, currentStep, totalSteps);
            currentStep += WARMUP_RUNS + MEASUREMENT_RUNS; // Update step count
            
            results.add(result);
            System.out.println("[AnalysisEngine] Result for size " + size + ": " + (result.isSuccess() ? result.getExecutionTime() + "ms" : "FAILED - " + result.getError()));
            
            // Send progressive result update
            if (resultsCallback != null) {
                boolean isStillRunning = (i < inputSizes.length - 1); // Not the last input size
                resultsCallback.onResultAvailable(config.getName(), new ArrayList<>(results), result, isStillRunning);
            }
            
            // If we hit a timeout, skip larger sizes
            if (!result.isSuccess() && result.getError() != null && result.getError().contains("timeout")) {
                for (int j = inputSizes.length - 1; j >= 0; j--) {
                    if (inputSizes[j] > size) {
                        ExecutionResult skippedResult = new ExecutionResult(inputSizes[j], -1, false, "Skipped due to previous timeout");
                        results.add(skippedResult);
                        // Send update for skipped results too
                        if (resultsCallback != null) {
                            resultsCallback.onResultAvailable(config.getName(), new ArrayList<>(results), skippedResult, false);
                        }
                    }
                }
                break;
            }
        }
        
        // Report analysis completion
        if (progressCallback != null) {
            progressCallback.onAnalysisComplete(true);
        }
        if (resultsCallback != null) {
            resultsCallback.onAnalysisCompleted(config.getName(), results, true);
        }
        
        return results;
    }
    
    private Method findImplementation(AlgorithmConfig config) throws Exception {
        // Get all Java projects in workspace
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        
        for (IProject project : projects) {
            if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject javaProject = JavaCore.create(project);
                Method method = codeScanner.findMethod(javaProject, config.getClassName(), config.getMethodSignature());
                if (method != null) {
                    return method;
                }
            }
        }
        
        return null;
    }
    
    private ExecutionResult measureExecution(Method method, Object instance, AlgorithmConfig config, int inputSize, int sizeIndex, int totalSizes, int stepOffset, int totalSteps) {
        try {
            System.out.println("[AnalysisEngine] Generating test data for size: " + inputSize);
            // Generate test data
            Object[] args = generateTestData(config.getMethodSignature(), inputSize);
            System.out.println("[AnalysisEngine] Generated args: " + (args != null ? java.util.Arrays.toString(args) : "null"));
            
            // Warm up the JVM
            System.out.println("[AnalysisEngine] Starting warmup runs...");
            for (int i = 0; i < WARMUP_RUNS; i++) {
                if (progressCallback != null) {
                    progressCallback.updateProgress(stepOffset + i, totalSteps, 
                        String.format("Warmup run %d/%d for input size %d (%d/%d)", 
                        i + 1, WARMUP_RUNS, inputSize, sizeIndex, totalSizes));
                }
                
                Object[] warmupArgs = generateTestData(config.getMethodSignature(), Math.min(inputSize, 1000));
                try {
                    method.invoke(instance, warmupArgs);
                    System.out.println("[AnalysisEngine] Warmup run " + (i+1) + " completed");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Warmup run " + (i+1) + " failed: " + e.getMessage());
                }
            }
            
            // Perform measurements
            System.out.println("[AnalysisEngine] Starting measurement runs...");
            List<Long> measurements = new ArrayList<>();
            
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                if (progressCallback != null) {
                    progressCallback.updateProgress(stepOffset + WARMUP_RUNS + i, totalSteps, 
                        String.format("Measurement run %d/%d for input size %d (%d/%d)", 
                        i + 1, MEASUREMENT_RUNS, inputSize, sizeIndex, totalSizes));
                }
                
                System.out.println("[AnalysisEngine] Measurement run " + (i+1) + "/" + MEASUREMENT_RUNS);
                // Generate fresh data for each run
                args = generateTestData(config.getMethodSignature(), inputSize);
                
                // Final copy to allow for reference in a lambda expression
                final Object[] callArgs = args;
                
                // Measure execution time
                Future<Long> future = executor.submit(new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        long startTime = System.nanoTime();
                        method.invoke(instance, callArgs);
                        long endTime = System.nanoTime();
                        return (endTime - startTime) / 1_000; // Convert to microseconds
                    }
                });
                
                try {
                    long executionTime = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    // If execution was too fast to measure (0μs), record as 1μs minimum
                    if (executionTime == 0) {
                        measurements.add(1L); // Minimum 1 microsecond
                        System.out.println("[AnalysisEngine] Run " + (i+1) + " completed in <1μs (recorded as 1μs)");
                    } else {
                        measurements.add(executionTime); // Store microseconds directly
                        System.out.println("[AnalysisEngine] Run " + (i+1) + " completed in " + executionTime + "μs");
                    }
                } catch (TimeoutException e) {
                    System.out.println("[AnalysisEngine] TIMEOUT on run " + (i+1) + " for input size " + inputSize + " (timeout: " + (timeoutMs/1000) + "s)");
                    future.cancel(true);
                    // Return ExecutionResult with timeout flag set to true and execution time set to timeout duration
                    long timeoutTimeScaled = timeoutMs * 1000; // Convert timeout milliseconds to microseconds
                    return new ExecutionResult(inputSize, timeoutTimeScaled, false, 
                        String.format("Execution timeout (%ds) for input size %d. This might indicate an infinite loop or very inefficient algorithm.", 
                                    timeoutMs/1000, inputSize), true);
                }
            }
            
            // Use median of measurements
            measurements.sort(Long::compareTo);
            long medianTime = measurements.get(measurements.size() / 2);
            
            return new ExecutionResult(inputSize, medianTime, true);
            
        } catch (Exception e) {
            return new ExecutionResult(inputSize, -1, false, 
                "Runtime error: " + e.getCause().getMessage());
        }
    }
    
    private Object[] generateTestData(MethodSignature signature, int size) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == int[].class) {
                // Generate random integer array
                int[] array = new int[size];
                for (int j = 0; j < size; j++) {
                    array[j] = random.nextInt(size * 10);
                }
                args[i] = array;
            } else if (paramTypes[i] == int.class) {
                // Generate random target value for search
                args[i] = random.nextInt(size * 10);
            } else if (paramTypes[i] == String[].class) {
                // Generate random string array
                String[] array = new String[size];
                for (int j = 0; j < size; j++) {
                    array[j] = generateRandomString(random, 10);
                }
                args[i] = array;
            } else if (paramTypes[i] == double[].class) {
                // Generate random double array
                double[] array = new double[size];
                for (int j = 0; j < size; j++) {
                    array[j] = random.nextDouble() * size;
                }
                args[i] = array;
            } else if (paramTypes[i] == Comparable[].class) {
                // Generate random Integer array for Comparable
                // Use a wider range to reduce duplicates that can cause worst-case O(n²) behavior in QuickSort
                Integer[] array = new Integer[size];
                for (int j = 0; j < size; j++) {
                    array[j] = random.nextInt(size * 100); // Increased range to reduce duplicates
                }
                args[i] = array;
            }
            // Add more types as needed
        }
        
        return args;
    }
    
    private String generateRandomString(Random random, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
    
    /**
     * Find the algorithm method using CodeScanner
     */
    private Method findAlgorithmMethod(AlgorithmConfig config) throws Exception {
        CodeScanner scanner = new CodeScanner();
        
        // Get Java projects from workspace
        IJavaProject[] projects = org.eclipse.jdt.core.JavaCore.create(
            org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
        
        Method foundMethod = null;
        for (IJavaProject project : projects) {
            try {
                foundMethod = scanner.findMethod(project, config.getClassName(), config.getMethodSignature());
                if (foundMethod != null) {
                    break;
                }
            } catch (Exception e) {
                // Continue searching other projects
            }
        }
        
        if (foundMethod == null) {
            throw new Exception("Cannot find implementation for: " + config.getClassName() + "." + config.getMethodSignature().getMethodName());
        }
        
        return foundMethod;
    }
    
    /**
     * Generate test scenarios based on algorithm configuration
     */
    private TestScenario[] generateTestScenarios(AlgorithmConfig config, Method method) {
        AnalysisMode mode = config.getAnalysisMode();
        AnalysisParameter parameter = config.getAnalysisParameter();
        Object[] parameterValues = parameter.getDefaultValues();
        
        TestScenario[] scenarios = new TestScenario[parameterValues.length];
        
        switch (mode) {
            case TRADITIONAL_SCALING:
                return generateTraditionalScalingScenarios(config, (Integer[]) parameterValues);
            case POSITION_BASED:
                // For position-based scenarios, we need the algorithm class for pre-population
                return generatePositionBasedScenarios(config, (PositionIndexParameter) parameter, method.getDeclaringClass(), method);
            case LOAD_FACTOR:
                return generateLoadFactorScenarios(config, (LoadFactorParameter) parameter, method.getDeclaringClass(), method);
            case INPUT_SIZE_WITH_POSITIONS:
                return generateInputSizeWithPositionsScenarios(config, (InputSizeWithPositionsParameter) parameter, method.getDeclaringClass(), method);
            case INPUT_SIZE_WITH_COLLISIONS:
                return generateInputSizeWithCollisionsScenarios(config, (CollisionRateParameter) parameter, method.getDeclaringClass(), method);
            case STRUCTURE_SIZE:
            case CONSTANT_TIME:
                return generateStructureSizeScenarios(config, (Integer[]) parameterValues, method.getDeclaringClass(), method);
            default:
                throw new UnsupportedOperationException("Analysis mode not implemented: " + mode);
        }
    }
    
    private TestScenario[] generateTraditionalScalingScenarios(AlgorithmConfig config, Integer[] sizes) {
        TestScenario[] scenarios = new TestScenario[sizes.length];
        
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            Object[] args = generateTestData(config.getMethodSignature(), size);
            // Use consistent scenario name for single-line analysis (don't include specific size)
            scenarios[i] = new TestScenario(null, args, config.getName(), size);
        }
        
        return scenarios;
    }
    
    private TestScenario[] generatePositionBasedScenarios(AlgorithmConfig config, PositionIndexParameter parameter, Class<?> algorithmClass, Method method) {
        // For position-based analysis, we want to test across input sizes with 3 position series
        Integer[] inputSizes = {50, 75, 100, 150, 200, 300, 400, 500, 650, 800, 
                                1000, 1300, 1600, 2000, 2500, 3200, 4000, 5000, 6500, 8000, 
                                10000, 13000, 16000, 20000, 25000}; // 25 input sizes for better trend visualization
        String[] positionNames = {"Beginning", "Middle", "End"};
        
        // Create scenarios: inputSizes.length * positionNames.length 
        java.util.List<TestScenario> scenarioList = new java.util.ArrayList<>();
        
        System.out.println("[AnalysisEngine] Generating position-based scenarios for " + config.getName());
        System.out.println("[AnalysisEngine] Input sizes: " + java.util.Arrays.toString(inputSizes));
        System.out.println("[AnalysisEngine] Positions: " + java.util.Arrays.toString(positionNames));
        boolean decrementer = false;
        for (int size : inputSizes) {
            // Skip empty structures for position-based analysis
            if (size <= 0) continue;
            
            for (String positionName : positionNames) {
                // Calculate position index based on size
                int position;
                switch (positionName) {
                    case "Beginning":
                        position = 0;
                        decrementer = false;
                        break;
                    case "Middle": 
                        position = size / 2;
                        decrementer = false;
                        break;
                    case "End":
                        position = Math.max(0, size - 1); // Valid end index for existing list, minimum 0
                        decrementer = true;
                        break;
                    default:
                        position = 0;
                }
                
                // Pre-populate structure with 'size' elements
                Object prePopulatedStructure = createPrePopulatedStructure(config, size, algorithmClass, method);
                System.out.println("[AnalysisEngine] Pre-populated structure (size=" + size + ", position=" + positionName + ", positionid=" + position + "): " + 
                                 (prePopulatedStructure != null ? "SUCCESS" : "FAILED"));
                // Generate method arguments with the specific position
                Object[] args = generatePositionBasedArguments(config.getMethodSignature(), position);

                // Create scenario with input size as X-axis value and position as series identifier
                String scenarioDescription = positionName + " Position";
                TestScenario scenario = new TestScenario(prePopulatedStructure, args, scenarioDescription, size, decrementer);
                scenarioList.add(scenario);
            }
        }
        
        System.out.println("[AnalysisEngine] Generated " + scenarioList.size() + " scenarios for " + config.getName());
        return scenarioList.toArray(new TestScenario[0]);
    }
    
    private TestScenario[] generateLoadFactorScenarios(AlgorithmConfig config, LoadFactorParameter parameter, Class<?> algorithmClass, Method method) {
        Double[] loadFactors = (Double[]) parameter.getDefaultValues();
        TestScenario[] scenarios = new TestScenario[loadFactors.length];
        
        for (int i = 0; i < loadFactors.length; i++) {
            double loadFactor = loadFactors[i];
            int entries = parameter.getEntriesForLoadFactor(loadFactor);
            // Pre-populate hash map with calculated entries
            Object prePopulatedStructure = createPrePopulatedStructure(config, entries, algorithmClass, method);
            // Generate method arguments for hash operation
            Object[] args = generateHashMapArguments(config.getMethodSignature());
            scenarios[i] = new TestScenario(prePopulatedStructure, args, 
                "Load factor " + loadFactor, loadFactor);
        }
        
        return scenarios;
    }
    
    private TestScenario[] generateStructureSizeScenarios(AlgorithmConfig config, Integer[] sizes, Class<?> algorithmClass, Method method) {
        TestScenario[] scenarios = new TestScenario[sizes.length];
        
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            // Pre-populate structure with size elements
            Object prePopulatedStructure = createPrePopulatedStructure(config, size, algorithmClass, method);
            // Generate method arguments using the pre-populated structure
            Object[] args = generateStructureOperationArguments(config.getMethodSignature(), prePopulatedStructure);
            
            // For static methods, don't pass prePopulatedStructure as instance - pass null
            // The prePopulatedStructure should only be in the method arguments
            boolean isStaticMethod = config.getMethodSignature().getDisplaySignature().contains("static");
            Object instanceForScenario = isStaticMethod ? null : prePopulatedStructure;
            
            // Use consistent scenario name for single-line analysis (don't include specific size)
            scenarios[i] = new TestScenario(instanceForScenario, args, 
                config.getName(), size);
        }
        
        return scenarios;
    }
    
    /**
     * Run analysis with test scenarios
     */
    private List<ExecutionResult> runAnalysisWithScenarios(AlgorithmConfig config, TestScenario[] scenarios) throws Exception {
        System.out.println("[AnalysisEngine] Starting analysis for: " + config.getName());
        List<ExecutionResult> results = new ArrayList<>();
        
        // Calculate total steps
        int totalSteps = 2 + scenarios.length * (WARMUP_RUNS + MEASUREMENT_RUNS);
        int currentStep = 0;
        
        // Report analysis start
        if (progressCallback != null) {
            progressCallback.onAnalysisStart(totalSteps);
        }
        if (resultsCallback != null) {
            resultsCallback.onAnalysisStarted(config.getName(), scenarios.length);
        }
        
        // Find the implementation
        if (progressCallback != null) {
            progressCallback.updateProgress(currentStep++, totalSteps, "Finding algorithm implementation...");
        }
        Method method = findImplementation(config);
        if (method == null) {
            if (progressCallback != null) progressCallback.onAnalysisComplete(false);
            if (resultsCallback != null) resultsCallback.onAnalysisCompleted(config.getName(), new ArrayList<>(), false);
            throw new RuntimeException("Cannot find implementation: " + config.getName());
        }
        
        // Create instance
        if (progressCallback != null) {
            progressCallback.updateProgress(currentStep++, totalSteps, "Initializing algorithm instance...");
        }
        Object instance = createInstanceSafely(method.getDeclaringClass());
        
        // Run analysis for each scenario
        System.out.println("[AnalysisEngine] Running analysis for " + scenarios.length + " scenarios");
        for (int i = 0; i < scenarios.length; i++) {
            TestScenario scenario = scenarios[i];
            System.out.println("[AnalysisEngine] Running scenario " + (i+1) + "/" + scenarios.length + ": " + scenario.getScenarioDescription());
            ExecutionResult result = runScenario(method, instance, scenario, currentStep, totalSteps, i + 1, scenarios.length);
            results.add(result);
            currentStep += (WARMUP_RUNS + MEASUREMENT_RUNS);
            
            if (resultsCallback != null) {
                resultsCallback.onResultAvailable(config.getName(), results, result, i < scenarios.length - 1);
            }
        }
        
        if (progressCallback != null) {
            progressCallback.onAnalysisComplete(true);
        }
        if (resultsCallback != null) {
            resultsCallback.onAnalysisCompleted(config.getName(), results, true);
        }
        
        return results;
    }
    
    private ExecutionResult runScenario(Method method, Object instance, TestScenario scenario, 
                                       int stepOffset, int totalSteps, int scenarioIndex, int totalScenarios) {
        try {
            double xValue = scenario.getXAxisValue();
            System.out.println("[AnalysisEngine] Running scenario: " + scenario.getScenarioDescription() + " (X=" + xValue + ")");
            
            // Handle pre-populated structure differently due to classloader compatibility
            Object targetInstance;
            if (scenario.getPrePopulatedStructure() != null) {
                // Create a fresh instance using the same classloader as the method
                // and copy the state from the pre-populated structure
                targetInstance = recreateInstanceWithSameClassloader(method, scenario.getPrePopulatedStructure());
                System.out.println("[AnalysisEngine] Using recreated pre-populated instance");
            } else {
                targetInstance = instance;
                System.out.println("[AnalysisEngine] Using fresh instance");
            }
            
            
            
            final Object finalTargetInstance = targetInstance; // For use in callable
            
            // For methods that need structure-specific arguments (like Position objects), 
            // regenerate arguments using the recreated instance
            Object[] baseMethodArgs = scenario.getMethodArguments();
            if (scenario.getPrePopulatedStructure() != null && finalTargetInstance != scenario.getPrePopulatedStructure()) {
                AlgorithmConfig currentConfig = getCurrentAlgorithmConfigFromMethod(method);
                if (currentConfig != null) {
                    // Only regenerate arguments for methods that need complex object parameters (Position, Vertex, etc.)
                    // For simple types like int.class (position-based analysis), preserve the original arguments
                    Class<?>[] paramTypes = currentConfig.getMethodSignature().getParameterTypes();
                    boolean needsComplexRegeneration = false;
                    for (Class<?> paramType : paramTypes) {
                        if (paramType != int.class && paramType != Object.class && paramType != String.class) {
                            needsComplexRegeneration = true;
                            break;
                        }
                    }
                    
                    if (needsComplexRegeneration) {
                        baseMethodArgs = generateStructureOperationArguments(currentConfig.getMethodSignature(), finalTargetInstance);
                        System.out.println("[AnalysisEngine] Regenerated method arguments for recreated instance: " + java.util.Arrays.toString(baseMethodArgs));
                    } else {
                        System.out.println("[AnalysisEngine] Preserving original scenario arguments for simple parameter types: " + java.util.Arrays.toString(baseMethodArgs));
                    }
                }
            }
            
            final Object[] finalMethodArgs = baseMethodArgs;
            
            // Warmup runs - need fresh instance and arguments for each run for methods that modify structure
            for (int i = 0; i < WARMUP_RUNS; i++) {
                if (progressCallback != null) {
                    progressCallback.updateProgress(stepOffset + i, totalSteps, 
                        String.format("Warmup run %d/%d for %s (%d/%d)", 
                        i + 1, WARMUP_RUNS, scenario.getScenarioDescription(), scenarioIndex, totalScenarios));
                }
                
                // For each warmup run, recreate instance and arguments to avoid state pollution
                Object warmupTargetInstance = finalTargetInstance;
                Object[] warmupMethodArgs = finalMethodArgs;
                
                // If this method might modify the structure, recreate for each warmup run
                if (scenario.getPrePopulatedStructure() != null) {
                    AlgorithmConfig currentConfig = getCurrentAlgorithmConfigFromMethod(method);
                    if (currentConfig != null && (currentConfig.getAnalysisMode() == AnalysisMode.STRUCTURE_SIZE || 
                        currentConfig.getAnalysisMode() == AnalysisMode.CONSTANT_TIME)) {
                        // Recreate instance for each warmup run to avoid state pollution
                        warmupTargetInstance = recreateInstanceWithSameClassloader(method, scenario.getPrePopulatedStructure());
                        warmupMethodArgs = generateStructureOperationArguments(currentConfig.getMethodSignature(), warmupTargetInstance);
                        System.out.println("[AnalysisEngine] Warmup run " + (i+1) + " - recreated instance and arguments");
                    }
                }
                
                System.out.println("[AnalysisEngine] Method arguments: " + java.util.Arrays.toString(warmupMethodArgs));
                
                try {
                    System.out.println("[AnalysisEngine] About to invoke method: " + method.getName());
                    System.out.println("[AnalysisEngine] Target instance: " + (warmupTargetInstance != null ? warmupTargetInstance.getClass().getName() : "null"));
                    System.out.println("[AnalysisEngine] Method signature: " + method.toString());
                    System.out.println("[AnalysisEngine] Is static method: " + java.lang.reflect.Modifier.isStatic(method.getModifiers()));
                    System.out.println("[AnalysisEngine] Arguments count: " + warmupMethodArgs.length);
                    for (int argIndex = 0; argIndex < warmupMethodArgs.length; argIndex++) {
                        Object arg = warmupMethodArgs[argIndex];
                        System.out.println("[AnalysisEngine]   Arg[" + argIndex + "]: " + 
                            (arg != null ? arg.getClass().getName() + " = " + arg.toString() : "null"));
                    }
                    
                    // For static methods, pass null as the instance
                    Object targetForInvocation = java.lang.reflect.Modifier.isStatic(method.getModifiers()) ? null : warmupTargetInstance;
                    
                    try {
                    	 System.out.println("Method expects: " + Arrays.toString(method.getParameterTypes()));
                    	 System.out.println("We're providing: " + Arrays.toString(Arrays.stream(warmupMethodArgs).map(arg -> arg != null ?
                    	  arg.getClass() : null).toArray()));
                    	 if (method.getParameterTypes().length > 1 && warmupMethodArgs.length > 1) {
                    	     System.out.println("Param type classloader = " + method.getParameterTypes()[1].getClassLoader());
                    	     System.out.println("Arg type classloader   = " + warmupMethodArgs[1].getClass().getClassLoader());
                    	 }

                        Object result = method.invoke(targetForInvocation, warmupMethodArgs);
                        System.out.println("[AnalysisEngine] Method invocation succeeded, result: " + 
                            (result != null ? result.getClass().getName() + " = " + result.toString() : "null"));
                    } catch (java.lang.reflect.InvocationTargetException ite) {
                        // Check if this is an argument type mismatch - try alternative approaches
                        if (ite.getCause() != null && ite.getCause().getClass().getName().contains("IllegalArgumentException")) {
                            System.out.println("[AnalysisEngine] Warmup detected argument type mismatch, attempting alternative argument conversion");
                            try {
                                Object[] alternativeArgs = tryAlternativeArgumentConversion(method, warmupMethodArgs);
                                Object result = method.invoke(targetForInvocation, alternativeArgs);
                                System.out.println("[AnalysisEngine] Warmup alternative invocation succeeded");
                            } catch (Exception retryException) {
                                System.out.println("[AnalysisEngine] Warmup alternative approach also failed: " + retryException.getMessage());
                                throw ite; // Re-throw original exception
                            }
                        } else {
                            throw ite; // Re-throw original exception
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Warmup run " + (i+1) + " failed with exception: " + e.getClass().getName());
                    System.out.println("[AnalysisEngine] Exception message: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.out.println("[AnalysisEngine] Root cause: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                    }
                    e.printStackTrace();
                }
            }
            
            // Measurement runs
            List<Long> measurements = new ArrayList<>();
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                if (progressCallback != null) {
                    progressCallback.updateProgress(stepOffset + WARMUP_RUNS + i, totalSteps, 
                        String.format("Measurement run %d/%d for %s (%d/%d)", 
                        i + 1, MEASUREMENT_RUNS, scenario.getScenarioDescription(), scenarioIndex, totalScenarios));
                }
                
                // For each measurement run, recreate instance and arguments to avoid state pollution
                Object measurementTargetInstance = finalTargetInstance;
                Object[] measurementMethodArgs = finalMethodArgs;
                
                // If this method might modify the structure, recreate for each measurement run
                if (scenario.getPrePopulatedStructure() != null) {
                    AlgorithmConfig currentConfig = getCurrentAlgorithmConfigFromMethod(method);
                    if (currentConfig != null && (currentConfig.getAnalysisMode() == AnalysisMode.STRUCTURE_SIZE || 
                        currentConfig.getAnalysisMode() == AnalysisMode.CONSTANT_TIME)) {
                        // Recreate instance for each measurement run to avoid state pollution
                        measurementTargetInstance = recreateInstanceWithSameClassloader(method, scenario.getPrePopulatedStructure());
                        measurementMethodArgs = generateStructureOperationArguments(currentConfig.getMethodSignature(), measurementTargetInstance);
                        System.out.println("[AnalysisEngine] Measurement run " + (i+1) + " - recreated instance and arguments");
                    }
                }
                
                System.out.println("[AnalysisEngine] Method arguments: " + java.util.Arrays.toString(measurementMethodArgs));
                
                final Object finalMeasurementTargetInstance = measurementTargetInstance;
                final Object[] finalMeasurementMethodArgs = measurementMethodArgs;
                
                Future<Long> future = executor.submit(new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        System.out.println("[AnalysisEngine] Starting measurement execution");
                        System.out.println("[AnalysisEngine] Method: " + method.getName());
                        System.out.println("[AnalysisEngine] Target: " + (finalMeasurementTargetInstance != null ? finalMeasurementTargetInstance.getClass().getSimpleName() : "null"));
                        
                        long startTime = System.nanoTime();
                        try {
                            // For static methods, pass null as the instance
                            Object targetForInvocation = java.lang.reflect.Modifier.isStatic(method.getModifiers()) ? null : finalMeasurementTargetInstance;
                            Object result = method.invoke(targetForInvocation, finalMeasurementMethodArgs);
                            long endTime = System.nanoTime();
                            long duration = (endTime - startTime) / 1_000; // Convert to microseconds
                            System.out.println("[AnalysisEngine] Measurement execution succeeded in " + duration + "μs, result: " + 
                                (result != null ? result.getClass().getSimpleName() : "null"));
                            return duration;
                        } catch (java.lang.reflect.InvocationTargetException ite) {
                            // Check if this is an argument type mismatch - try alternative approaches
                            if (ite.getCause() != null && ite.getCause().getClass().getName().contains("IllegalArgumentException")) {
                                System.out.println("[AnalysisEngine] Detected argument type mismatch, attempting alternative argument conversion");
                                try {
                                    Object[] alternativeArgs = tryAlternativeArgumentConversion(method, finalMeasurementMethodArgs);
                                    Object targetForInvocation = java.lang.reflect.Modifier.isStatic(method.getModifiers()) ? null : finalMeasurementTargetInstance;
                                    Object result = method.invoke(targetForInvocation, alternativeArgs);
                                    long endTime = System.nanoTime();
                                    long duration = (endTime - startTime) / 1_000; // Convert to microseconds
                                    System.out.println("[AnalysisEngine] Alternative invocation succeeded in " + duration + "μs");
                                    return duration;
                                } catch (Exception retryException) {
                                    System.out.println("[AnalysisEngine] Alternative approach also failed: " + retryException.getMessage());
                                }
                            }
                            throw ite; // Re-throw original exception
                        } catch (Exception e) {
                            long endTime = System.nanoTime();
                            long duration = (endTime - startTime) / 1_000;
                            System.out.println("[AnalysisEngine] Measurement execution failed after " + duration + "μs");
                            System.out.println("[AnalysisEngine] Measurement exception: " + e.getClass().getName() + " - " + e.getMessage());
                            if (e.getCause() != null) {
                                System.out.println("[AnalysisEngine] Measurement root cause: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                            }
                            throw e; // Re-throw to indicate failure
                        }
                    }
                });
                
                try {
                    System.out.println("[AnalysisEngine] Waiting for measurement result with timeout: " + timeoutMs + "ms");
                    long executionTime = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    if (executionTime == 0) {
                        measurements.add(1L); // Minimum 1 microsecond
                        System.out.println("[AnalysisEngine] Measurement completed in <1μs (recorded as 1μs)");
                    } else {
                        measurements.add(executionTime); // Store microseconds directly
                        System.out.println("[AnalysisEngine] Measurement completed in " + executionTime + "μs");
                    }
                } catch (TimeoutException e) {
                    System.out.println("[AnalysisEngine] Measurement timed out after " + timeoutMs + "ms");
                    future.cancel(true);
                    long timeoutTimeScaled = timeoutMs * 1000; // Convert timeout milliseconds to microseconds
                    return new ExecutionResult(xValue, timeoutTimeScaled, false, 
                        String.format("Execution timeout (%ds) for %s", timeoutMs/1000, scenario.getScenarioDescription()), true, scenario.getScenarioDescription());
                }
            }
            
            // Use median of measurements
            measurements.sort(Long::compareTo);
            long medianTime = measurements.get(measurements.size() / 2);
            
            return new ExecutionResult(xValue, medianTime, true, null, false, scenario.getScenarioDescription());
            
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] SCENARIO EXECUTION FAILED with exception: " + e.getClass().getName());
            System.out.println("[AnalysisEngine] Exception message: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("[AnalysisEngine] Root cause: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            } else {
                System.out.println("[AnalysisEngine] No root cause available");
                e.printStackTrace();
            }
            // Build a more descriptive error message
            String errorMessage = "Runtime error: ";
            
            // Special handling for InvocationTargetException to unwrap the real cause
            Throwable actualCause = e;
            if (e instanceof java.lang.reflect.InvocationTargetException) {
                actualCause = e.getCause() != null ? e.getCause() : e;
            }
            
            if (actualCause.getCause() != null) {
                String causeMessage = actualCause.getCause().getMessage();
                errorMessage += (causeMessage != null && !causeMessage.trim().isEmpty()) ? 
                    causeMessage : actualCause.getCause().getClass().getSimpleName();
            } else {
                String exceptionMessage = actualCause.getMessage();
                errorMessage += (exceptionMessage != null && !exceptionMessage.trim().isEmpty()) ? 
                    exceptionMessage : actualCause.getClass().getSimpleName();
            }
            
            return new ExecutionResult(scenario.getXAxisValue(), -1, false, errorMessage, false, scenario.getScenarioDescription());
        }
    }
    
    /**
     * Recreate an instance using the same classloader as the method to avoid classloader compatibility issues
     */
    private Object recreateInstanceWithSameClassloader(Method method, Object prePopulatedInstance) {
        try {
            // Create a new instance using the method's declaring class (same classloader)
            Class<?> methodClass = method.getDeclaringClass();
            Object newInstance = createInstanceSafely(methodClass);
            
            // Copy the state by using the same pre-population logic
            String className = methodClass.getName();
            if (className.contains("ArrayBasedList")) {
                // For ArrayBasedList, get the size and elements from the pre-populated instance
                Method sizeMethod = prePopulatedInstance.getClass().getMethod("size");
                int size = (Integer) sizeMethod.invoke(prePopulatedInstance);
                
                Method getMethod = prePopulatedInstance.getClass().getMethod("get", int.class);
                Method addMethod = methodClass.getMethod("add", int.class, Object.class);
                
                // Copy all elements from pre-populated instance to new instance
                for (int i = 0; i < size; i++) {
                    Object element = getMethod.invoke(prePopulatedInstance, i);
                    addMethod.invoke(newInstance, i, element);
                }
                
                System.out.println("[AnalysisEngine] Recreated ArrayBasedList with " + size + " elements");
            } else if (className.contains("LinkedBinaryTree")) {
                // For LinkedBinaryTree, copy the tree structure
                try {
                    Method rootMethod = prePopulatedInstance.getClass().getMethod("root");
                    Object originalRoot = rootMethod.invoke(prePopulatedInstance);
                    
                    if (originalRoot != null) {
                        // Copy the tree structure recursively
                        copyTreeStructure(prePopulatedInstance, newInstance, originalRoot, methodClass);
                        System.out.println("[AnalysisEngine] Recreated LinkedBinaryTree with tree structure");
                    } else {
                        System.out.println("[AnalysisEngine] Original tree was empty, recreated as empty tree");
                    }
                } catch (Exception treeException) {
                    System.out.println("[AnalysisEngine] Failed to copy tree structure: " + treeException.getMessage());
                    // Create at least a root for fallback
                    try {
                        Method addRootMethod = methodClass.getMethod("addRoot", Object.class);
                        addRootMethod.invoke(newInstance, "root");
                        System.out.println("[AnalysisEngine] Created fallback root for tree");
                    } catch (Exception rootException) {
                        System.out.println("[AnalysisEngine] Failed to create fallback root: " + rootException.getMessage());
                    }
                }
            } else if (className.contains("SinglyLinkedList")) {
                // For SinglyLinkedList, copy all elements
                try {
                    Method sizeMethod = prePopulatedInstance.getClass().getMethod("size");
                    int size = (Integer) sizeMethod.invoke(prePopulatedInstance);
                    
                    Method getMethod = prePopulatedInstance.getClass().getMethod("get", int.class);
                    Method addMethod = methodClass.getMethod("add", int.class, Object.class); // Correct signature!
                    
                    // Copy all elements from pre-populated instance
                    for (int i = 0; i < size; i++) {
                        Object element = getMethod.invoke(prePopulatedInstance, i);
                        addMethod.invoke(newInstance, i, element); // Add at correct position
                    }
                    System.out.println("[AnalysisEngine] Recreated SinglyLinkedList with " + size + " elements");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Failed to copy SinglyLinkedList: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (className.contains("ArrayBasedQueue")) {
                // For ArrayBasedQueue, copy all elements using enqueue
                try {
                    Method sizeMethod = prePopulatedInstance.getClass().getMethod("size");
                    int size = (Integer) sizeMethod.invoke(prePopulatedInstance);
                    
                    Method enqueueMethod = methodClass.getMethod("enqueue", Object.class);
                    
                    // Pre-populate the new queue with same number of elements
                    for (int i = 0; i < size; i++) {
                        enqueueMethod.invoke(newInstance, "queue-element-" + i);
                    }
                    System.out.println("[AnalysisEngine] Recreated ArrayBasedQueue with " + size + " elements");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Failed to copy ArrayBasedQueue: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (className.contains("LinkedStack")) {
                // For LinkedStack, we can't easily copy the stack state, so create a basic populated stack
                try {
                    Method pushMethod = methodClass.getMethod("push", Object.class);
                    // Add a few elements to make the stack non-empty
                    for (int i = 0; i < 5; i++) {
                        pushMethod.invoke(newInstance, "stack-element-" + i);
                    }
                    System.out.println("[AnalysisEngine] Recreated LinkedStack with basic elements");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Failed to populate LinkedStack: " + e.getMessage());
                }
            } else if (className.contains("Graph")) {
                // Graph structures are temporarily disabled
                System.out.println("[AnalysisEngine] WARNING: Graph structure recreation disabled");
                throw new RuntimeException("Graph structures (Workshops 11 & 12) are temporarily disabled.");
            } else if (className.contains("PriorityQueue")) {
                // For PriorityQueue, add basic entries
                try {
                    Method insertMethod = methodClass.getMethod("insert", Object.class, Object.class);
                    for (int i = 0; i < 5; i++) {
                        insertMethod.invoke(newInstance, i, "priority-element-" + i);
                    }
                    System.out.println("[AnalysisEngine] Recreated PriorityQueue with basic entries");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Failed to populate PriorityQueue: " + e.getMessage());
                }
            } else if (className.contains("DisjointSet")) {
                // For DisjointSet, create basic structure with some elements
                try {
                    Method makeSetMethod = methodClass.getMethod("makeSet", Object.class);
                    for (int i = 0; i < 5; i++) {
                        makeSetMethod.invoke(newInstance, "set-element-" + i);
                    }
                    System.out.println("[AnalysisEngine] Recreated DisjointSet with basic sets");
                } catch (Exception e) {
                    System.out.println("[AnalysisEngine] Failed to populate DisjointSet: " + e.getMessage());
                }
            }
            // Add other class types as needed
            
            return newInstance;
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] Failed to recreate instance: " + e.getMessage());
            e.printStackTrace();
            // Fall back to creating a fresh instance  
            try {
                return createInstanceSafely(method.getDeclaringClass());
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
    /**
     * Recursively copy tree structure from original tree to new tree
     */
    private void copyTreeStructure(Object originalTree, Object newTree, Object originalRoot, Class<?> treeClass) throws Exception {
        // Get the element from the original root
        Method elementMethod = originalRoot.getClass().getMethod("element");
        Object rootElement = elementMethod.invoke(originalRoot);
        
        // Add root to new tree
        Method addRootMethod = treeClass.getMethod("addRoot", Object.class);
        Object newRoot = addRootMethod.invoke(newTree, rootElement);
        
        // Recursively copy children
        copyTreeChildren(originalTree, newTree, originalRoot, newRoot, treeClass);
    }
    
    /**
     * Recursively copy children of a tree node
     */
    private void copyTreeChildren(Object originalTree, Object newTree, Object originalParent, Object newParent, Class<?> treeClass) throws Exception {
        try {
            // Get left child
            Method leftMethod = originalParent.getClass().getMethod("left");
            Object originalLeft = leftMethod.invoke(originalParent);
            
            if (originalLeft != null) {
                Method elementMethod = originalLeft.getClass().getMethod("element");
                Object leftElement = elementMethod.invoke(originalLeft);
                
                Method addLeftMethod = treeClass.getMethod("addLeft", Object.class, Object.class);
                Object newLeft = addLeftMethod.invoke(newTree, newParent, leftElement);
                
                // Recursively copy children of left child
                copyTreeChildren(originalTree, newTree, originalLeft, newLeft, treeClass);
            }
        } catch (Exception e) {
            // Left child doesn't exist or method failed, continue
        }
        
        try {
            // Get right child
            Method rightMethod = originalParent.getClass().getMethod("right");
            Object originalRight = rightMethod.invoke(originalParent);
            
            if (originalRight != null) {
                Method elementMethod = originalRight.getClass().getMethod("element");
                Object rightElement = elementMethod.invoke(originalRight);
                
                Method addRightMethod = treeClass.getMethod("addRight", Object.class, Object.class);
                Object newRight = addRightMethod.invoke(newTree, newParent, rightElement);
                
                // Recursively copy children of right child
                copyTreeChildren(originalTree, newTree, originalRight, newRight, treeClass);
            }
        } catch (Exception e) {
            // Right child doesn't exist or method failed, continue
        }
    }
    
    // Helper methods for generating different types of structures and arguments
    private Object createPrePopulatedStructure(AlgorithmConfig config, int size, Class<?> algorithmClass, Method method) {
        try {
            String className = algorithmClass.getName();
            System.out.println("[AnalysisEngine] Pre-populating " + className + " with " + size + " elements");
            
            // Graph algorithms (Workshops 11 & 12) are temporarily disabled due to classloader issues
            if (className.contains("GraphTraversalUtil") || className.contains("ShortestPathUtil") || className.contains("MinimumSpanningTreeUtil")) {
                System.out.println("[AnalysisEngine] WARNING: Graph algorithms are temporarily disabled");
                throw new RuntimeException("Graph algorithms (Workshops 11 & 12) are temporarily disabled. Please try other algorithms.");
            }

            
            // Create instance of the algorithm class using the proper classloader
            Object instance = createInstanceSafely(algorithmClass);
            
            if (className.contains("ArrayBasedList") || className.contains("SinglyLinkedList")) {
                // Pre-populate list with elements by adding at the end each time
                Method addMethod = algorithmClass.getMethod("add", int.class, Object.class);
                
                // For pre-population, always add at the end (current size index) to avoid O(n) cost
                for (int i = 0; i < size; i++) {
                    addMethod.invoke(instance, i, "element-" + i); // Add at position i (which is the current size)
                }
                System.out.println("[AnalysisEngine] Successfully pre-populated " + className + " with " + size + " elements");
            } else if (className.contains("PositionalLinkedList")) {
                // Pre-populate positional list
                Method addFirstMethod = algorithmClass.getMethod("addFirst", Object.class);
                for (int i = 0; i < size; i++) {
                    addFirstMethod.invoke(instance, "element-" + i);
                }
            } else if (className.contains("Stack")) {
                // Pre-populate stack
                Method pushMethod = algorithmClass.getMethod("push", Object.class);
                for (int i = 0; i < size; i++) {
                    pushMethod.invoke(instance, "element-" + i);
                }
            } else if (className.contains("Queue")) {
                // Pre-populate queue
                Method enqueueMethod = algorithmClass.getMethod("enqueue", Object.class);
                for (int i = 0; i < size; i++) {
                    enqueueMethod.invoke(instance, "element-" + i);
                }
            } else if (className.contains("Map")) {
                // Pre-populate map
                Method putMethod = algorithmClass.getMethod("put", Object.class, Object.class);
                for (int i = 0; i < size; i++) {
                    putMethod.invoke(instance, "key-" + i, "value-" + i);
                }
            } else if (className.contains("Set")) {
                // Pre-populate set
                Method addMethod = algorithmClass.getMethod("add", Object.class);
                for (int i = 0; i < size; i++) {
                    addMethod.invoke(instance, "element-" + i);
                }
            } else if (className.contains("LinkedBinaryTree")) {
                // Pre-populate binary tree with a simple structure
                Method addRootMethod = algorithmClass.getMethod("addRoot", Object.class);
                Object root = addRootMethod.invoke(instance, "root");
                
                // Add children to create a tree of the desired size
                Object currentParent = root;
                for (int i = 1; i < size && i < 15; i++) { // Limit to reasonable tree size
                    try {
                        if (i % 2 == 1) {
                            // Add left child
                            Method addLeftMethod = algorithmClass.getMethod("addLeft", Object.class, Object.class);
                            Object leftChild = addLeftMethod.invoke(instance, currentParent, "element-" + i);
                            if (i == 1) currentParent = leftChild; // Use first left child as next parent
                        } else {
                            // Add right child  
                            Method addRightMethod = algorithmClass.getMethod("addRight", Object.class, Object.class);
                            addRightMethod.invoke(instance, currentParent, "element-" + i);
                        }
                    } catch (Exception treeException) {
                        System.out.println("[AnalysisEngine] Warning adding tree node " + i + ": " + treeException.getMessage());
                        break;
                    }
                }
                System.out.println("[AnalysisEngine] Successfully pre-populated LinkedBinaryTree with root and " + (size-1) + " additional nodes");
            } else if (className.contains("Tree")) {
                // Pre-populate other trees (like TreeMap)
                Method putMethod = algorithmClass.getMethod("put", Object.class, Object.class);
                for (int i = 0; i < size; i++) {
                    putMethod.invoke(instance, i, "value-" + i);
                }
            } else if (className.contains("PriorityQueue")) {
                // Pre-populate priority queue
                Method insertMethod = algorithmClass.getMethod("insert", Object.class, Object.class);
                for (int i = 0; i < size; i++) {
                    insertMethod.invoke(instance, i, "element-" + i);
                }
            } else if (className.contains("Graph")) {
                // Graph structures (Workshops 11 & 12) are temporarily disabled
                System.out.println("[AnalysisEngine] WARNING: Graph structures are temporarily disabled");
                throw new RuntimeException("Graph structures (Workshops 11 & 12) are temporarily disabled. Please try other algorithms.");
            } else if (className.contains("UpTreeDisjointSetForest")) {
                // Pre-populate disjoint set with individual sets
                Method makeSetMethod = algorithmClass.getMethod("makeSet", Object.class);
                for (int i = 0; i < size; i++) {
                    makeSetMethod.invoke(instance, "element-" + i);
                }
                System.out.println("[AnalysisEngine] Successfully pre-populated UpTreeDisjointSetForest with " + size + " disjoint sets");
            }
            
            return instance;
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] Failed to create pre-populated structure for " + config.getClassName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    
    private Object[] generatePositionBasedArguments(MethodSignature signature, int position) {
        // Generate arguments with specific position
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == int.class) {
                args[i] = position; // Use the specific position
            } else if (paramTypes[i] == Object.class) {
                args[i] = "test-element"; // Generic test element
            }
        }
        
        return args;
    }
    
    private Object[] generateHashMapArguments(MethodSignature signature) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        Random random = new Random(42);
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Object.class) {
                args[i] = "test-key-" + random.nextInt(1000);
            }
        }
        
        return args;
    }
    
    private Object[] generateSingleOperationArguments(MethodSignature signature) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Object.class) {
                args[i] = "test-element";
            } else if (paramTypes[i] == int.class) {
                args[i] = 42;
            }
        }
        
        return args;
    }
    
    private Object[] generateStructureOperationArguments(MethodSignature signature, Object prePopulatedStructure) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        String displaySignature = signature.getDisplaySignature();
        String methodName = signature.getMethodName();
        
        System.out.println("[AnalysisEngine] Generating arguments for method: " + methodName);
        System.out.println("[AnalysisEngine] Display signature: " + displaySignature);
        System.out.println("[AnalysisEngine] Parameter types: " + java.util.Arrays.toString(paramTypes));
        System.out.println("[AnalysisEngine] Pre-populated structure type: " + (prePopulatedStructure != null ? prePopulatedStructure.getClass().getName() : "null"));
        
        // Handle special cases based on method signature patterns
        // Check method names first, then fall back to signature pattern matching
        if (methodName.equals("depthFirstSearch") || methodName.equals("breadthFirstSearch") ||
            methodName.equals("dijkstra") || methodName.equals("kruskal") || methodName.equals("primJarnik")) {
            // Graph algorithms are temporarily disabled
            System.out.println("[AnalysisEngine] ERROR: Graph algorithms are temporarily disabled");
            throw new RuntimeException("Graph algorithms (Workshops 11 & 12) are temporarily disabled. Please try other algorithms.");
        } else if (methodName.equals("union")) {
            // Disjoint set union method is temporarily disabled
            System.out.println("[AnalysisEngine] ERROR: union() method is temporarily disabled");
            throw new RuntimeException("UpTreeDisjointSetForest.union() is temporarily disabled due to classloader issues. Please try other algorithms.");
        } else if (displaySignature.contains("Position")) {
            // Tree operations: addLeft(Position, E), remove(Position)
            if (paramTypes.length == 2) {
                // addLeft(Position, E) 
                args[0] = getRandomPositionFromTree(prePopulatedStructure); // Valid position from tree
                args[1] = "test-element"; // Element to add
            } else if (paramTypes.length == 1) {
                // remove(Position)
                args[0] = getRandomPositionFromTree(prePopulatedStructure); // Valid position from tree
            }
        } else if (methodName.equals("add") && paramTypes.length == 2 && paramTypes[0] == int.class) {
            // List add operation: add(int index, E element)
            System.out.println("[AnalysisEngine] Detected list add(index, element) method");
            args[0] = 0; // Index position (will be overridden by scenario)
            args[1] = "test-element"; // Element to add
        } else if (methodName.equals("get") && paramTypes.length == 1 && paramTypes[0] == int.class) {
            // List get operation: get(int index)
            System.out.println("[AnalysisEngine] Detected list get(index) method");
            args[0] = 0; // Index position (will be overridden by scenario)
        } else if (methodName.equals("remove") && paramTypes.length == 1 && paramTypes[0] == int.class) {
            // List remove operation: remove(int index)
            System.out.println("[AnalysisEngine] Detected list remove(index) method");
            args[0] = 0; // Index position (will be overridden by scenario)
        } else if (methodName.equals("insertVertex") || methodName.equals("insertEdge") || methodName.equals("getEdge")) {
            // Graph operations are temporarily disabled
            System.out.println("[AnalysisEngine] ERROR: Graph operations are temporarily disabled");
            throw new RuntimeException("Graph operations (Workshops 11 & 12) are temporarily disabled. Please try other algorithms.");
        } else if (methodName.equals("dequeue") && paramTypes.length == 0) {
            // Queue dequeue operation: dequeue()
            System.out.println("[AnalysisEngine] Detected queue dequeue method - no arguments needed");
            // No arguments needed, but make sure queue is pre-populated
            if (prePopulatedStructure == null) {
                System.out.println("[AnalysisEngine] WARNING: dequeue method has null pre-populated structure");
            }
        } else {
            // Default handling - first Object.class gets the structure
            boolean structureAssigned = false;
            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i] == Object.class && !structureAssigned) {
                    args[i] = prePopulatedStructure;
                    structureAssigned = true;
                } else if (paramTypes[i] == Object.class) {
                    args[i] = "test-element";
                } else if (paramTypes[i] == int.class) {
                    args[i] = 42;
                } else {
                    args[i] = "test-element";
                }
            }
        }
        
        System.out.println("[AnalysisEngine] Generated arguments: " + java.util.Arrays.toString(args));
        for (int i = 0; i < args.length; i++) {
            System.out.println("[AnalysisEngine]   Arg[" + i + "]: " + (args[i] != null ? args[i].getClass().getName() + " = " + args[i].toString() : "null"));
        }
        
        return args;
    }
    
    // Graph-related helper methods removed - Workshops 11 & 12 temporarily disabled


    
    // Disjoint set helper methods removed - union() method temporarily disabled
    
    /**
     * Gets a suitable position from a pre-populated tree for use in tree operations
     */
    private Object getRandomPositionFromTree(Object tree) {
        try {
            System.out.println("[AnalysisEngine] Attempting to extract position from tree: " + tree.getClass().getName());
            
            // First try to get root
            Method rootMethod = tree.getClass().getMethod("root");
            Object root = rootMethod.invoke(tree);
            System.out.println("[AnalysisEngine] Root method returned: " + root);
            
            if (root != null) {
                // For addLeft operations, we need a position that doesn't have a left child
                // Try to find a leaf or create a fresh position for addLeft
                Object suitablePosition = findPositionForAddLeft(tree, root);
                if (suitablePosition != null) {
                    System.out.println("[AnalysisEngine] Found suitable position for addLeft: " + suitablePosition.getClass().getName());
                    return suitablePosition;
                }
                
                // Fallback to root if we can't find a better position
                System.out.println("[AnalysisEngine] Using root position as fallback: " + root.getClass().getName());
                return root;
            }
            
            // If root is null, tree might be empty. Try to add an element first.
            System.out.println("[AnalysisEngine] Root is null, attempting to add root element");
            try {
                Method addRootMethod = tree.getClass().getMethod("addRoot", Object.class);
                Object newRoot = addRootMethod.invoke(tree, "root-element");
                if (newRoot != null) {
                    System.out.println("[AnalysisEngine] Successfully created and extracted root position: " + newRoot.getClass().getName());
                    return newRoot;
                }
            } catch (Exception addException) {
                System.out.println("[AnalysisEngine] Could not add root: " + addException.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] Warning: Could not extract position from tree: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Fallback: return a generic test element  
        System.out.println("[AnalysisEngine] Falling back to test-position string");
        return "test-position";
    }
    
    /**
     * Find a position suitable for addLeft operation (one that doesn't already have a left child)
     */
    private Object findPositionForAddLeft(Object tree, Object startPosition) {
        try {
            // Check if the current position has a left child
            Method leftMethod = startPosition.getClass().getMethod("left");
            Object leftChild = leftMethod.invoke(startPosition);
            
            if (leftChild == null) {
                // This position doesn't have a left child, perfect for addLeft
                System.out.println("[AnalysisEngine] Found position without left child for addLeft");
                return startPosition;
            }
            
            // This position has a left child, try to find a different one
            // Check if it has a right child we can use instead
            Method rightMethod = startPosition.getClass().getMethod("right");
            Object rightChild = rightMethod.invoke(startPosition);
            
            if (rightChild != null) {
                // Recursively search the right subtree for a position without left child
                Object suitableFromRight = findPositionForAddLeft(tree, rightChild);
                if (suitableFromRight != null) {
                    return suitableFromRight;
                }
            }
            
            // If left child exists, search it too
            Object suitableFromLeft = findPositionForAddLeft(tree, leftChild);
            if (suitableFromLeft != null) {
                return suitableFromLeft;
            }
            
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] Error searching for suitable position: " + e.getMessage());
        }
        
        return null; // No suitable position found
    }
    
    
    /**
     * Helper method to get the current algorithm config from a method signature
     */
    private AlgorithmConfig getCurrentAlgorithmConfigFromMethod(Method method) {
        // This is a simplified lookup - in practice, you might need to store the current config
        // For now, we'll create a basic config to use the signature
        try {
            String methodName = method.getName();
            String className = method.getDeclaringClass().getName();
            
            // Create a basic method signature for argument regeneration
            Class<?>[] paramTypes = method.getParameterTypes();
            MethodSignature signature = new MethodSignature(methodName, paramTypes, method.getReturnType(), method.toString());
            
            // Return a minimal config just for argument generation
            return new AlgorithmConfig(
                className + "." + methodName + "()",
                className,
                "Current Analysis",
                signature,
                AnalysisMode.STRUCTURE_SIZE,
                new StructureSizeParameter(),
                "O(1)",
                "Generated config for argument regeneration"
            );
        } catch (Exception e) {
            System.out.println("[AnalysisEngine] Warning: Could not create config for method " + method.getName());
            return null;
        }
    }
    
    private TestScenario[] generateInputSizeWithPositionsScenarios(AlgorithmConfig config, InputSizeWithPositionsParameter parameter, Class<?> algorithmClass, Method method) {
        Object[] inputSizes = parameter.getDefaultValues();
        String[] positions = InputSizeWithPositionsParameter.Position.getAllPositions();
        
        // Create scenarios for each input size * position combination
        List<TestScenario> scenarios = new ArrayList<>();
        
        for (Object sizeObj : inputSizes) {
            Integer size = (Integer) sizeObj;
            for (String position : positions) {
                int positionIndex = InputSizeWithPositionsParameter.Position.getIndex(position, size);
                String scenarioName = position + " Position"; // Use only position name for proper multi-line grouping
                
                // Create pre-populated structure for position-based testing
                Object prePopulatedStructure = createPrePopulatedStructure(config, size, algorithmClass, method);
                // Create test scenario with position-specific data
                Object[] args = generatePositionSearchArguments(config.getMethodSignature(), size, positionIndex);
                scenarios.add(new TestScenario(prePopulatedStructure, args, scenarioName, size.doubleValue()));
            }
        }
        
        return scenarios.toArray(new TestScenario[0]);
    }
    
    private TestScenario[] generateInputSizeWithCollisionsScenarios(AlgorithmConfig config, CollisionRateParameter parameter, Class<?> algorithmClass, Method method) {
        Object[] inputSizes = parameter.getDefaultValues();
        String[] scenarios = CollisionRateParameter.CollisionScenario.getAllScenarios();
        
        // Create scenarios for each input size * collision scenario combination
        List<TestScenario> testScenarios = new ArrayList<>();
        
        for (Object sizeObj : inputSizes) {
            Integer size = (Integer) sizeObj;
            for (String scenario : scenarios) {
                int buckets = CollisionRateParameter.CollisionScenario.getBucketsForScenario(scenario, size, parameter.getBaseBuckets());
                String scenarioName = scenario; // Use only collision scenario name for proper multi-line grouping
                
                // Create pre-populated structure for collision testing  
                Object prePopulatedStructure = createPrePopulatedStructure(config, size, algorithmClass, method);
                // Create test scenario with collision-specific setup
                Object[] args = generateCollisionHashArguments(config.getMethodSignature(), size, buckets);
                testScenarios.add(new TestScenario(prePopulatedStructure, args, scenarioName, size.doubleValue()));
            }
        }
        
        return testScenarios.toArray(new TestScenario[0]);
    }
    
    private Object[] generatePositionSearchArguments(MethodSignature signature, int arraySize, int targetPosition) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Object.class) {
                // For search operations, generate a key that will be at the specified position
                args[i] = "target-key-" + targetPosition;
            } else if (paramTypes[i] == int.class) {
                // Generate target value that would be at the specified position
                args[i] = targetPosition * 10; // Simple mapping
            } else if (paramTypes[i] == int[].class) {
                // Generate sorted array with target at specified position
                int[] array = new int[arraySize];
                for (int j = 0; j < arraySize; j++) {
                    array[j] = j * 10; // Sorted array
                }
                args[i] = array;
            }
        }
        
        return args;
    }
    
    private Object[] generateCollisionHashArguments(MethodSignature signature, int dataSize, int buckets) {
        Class<?>[] paramTypes = signature.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Object.class) {
                // Generate keys that will create appropriate collision patterns
                // This is a simplified approach - in practice, you'd want to pre-populate the hash structure
                args[i] = "hash-key-" + random.nextInt(dataSize);
            }
        }
        
        return args;
    }
    
    /**
     * Attempts alternative argument conversion strategies for methods that fail with argument type mismatch
     */
    private Object[] tryAlternativeArgumentConversion(Method method, Object[] originalArgs) {
        System.out.println("[AnalysisEngine] Trying alternative argument conversion for method: " + method.getName());
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] newArgs = new Object[originalArgs.length];
        
        // Log the expected vs actual types
        for (int i = 0; i < originalArgs.length && i < parameterTypes.length; i++) {
            System.out.println("[AnalysisEngine] Param[" + i + "] expected: " + parameterTypes[i].getName() + 
                             ", actual: " + (originalArgs[i] != null ? originalArgs[i].getClass().getName() : "null"));
        }
        
        // Graph-related conversion strategies removed - Workshops 11 & 12 temporarily disabled
        
        // union() method is temporarily disabled due to classloader issues
        
        // Default: return original arguments (will likely still fail)
        System.out.println("[AnalysisEngine] No specific conversion strategy available");
        return originalArgs;
    }
    
    // Graph-related conversion methods removed - Workshops 11 & 12 temporarily disabled
    
    // Union argument conversion methods removed - union() method temporarily disabled
    
    /**
     * Creates an instance of the given class with fallback constructor handling.
     * Tries default constructor first, then attempts other constructors with sensible defaults.
     */
    private Object createInstanceSafely(Class<?> clazz) throws Exception {
        try {
            // First try: default (no-arg) constructor
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            System.out.println("[AnalysisEngine] No default constructor found for " + clazz.getName() + ", trying alternatives...");
            
            // Try to find constructors and use sensible defaults
            java.lang.reflect.Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (java.lang.reflect.Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                
                // Try constructors with 1-3 parameters using sensible defaults
                if (paramTypes.length <= 3) {
                    Object[] args = new Object[paramTypes.length];
                    
                    boolean canConstruct = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                            args[i] = 10; // Default size/capacity
                        } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                            args[i] = false;
                        } else if (paramTypes[i] == double.class || paramTypes[i] == Double.class) {
                            args[i] = 1.0;
                        } else if (paramTypes[i] == String.class) {
                            args[i] = "default";
                        } else {
                            // Can't handle this parameter type, skip this constructor
                            canConstruct = false;
                            break;
                        }
                    }
                    
                    if (canConstruct) {
                        try {
                            constructor.setAccessible(true);
                            System.out.println("[AnalysisEngine] Successfully created instance using constructor: " + constructor);
                            return constructor.newInstance(args);
                        } catch (Exception constructorException) {
                            // This constructor failed, try the next one
                            System.out.println("[AnalysisEngine] Constructor failed: " + constructorException.getMessage());
                        }
                    }
                }
            }
            
            // If we get here, we couldn't find a suitable constructor
            throw new Exception("Cannot create instance of " + clazz.getName() + 
                ". Class must have either a default (no-argument) constructor or a constructor with " +
                "simple parameter types (int, boolean, double, String). " +
                "Found constructors: " + java.util.Arrays.toString(constructors));
        } catch (Exception otherException) {
            // Re-throw with helpful message
            throw new Exception("Failed to create instance of " + clazz.getName() + ": " + otherException.getMessage(), otherException);
        }
    }
    
    // Graph creation methods removed - Workshops 11 & 12 temporarily disabled
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}