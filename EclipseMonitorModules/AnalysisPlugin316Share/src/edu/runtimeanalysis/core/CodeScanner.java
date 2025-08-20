package edu.runtimeanalysis.core;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import edu.runtimeanalysis.models.MethodSignature;

/**
 * Scans workspace for method implementations
 */
public class CodeScanner {
    
    public Method findMethod(IJavaProject project, String className, MethodSignature signature) throws Exception {
        // First check for compilation errors
        checkForCompilationErrors(project.getProject());
        
        try {
            // Get the output location
            IPath outputLocation = project.getOutputLocation();
            IPath projectPath = project.getProject().getLocation();
            IPath fullOutputPath = projectPath.append(outputLocation.removeFirstSegments(1));
            
            // Create classloader for the project
            URL[] urls = new URL[] { fullOutputPath.toFile().toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            
            // Search for the class
            List<String> candidateClasses = findCandidateClasses(project, className);
            
            for (String candidate : candidateClasses) {
                try {
                    Class<?> clazz = classLoader.loadClass(candidate);
                    System.out.println("[CodeScanner] Searching for method " + signature.getMethodName() + " in class " + candidate);
                    Method method = findMethodInClass(clazz, signature);
                    if (method != null) {
                        System.out.println("[CodeScanner] Found method: " + method.toString());
                        return method;
                    }
                    System.out.println("[CodeScanner] Method " + signature.getMethodName() + " not found in " + candidate);
                } catch (ClassNotFoundException e) {
                    System.out.println("[CodeScanner] Class not found: " + candidate);
                    // Continue searching
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private List<String> findCandidateClasses(IJavaProject project, String className) throws JavaModelException {
        List<String> candidates = new ArrayList<>();
        
        // Only support fully qualified class names - exact match only
        for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                for (Object element : root.getChildren()) {
                    if (element instanceof IPackageFragment) {
                        IPackageFragment pkg = (IPackageFragment) element;
                        for (ICompilationUnit unit : pkg.getCompilationUnits()) {
                            IType[] types = unit.getTypes();
                            for (IType type : types) {
                                if (type.getFullyQualifiedName().equals(className)) {
                                    candidates.add(type.getFullyQualifiedName());
                                    return candidates; // Found exact match, return immediately
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return candidates; // Return empty list if no exact match found
    }
    
    private Method findMethodInClass(Class<?> clazz, MethodSignature signature) {
        StringBuilder diagnostics = new StringBuilder();
        
        try {
            // Try exact match first
            Method method = clazz.getDeclaredMethod(signature.getMethodName(), signature.getParameterTypes());
            if (isReturnTypeCompatible(method.getReturnType(), signature.getReturnType())) {
                method.setAccessible(true);
                return method;
            }
        } catch (NoSuchMethodException e) {
            System.out.println("[CodeScanner] Exact method not found, trying flexible matching...");
        }
        
        // Collect methods with matching names for better error reporting
        List<Method> methodsWithSameName = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(signature.getMethodName())) {
                methodsWithSameName.add(method);
                System.out.println("[CodeScanner] Found method with matching name: " + method.toString());
                if (isCompatibleSignature(method, signature)) {
                    System.out.println("[CodeScanner] Method signature is compatible!");
                    method.setAccessible(true);
                    return method;
                } else {
                    System.out.println("[CodeScanner] Method signature is not compatible");
                    // Record why this method didn't match for diagnostics
                    diagnostics.append(String.format("  - Found: %s (incompatible: %s)\n", 
                        method.toString(), describeSignatureMismatch(method, signature)));
                }
            }
        }
        
        // If we didn't find any compatible method, log helpful diagnostics
        if (methodsWithSameName.isEmpty()) {
            System.out.println("[CodeScanner] ERROR: No method named '" + signature.getMethodName() + 
                "' found in class " + clazz.getName());
            System.out.println("[CodeScanner] Available methods:");
            for (Method method : clazz.getDeclaredMethods()) {
                System.out.println("  - " + method.toString());
            }
        } else {
            System.out.println("[CodeScanner] ERROR: Found " + methodsWithSameName.size() + 
                " method(s) named '" + signature.getMethodName() + "' but none were compatible:");
            System.out.println(diagnostics.toString());
            System.out.println("[CodeScanner] Expected: " + signature.getDisplaySignature());
        }
        
        return null;
    }
    
    private boolean isCompatibleSignature(Method method, MethodSignature signature) {
        // Check return type - allow Object.class to match any return type (for generic flexibility)
        if (!isReturnTypeCompatible(method.getReturnType(), signature.getReturnType())) {
            return false;
        }
        
        // Check parameter count
        Class<?>[] methodParams = method.getParameterTypes();
        Class<?>[] signatureParams = signature.getParameterTypes();
        
        if (methodParams.length != signatureParams.length) {
            return false;
        }
        
        // Check parameter types - allow Object.class to match any parameter type (for generic flexibility)
        for (int i = 0; i < methodParams.length; i++) {
            if (!isParameterTypeCompatible(methodParams[i], signatureParams[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Describes why a method signature doesn't match the expected signature.
     */
    private String describeSignatureMismatch(Method method, MethodSignature signature) {
        List<String> issues = new ArrayList<>();
        
        // Check return type
        if (!isReturnTypeCompatible(method.getReturnType(), signature.getReturnType())) {
            issues.add("return type " + method.getReturnType().getSimpleName() + 
                      " vs expected " + signature.getReturnType().getSimpleName());
        }
        
        // Check parameter count
        Class<?>[] methodParams = method.getParameterTypes();
        Class<?>[] signatureParams = signature.getParameterTypes();
        
        if (methodParams.length != signatureParams.length) {
            issues.add("parameter count " + methodParams.length + " vs expected " + signatureParams.length);
        } else {
            // Check parameter types
            for (int i = 0; i < methodParams.length; i++) {
                if (!isParameterTypeCompatible(methodParams[i], signatureParams[i])) {
                    issues.add("parameter " + (i+1) + " type " + methodParams[i].getSimpleName() + 
                              " vs expected " + signatureParams[i].getSimpleName());
                }
            }
        }
        
        return issues.isEmpty() ? "unknown" : String.join(", ", issues);
    }
    
    /**
     * Check if return types are compatible.
     * Object.class in signature matches any actual return type (for generic flexibility).
     */
    private boolean isReturnTypeCompatible(Class<?> actualType, Class<?> expectedType) {
        // If expected type is Object, accept any actual type (for generic handling)
        if (expectedType == Object.class) {
            return true;
        }
        
        // Otherwise require exact match
        return actualType.equals(expectedType);
    }
    
    /**
     * Check if parameter types are compatible.
     * Object.class in signature matches any actual parameter type (for generic flexibility).
     * Also handles common student mistakes like Integer[] vs int[] vs Comparable[].
     */
    private boolean isParameterTypeCompatible(Class<?> actualType, Class<?> expectedType) {
        // If expected type is Object, accept any actual type (for generic handling)
        if (expectedType == Object.class) {
            return true;
        }
        
        // Exact match is always OK
        if (actualType.equals(expectedType)) {
            return true;
        }
        
        // Handle common array type mismatches that students make
        if (expectedType == Comparable[].class) {
            // Accept Integer[], String[], or any object array that could be Comparable
            if (actualType.isArray() && !actualType.getComponentType().isPrimitive()) {
                Class<?> componentType = actualType.getComponentType();
                // Check if the component type could be Comparable
                return Comparable.class.isAssignableFrom(componentType) ||
                       componentType == Object.class;
            }
        }
        
        // Handle primitive/wrapper mismatches (int vs Integer)
        if ((actualType == int.class && expectedType == Integer.class) ||
            (actualType == Integer.class && expectedType == int.class)) {
            return true;
        }
        
        if ((actualType == double.class && expectedType == Double.class) ||
            (actualType == Double.class && expectedType == double.class)) {
            return true;
        }
        
        if ((actualType == boolean.class && expectedType == Boolean.class) ||
            (actualType == Boolean.class && expectedType == boolean.class)) {
            return true;
        }
        
        // Handle int[] vs Integer[] mismatches  
        if (actualType == int[].class && expectedType == Integer[].class) {
            return true;
        }
        if (actualType == Integer[].class && expectedType == int[].class) {
            return true;
        }
        
        // Otherwise, no match
        return false;
    }
    
    /**
     * Checks for compilation errors in the project
     * @param project The project to check
     * @throws Exception if compilation errors are found
     */
    private void checkForCompilationErrors(IProject project) throws Exception {
        try {
            IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            
            List<String> compilationErrors = new ArrayList<>();
            
            for (IMarker marker : markers) {
                Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                if (severity != null && severity == IMarker.SEVERITY_ERROR) {
                    String message = (String) marker.getAttribute(IMarker.MESSAGE);
                    String resource = marker.getResource().getName();
                    Integer line = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                    
                    String errorDetail = String.format("%s (line %d): %s", resource, line != null ? line : 0, message);
                    compilationErrors.add(errorDetail);
                }
            }
            
            if (!compilationErrors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Your code has compilation errors that must be fixed before running analysis:\n\n");
                
                // Show up to 5 errors to avoid overwhelming the user
                int errorCount = Math.min(5, compilationErrors.size());
                for (int i = 0; i < errorCount; i++) {
                    errorMessage.append("• ").append(compilationErrors.get(i)).append("\n");
                }
                
                if (compilationErrors.size() > 5) {
                    errorMessage.append("... and ").append(compilationErrors.size() - 5).append(" more errors.\n");
                }
                
                errorMessage.append("\nPlease fix all compilation errors and try again.");
                
                throw new Exception(errorMessage.toString());
            }
            
        } catch (CoreException e) {
            throw new Exception("Unable to check for compilation errors: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a valid implementation exists for the given class and method signature.
     * This is a lighter-weight check that doesn't require full compilation and loading.
     * @param className The fully qualified class name
     * @param signature The method signature to check for
     * @return true if a valid implementation exists, false otherwise
     */
    public boolean hasValidImplementation(String className, MethodSignature signature) {
        try {
            // Check all Java projects in the workspace
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            
            for (IProject project : projects) {
                if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                    IJavaProject javaProject = JavaCore.create(project);
                    
                    // Quick compilation error check - if there are errors, assume implementations are not valid
                    try {
                        IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
                        boolean hasErrors = false;
                        for (IMarker marker : markers) {
                            Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
                            if (severity != null && severity == IMarker.SEVERITY_ERROR) {
                                hasErrors = true;
                                break;
                            }
                        }
                        if (hasErrors) {
                            continue; // Skip projects with compilation errors
                        }
                    } catch (CoreException e) {
                        continue; // Skip if we can't check for errors
                    }
                    
                    // Look for the class in this project
                    List<String> candidateClasses = findCandidateClasses(javaProject, className);
                    if (!candidateClasses.isEmpty()) {
                        // Found the class, now try to find the method
                        Method method = findMethod(javaProject, className, signature);
                        return method != null;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            // If we can't check, assume it doesn't exist
            return false;
        }
    }
}