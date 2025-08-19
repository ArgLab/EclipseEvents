package edu.runtimeanalysis.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import edu.runtimeanalysis.core.CodeScanner;
import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AlgorithmRegistry;
import edu.runtimeanalysis.models.AnalysisMode;

/**
 * Dialog for selecting a workshop and configuring HTML report export for all algorithms in that workshop
 */
public class ExportReportDialog extends Dialog {
    
    private Combo categoryCombo;
    private Spinner timeoutSpinner;
    private Text outputPathText;
    private Button browseButton;
    private List<AlgorithmConfig> selectedAlgorithms;
    private int[] selectedSizes; // Kept for backward compatibility with export engine
    private int timeoutSeconds;
    private String outputPath;
    private Text workshopInfoText;
    private CodeScanner codeScanner;
    
    public ExportReportDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        selectedAlgorithms = new ArrayList<>();
        codeScanner = new CodeScanner();
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Export Runtime Analysis Report");
        newShell.setSize(600, 500);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        
        // Workshop selection
        Composite categoryComp = new Composite(container, SWT.NONE);
        categoryComp.setLayout(new GridLayout(2, false));
        categoryComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label categoryLabel = new Label(categoryComp, SWT.NONE);
        categoryLabel.setText("Workshop to analyze:");
        
        categoryCombo = new Combo(categoryComp, SWT.READ_ONLY);
        categoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        categoryCombo.setItems(AlgorithmRegistry.getCategories());
        categoryCombo.select(0);
        categoryCombo.addListener(SWT.Selection, e -> {
            updateWorkshopInfo();
            updateDefaultFilename();
        });
        
        // Workshop information
        Group workshopGroup = new Group(container, SWT.NONE);
        workshopGroup.setText("Workshop Details");
        workshopGroup.setLayout(new GridLayout(1, false));
        workshopGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Info label
        Label infoLabel = new Label(workshopGroup, SWT.WRAP);
        infoLabel.setText("Select a workshop above. The report will include ALL algorithms from that workshop. Each algorithm will be analyzed and included in an HTML report with graphs arranged in a grid layout. The HTML report can be opened in your browser and saved/printed as PDF if needed.");
        infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        // Workshop details (scrollable text area)
        workshopInfoText = new Text(workshopGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY);
        workshopInfoText.setText("Select a workshop to see details about algorithms and implementation requirements.");
        GridData workshopData = new GridData(SWT.FILL, SWT.FILL, true, true);
        workshopData.heightHint = 200; // Increased height for better visibility
        workshopInfoText.setLayoutData(workshopData);
        
        // Timeout configuration
        Group timeoutGroup = new Group(container, SWT.NONE);
        timeoutGroup.setText("Timeout Settings");
        timeoutGroup.setLayout(new GridLayout(3, false));
        timeoutGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label timeoutLabel = new Label(timeoutGroup, SWT.NONE);
        timeoutLabel.setText("Timeout per run:");
        
        timeoutSpinner = new Spinner(timeoutGroup, SWT.BORDER);
        timeoutSpinner.setMinimum(1);
        timeoutSpinner.setMaximum(300); // Max 5 minutes
        timeoutSpinner.setSelection(15); // Default 15 seconds
        timeoutSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        
        Label secondsLabel = new Label(timeoutGroup, SWT.NONE);
        secondsLabel.setText("seconds");
        
        Label timeoutInfo = new Label(timeoutGroup, SWT.WRAP);
        timeoutInfo.setText("If an algorithm takes longer than this timeout, it will be marked as timed out and displayed in red on the charts.");
        timeoutInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
        
        // Output file selection
        Group outputGroup = new Group(container, SWT.NONE);
        outputGroup.setText("Output File");
        outputGroup.setLayout(new GridLayout(3, false));
        outputGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label pathLabel = new Label(outputGroup, SWT.NONE);
        pathLabel.setText("Save as:");
        
        outputPathText = new Text(outputGroup, SWT.BORDER);
        outputPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        // Default filename will be set when workshop is selected
        updateDefaultFilename();
        
        browseButton = new Button(outputGroup, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addListener(SWT.Selection, e -> browseOutputFile());
        
        // Initialize the workshop info
        updateWorkshopInfo();
        
        return container;
    }
    
    private void updateDefaultFilename() {
        String category = categoryCombo.getText();
        if (category != null && !category.trim().isEmpty()) {
            // Convert workshop name to filename-friendly format
            String filename = category.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Remove special characters
                .replaceAll("\\s+", "_") // Replace spaces with underscores
                + "_report.html";
            
            String defaultPath = System.getProperty("user.home") + File.separator + filename;
            outputPathText.setText(defaultPath);
        } else {
            // Fallback to generic name
            outputPathText.setText(System.getProperty("user.home") + File.separator + "runtime_analysis_report.html");
        }
    }
    
    private void updateWorkshopInfo() {
        String category = categoryCombo.getText();
        List<AlgorithmConfig> algorithms = AlgorithmRegistry.getAlgorithmsForCategory(category);
        
        StringBuilder info = new StringBuilder();
        info.append("Workshop: ").append(category).append("\n");
        info.append("Total algorithms: ").append(algorithms.size()).append("\n\n");
        
        // Group algorithms by analysis mode
        Map<AnalysisMode, Integer> modeCounts = new LinkedHashMap<>();
        for (AlgorithmConfig algorithm : algorithms) {
            AnalysisMode mode = algorithm.getAnalysisMode();
            modeCounts.put(mode, modeCounts.getOrDefault(mode, 0) + 1);
        }
        
        info.append("Analysis types included:\n");
        for (Map.Entry<AnalysisMode, Integer> entry : modeCounts.entrySet()) {
            AnalysisMode mode = entry.getKey();
            int count = entry.getValue();
            info.append("• ").append(count).append(" algorithm").append(count == 1 ? "" : "s")
                .append(" using ").append(mode.getDescription()).append("\n");
        }
        
        info.append("\nRequired implementations:\n");
        for (AlgorithmConfig algorithm : algorithms) {
            info.append("• ").append(algorithm.getClassName())
                .append(".").append(algorithm.getMethodSignature().getMethodName()).append("()\n");
        }
        
        info.append("\nEach algorithm will use its predefined parameter values during analysis.");
        
        workshopInfoText.setText(info.toString());
    }
    
    private void browseOutputFile() {
        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText("Save Analysis Report");
        dialog.setFilterExtensions(new String[]{"*.html"});
        dialog.setFilterNames(new String[]{"HTML Files (*.html)"});
        dialog.setFileName("runtime_analysis_report.html");
        
        String currentPath = outputPathText.getText().trim();
        if (!currentPath.isEmpty()) {
            File file = new File(currentPath);
            dialog.setFilterPath(file.getParent());
            dialog.setFileName(file.getName());
        }
        
        String selectedFile = dialog.open();
        if (selectedFile != null) {
            outputPathText.setText(selectedFile);
        }
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Generate Report", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected void okPressed() {
        // Get selected workshop
        String category = categoryCombo.getText();
        if (category == null || category.trim().isEmpty()) {
            MessageDialog.openError(getShell(), "Error", "Please select a workshop to analyze.");
            return;
        }
        
        // Get all algorithms for the selected workshop
        List<AlgorithmConfig> algorithms = AlgorithmRegistry.getAlgorithmsForCategory(category);
        if (algorithms.isEmpty()) {
            MessageDialog.openError(getShell(), "Error", "No algorithms found for the selected workshop.");
            return;
        }
        
        // Check for missing implementations
        List<String> missingImplementations = new ArrayList<>();
        List<String> availableAlgorithms = new ArrayList<>();
        
        for (AlgorithmConfig algorithm : algorithms) {
            boolean hasImplementation = codeScanner.hasValidImplementation(
                algorithm.getClassName(), 
                algorithm.getMethodSignature()
            );
            
            if (hasImplementation) {
                availableAlgorithms.add(algorithm.getName());
            } else {
                missingImplementations.add(algorithm.getClassName() + "." + 
                    algorithm.getMethodSignature().getMethodName() + "()");
            }
        }
        
        // If there are missing implementations, show helpful error message
        if (!missingImplementations.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("To generate a complete report for ").append(category)
                   .append(", you need to implement ALL of the following methods:\n\n");
            
            errorMsg.append("Missing implementations:\n");
            for (String missing : missingImplementations) {
                errorMsg.append("• ").append(missing).append("\n");
            }
            
            if (!availableAlgorithms.isEmpty()) {
                errorMsg.append("\nFound implementations:\n");
                for (String available : availableAlgorithms) {
                    errorMsg.append("✓ ").append(available).append("\n");
                }
            }
            
            errorMsg.append("\nPlease implement the missing methods and try again. ");
            errorMsg.append("The report will include all algorithms from the workshop, ");
            errorMsg.append("so all implementations must be available.");
            
            MessageDialog.openError(getShell(), "Missing Implementations", errorMsg.toString());
            return;
        }
        
        // All implementations found, proceed with export
        selectedAlgorithms.clear();
        selectedAlgorithms.addAll(algorithms);
        
        // For backward compatibility with export engine, we set a default size array
        // The new export engine will use algorithm-specific parameters instead
        selectedSizes = new int[]{1000};
        
        // Get timeout value
        timeoutSeconds = timeoutSpinner.getSelection();
        
        // Validate output path
        outputPath = outputPathText.getText().trim();
        if (outputPath.isEmpty()) {
            MessageDialog.openError(getShell(), "Error", "Please specify an output file path.");
            return;
        }
        
        // Ensure .html extension
        if (!outputPath.toLowerCase().endsWith(".html")) {
            outputPath += ".html";
        }
        
        // Check if file already exists
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            boolean overwrite = MessageDialog.openQuestion(getShell(), 
                "File Exists", 
                "The file '" + outputFile.getName() + "' already exists. Do you want to overwrite it?");
            if (!overwrite) {
                return;
            }
        }
        
        super.okPressed();
    }
    
    public List<AlgorithmConfig> getSelectedAlgorithms() {
        return selectedAlgorithms;
    }
    
    public int[] getSelectedInputSizes() {
        return selectedSizes;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}