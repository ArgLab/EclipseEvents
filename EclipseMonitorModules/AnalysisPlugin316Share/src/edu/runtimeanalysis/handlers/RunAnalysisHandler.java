package edu.runtimeanalysis.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.runtimeanalysis.Activator;
import edu.runtimeanalysis.core.AnalysisEngine;
import edu.runtimeanalysis.core.AnalysisProgressCallback;
import edu.runtimeanalysis.core.AnalysisResultsCallback;
import edu.runtimeanalysis.core.ExecutionResult;
import edu.runtimeanalysis.dialogs.AlgorithmSelectionDialog;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AnalysisMode;
import edu.runtimeanalysis.views.RuntimeAnalysisView;

/**
 * Handler for the Run Analysis command
 */
public class RunAnalysisHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        
        // Log the action
        Activator.getDefault().getUsageLogger().logAction("ANALYSIS_INITIATED", null);
        
        // Show algorithm selection dialog
        AlgorithmSelectionDialog dialog = new AlgorithmSelectionDialog(shell);
        if (dialog.open() == Window.OK) {
            AlgorithmConfig selectedConfig = dialog.getSelectedAlgorithm();
            int[] inputSizes = dialog.getSelectedInputSizes();
            int timeoutSeconds = dialog.getTimeoutSeconds();
            
            // Log the selection
            Activator.getDefault().getUsageLogger().logAction("ALGORITHM_SELECTED", 
                "algorithm=" + selectedConfig.getName() + ",sizes=" + java.util.Arrays.toString(inputSizes) +
                ",timeout=" + timeoutSeconds + "s");
            
            // Show the view and detect if it was already open
            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            try {
                // Check if view is already open before showing it
                IViewPart existingView = page.findView(RuntimeAnalysisView.ID);
                boolean viewWasAlreadyOpen = (existingView != null);
                
                IViewPart view = page.showView(RuntimeAnalysisView.ID);
                if (view instanceof RuntimeAnalysisView) {
                    RuntimeAnalysisView analysisView = (RuntimeAnalysisView) view;
                    
                    // If view wasn't already open, mark it as just opened to show instructions
                    if (!viewWasAlreadyOpen) {
                        analysisView.markAsJustOpened();
                    }
                    
                    // Check if analysis is already running
                    if (analysisView.isAnalysisRunning()) {
                        boolean proceed = MessageDialog.openQuestion(shell, 
                            "Analysis Already Running", 
                            "An analysis is currently running. Starting a new analysis will stop the current one.\n\n" +
                            "Do you want to proceed?");
                        
                        if (!proceed) {
                            Activator.getDefault().getUsageLogger().logAction("CONCURRENT_ANALYSIS_CANCELLED", null);
                            return null;
                        }
                        
                        Activator.getDefault().getUsageLogger().logAction("CONCURRENT_ANALYSIS_STARTED", null);
                    }
                    
                    // Run the analysis in a separate thread
                    new Thread(() -> {
                        runAnalysis(shell, selectedConfig, inputSizes, timeoutSeconds, analysisView);
                    }).start();
                }
            } catch (PartInitException e) {
                MessageDialog.openError(shell, "Error", "Could not open Runtime Analysis view: " + e.getMessage() + "\nExpected: " + selectedConfig.toString());
                Activator.getDefault().getUsageLogger().logAction("VIEW_OPEN_ERROR", e.getMessage());
            }
        } else {
            Activator.getDefault().getUsageLogger().logAction("ANALYSIS_CANCELLED", null);
        }
        
        return null;
    }
    
    private void runAnalysis(Shell shell, AlgorithmConfig config, int[] inputSizes, int timeoutSeconds, RuntimeAnalysisView view) {
        AnalysisEngine engine = new AnalysisEngine();
        engine.setTimeoutSeconds(timeoutSeconds);
        
        // Create progress callback that updates the view
        AnalysisProgressCallback progressCallback = new AnalysisProgressCallback() {
            @Override
            public void onAnalysisStart(int totalSteps) {
                shell.getDisplay().asyncExec(() -> {
                    view.setAnalysisRunning(true);
                    view.showProgress(totalSteps);
                });
            }
            
            @Override
            public void updateProgress(int currentStep, int totalSteps, String stepDescription) {
                shell.getDisplay().asyncExec(() -> {
                    view.updateProgress(currentStep, totalSteps, stepDescription);
                });
            }
            
            @Override
            public void onAnalysisComplete(boolean success) {
                shell.getDisplay().asyncExec(() -> {
                    view.setAnalysisRunning(false);
                    view.hideProgress();
                });
            }
        };
        
        // Create results callback that updates the chart progressively
        AnalysisResultsCallback resultsCallback = new AnalysisResultsCallback() {
            @Override
            public void onAnalysisStarted(String algorithmName, int totalInputSizes) {
                shell.getDisplay().asyncExec(() -> {
                    // Initialize the chart with empty results but show the algorithm name
                    view.updateResults(algorithmName + " (Running...)", new java.util.ArrayList<>(), config);
                });
            }
            
            @Override
            public void onResultAvailable(String algorithmName, List<ExecutionResult> allResults, 
                                         ExecutionResult newResult, boolean isRunning) {
                shell.getDisplay().asyncExec(() -> {
                    // Update chart with progressive results
                    String displayName = isRunning ? algorithmName + " (Running...)" : algorithmName;
                    view.updateResults(displayName, allResults, config);
                    System.out.println("[RunAnalysisHandler] Progressive update: " + allResults.size() + 
                                     " results, latest: " + newResult.getInputSize() + 
                                     " -> " + (newResult.isSuccess() ? newResult.getExecutionTime() + "ms" : "FAILED"));
                });
            }
            
            @Override
            public void onAnalysisCompleted(String algorithmName, List<ExecutionResult> finalResults, boolean success) {
                shell.getDisplay().asyncExec(() -> {
                    // Final update with completed status
                    view.updateResults(algorithmName, finalResults, config);
                    System.out.println("[RunAnalysisHandler] Analysis completed: " + finalResults.size() + 
                                     " final results, success: " + success);
                });
            }
        };
        
        // Set the callbacks
        engine.setProgressCallback(progressCallback);
        engine.setResultsCallback(resultsCallback);
        
        try {
            // Use new analysis method if algorithm config has proper analysis mode configuration
            List<ExecutionResult> results;
            if (config.getAnalysisMode() != null && !config.getAnalysisMode().equals(AnalysisMode.TRADITIONAL_SCALING)) {
                // Use the new scenario-based analysis
                results = engine.runAnalysis(config);
            } else {
                // Fall back to traditional input sizes analysis for backward compatibility
                results = engine.runAnalysis(config, inputSizes);
            }
            
            // Log successful completion
            Activator.getDefault().getUsageLogger().logAnalysisResults(config.getName(), results);
            
        } catch (Exception e) {
            // Show error dialog
            shell.getDisplay().asyncExec(() -> {
                view.setAnalysisRunning(false);
                view.hideProgress();
                String errorMessage = formatErrorMessage(e) + "\n";
                MessageDialog.openError(shell, "Analysis Error", errorMessage);
            });
            
            // Log the error
            Activator.getDefault().getUsageLogger().logAction("ANALYSIS_ERROR", 
                "algorithm=" + config.getName() + ",error=" + e.getMessage());
        }
    }
    
    private String formatErrorMessage(Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("Cannot find implementation")) {
            return e.getMessage();
        } else if (e.getMessage() != null && e.getMessage().contains("compilation errors")) {
            return e.getMessage(); // Return the detailed compilation error message from CodeScanner
        } else if (e.getMessage() != null && e.getMessage().contains("Compilation error")) {
            return "Your code has compilation errors. Please fix all errors before running analysis.";
        } else {
            return "An unexpected error occurred during analysis: " + e.getMessage() + 
                   "\n\nIf this problem persists, please email jtbacher@ncsu.edu";
        }
    }
}