package edu.runtimeanalysis.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.runtimeanalysis.Activator;
import edu.runtimeanalysis.core.AnalysisProgressCallback;
import edu.runtimeanalysis.dialogs.ExportReportDialog;
import edu.runtimeanalysis.export.ReportExportEngine;
import edu.runtimeanalysis.models.AlgorithmConfig;

/**
 * Handler for the Export Report command
 */
public class ExportReportHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        
        // Log the action
        Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_INITIATED", null);
        
        // Show export report dialog
        ExportReportDialog dialog = new ExportReportDialog(shell);
        if (dialog.open() == Window.OK) {
            List<AlgorithmConfig> selectedAlgorithms = dialog.getSelectedAlgorithms();
            int[] inputSizes = dialog.getSelectedInputSizes();
            int timeoutSeconds = dialog.getTimeoutSeconds();
            String outputPath = dialog.getOutputPath();
            
            // Log the selection
            StringBuilder algorithmsStr = new StringBuilder();
            for (int i = 0; i < selectedAlgorithms.size(); i++) {
                if (i > 0) algorithmsStr.append(",");
                algorithmsStr.append(selectedAlgorithms.get(i).getName());
            }
            
            Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_CONFIGURED", 
                "algorithms=" + algorithmsStr.toString() + ",sizes=" + java.util.Arrays.toString(inputSizes) + 
                ",timeout=" + timeoutSeconds + "s,output=" + outputPath);
            
            // Run the export in a background thread with progress dialog
            try {
                ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
                progressDialog.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(org.eclipse.core.runtime.IProgressMonitor monitor) 
                            throws java.lang.reflect.InvocationTargetException, InterruptedException {
                        
                        try {
                            // Calculate total work units
                            int totalSteps = selectedAlgorithms.size() * (2 + inputSizes.length * 8) + 1;
                            monitor.beginTask("Generating Runtime Analysis Report", totalSteps);
                            
                            // Create export engine
                            ReportExportEngine exportEngine = new ReportExportEngine();
                            
                            // Create progress callback that updates the Eclipse progress monitor
                            AnalysisProgressCallback progressCallback = new AnalysisProgressCallback() {
                                @Override
                                public void onAnalysisStart(int totalSteps) {
                                    monitor.setTaskName("Starting export analysis...");
                                }
                                
                                @Override
                                public void updateProgress(int currentStep, int totalSteps, String stepDescription) {
                                    if (monitor.isCanceled()) {
                                        throw new RuntimeException("Export was cancelled by user");
                                    }
                                    
                                    monitor.worked(1);
                                    monitor.setTaskName(stepDescription);
                                }
                                
                                @Override
                                public void onAnalysisComplete(boolean success) {
                                    if (success) {
                                        monitor.setTaskName("Export completed successfully");
                                    } else {
                                        monitor.setTaskName("Export failed");
                                    }
                                }
                            };
                            
                            exportEngine.setProgressCallback(progressCallback);
                            
                            // Run the export
                            exportEngine.generateReport(selectedAlgorithms, inputSizes, timeoutSeconds, outputPath);
                            
                            monitor.done();
                            
                        } catch (Exception e) {
                            throw new java.lang.reflect.InvocationTargetException(e);
                        }
                    }
                });
                
                // Show completion dialog
                boolean openReport = MessageDialog.openQuestion(shell, 
                    "Export Complete", 
                    "Runtime analysis report has been generated successfully!\n\n" +
                    "Location: " + outputPath + "\n\n" +
                    "The report has been saved as an HTML file with embedded charts. " +
                    "You can open it in your browser and save/print as PDF if needed.\n\n" +
                    "Would you like to open the report now?");
                
                if (openReport) {
                    // Open the HTML report in the default browser
                    String htmlPath = outputPath;
                    if (!Program.launch(htmlPath)) {
                        MessageDialog.openInformation(shell, "Report Generated", 
                            "Report generated successfully at:\n" + htmlPath + 
                            "\n\nPlease open this file in your web browser.");
                    }
                }
                
                // Log successful completion
                Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_SUCCESS", 
                    "algorithms_count=" + selectedAlgorithms.size() + ",output=" + outputPath);
                
            } catch (Exception e) {
                // Handle errors
                String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                
                if (errorMessage != null && errorMessage.contains("cancelled")) {
                    // User cancelled
                    Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_CANCELLED", null);
                } else {
                    // Show error dialog
                    MessageDialog.openError(shell, "Export Error", 
                        "Failed to generate report: " + errorMessage + 
                        "\n\nPlease check that:\n" +
                        "- Your algorithm implementations exist and compile\n" +
                        "- The output directory is writable\n" +
                        "- You have sufficient disk space");
                    
                    // Log the error
                    Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_ERROR", 
                        "error=" + errorMessage);
                }
            }
            
        } else {
            Activator.getDefault().getUsageLogger().logAction("EXPORT_REPORT_CANCELLED", null);
        }
        
        return null;
    }
}