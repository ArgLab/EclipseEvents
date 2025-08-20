package edu.runtimeanalysis.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import edu.runtimeanalysis.models.AlgorithmConfig;
import edu.runtimeanalysis.models.AlgorithmRegistry;
import edu.runtimeanalysis.models.AnalysisMode;
import edu.runtimeanalysis.models.AnalysisParameter;

/**
 * Dialog for selecting algorithm and input sizes for analysis
 */
public class AlgorithmSelectionDialog extends Dialog {
    
    // Static variable to remember last selected workshop across dialog instances
    private static String lastSelectedWorkshop = null;
    
    private Combo categoryCombo;
    private org.eclipse.swt.widgets.List algorithmList;
    private Text signatureText;
    private Text setupInstructionsText;
    private Group parametersGroup;
    private Composite parametersComposite;
    private Button[] parameterCheckboxes;
    private Spinner timeoutSpinner;
    private AlgorithmConfig selectedAlgorithm;
    private int[] selectedSizes; // Kept for backward compatibility
    private int timeoutSeconds;
    
    public AlgorithmSelectionDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Algorithm for Analysis");
        newShell.setSize(600, 750); // Make wider and taller
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        
        // Category selection
        Composite categoryComp = new Composite(container, SWT.NONE);
        categoryComp.setLayout(new GridLayout(2, false));
        categoryComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label categoryLabel = new Label(categoryComp, SWT.NONE);
        categoryLabel.setText("Category:");
        
        categoryCombo = new Combo(categoryComp, SWT.READ_ONLY);
        categoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String[] categories = AlgorithmRegistry.getCategories();
        categoryCombo.setItems(categories);
        
        // Restore last selected workshop, or default to first one
        int initialSelection = 0;
        if (lastSelectedWorkshop != null) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(lastSelectedWorkshop)) {
                    initialSelection = i;
                    break;
                }
            }
        }
        categoryCombo.select(initialSelection);
        
        categoryCombo.addListener(SWT.Selection, e -> {
            updateAlgorithmList();
            // Remember the selected workshop for next time
            lastSelectedWorkshop = categoryCombo.getText();
        });
        
        // Algorithm selection
        Group algorithmGroup = new Group(container, SWT.NONE);
        algorithmGroup.setText("Available Algorithms");
        algorithmGroup.setLayout(new GridLayout(1, false));
        GridData algorithmGroupData = new GridData(SWT.FILL, SWT.FILL, true, true);
        algorithmGroupData.heightHint = 150; // Fixed height to show ~5-7 methods, scrollable
        algorithmGroup.setLayoutData(algorithmGroupData);
        
        algorithmList = new org.eclipse.swt.widgets.List(algorithmGroup, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        algorithmList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        algorithmList.addListener(SWT.Selection, e -> updateSignature());
        
        // Algorithm information
        Group infoGroup = new Group(container, SWT.NONE);
        infoGroup.setText("Algorithm Information");
        infoGroup.setLayout(new GridLayout(2, false));
        infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label signatureLabel = new Label(infoGroup, SWT.NONE);
        signatureLabel.setText("Method Signature:");
        signatureText = new Text(infoGroup, SWT.READ_ONLY | SWT.BORDER);
        signatureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        signatureText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        
        Label setupLabel = new Label(infoGroup, SWT.NONE);
        setupLabel.setText("Test Setup:");
        setupInstructionsText = new Text(infoGroup, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
        GridData setupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        setupData.heightHint = 60; // Make it a bit taller since we removed complexity field
        setupInstructionsText.setLayoutData(setupData);
        setupInstructionsText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        
        // Analysis parameters (dynamic based on selected algorithm)
        parametersGroup = new Group(container, SWT.NONE);
        parametersGroup.setText("Analysis Parameters");
        parametersGroup.setLayout(new GridLayout(1, false));
        parametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        parametersComposite = new Composite(parametersGroup, SWT.NONE);
        parametersComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
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
        timeoutSpinner.setSelection(5); // Default 15 seconds
        timeoutSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        
        Label secondsLabel = new Label(timeoutGroup, SWT.NONE);
        secondsLabel.setText("seconds");
        
        Label timeoutInfo = new Label(timeoutGroup, SWT.WRAP);
        timeoutInfo.setText("If an algorithm takes longer than this timeout, it will be marked as timed out and displayed in red on the chart.");
        timeoutInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
        
        // Initialize the algorithm list with the restored/default category selection
        updateAlgorithmList();
        
        return container;
    }
    
    private void updateAlgorithmList() {
        String category = categoryCombo.getText();
        List<AlgorithmConfig> algorithms = AlgorithmRegistry.getAlgorithmsForCategory(category);
        
        algorithmList.removeAll();
        for (AlgorithmConfig algo : algorithms) {
            algorithmList.add(algo.getName());
        }
        
        if (algorithmList.getItemCount() > 0) {
            algorithmList.select(0);
            updateSignature();
        }
    }
    
    private void updateSignature() {
        int selection = algorithmList.getSelectionIndex();
        if (selection >= 0) {
            String category = categoryCombo.getText();
            List<AlgorithmConfig> algorithms = AlgorithmRegistry.getAlgorithmsForCategory(category);
            AlgorithmConfig selected = algorithms.get(selection);
            
            // Update algorithm information
            signatureText.setText(selected.getMethodSignature().getDisplaySignature());
            setupInstructionsText.setText(selected.getSetupInstructions() != null ? selected.getSetupInstructions() : "");
            
            // Update parameters section
            updateParametersSection(selected);
        }
    }
    
    private void updateParametersSection(AlgorithmConfig algorithmConfig) {
        // Clear existing controls
        if (parameterCheckboxes != null) {
            for (Button checkbox : parameterCheckboxes) {
                checkbox.dispose();
            }
        }
        Control[] children = parametersComposite.getChildren();
        for (Control child : children) {
            child.dispose();
        }
        
        // Get analysis parameter
        AnalysisParameter parameter = algorithmConfig.getAnalysisParameter();
        AnalysisMode mode = algorithmConfig.getAnalysisMode();
        
        // Set layout based on number of parameter values
        Object[] values = parameter.getDefaultValues();
        // Use more columns for better layout when we have many values
        int numColumns = values.length <= 5 ? values.length : 
                        values.length <= 10 ? 5 : 
                        values.length <= 20 ? 6 : 8; // More flexible column count
        parametersComposite.setLayout(new GridLayout(numColumns, true));
        
        // Update group title to reflect parameter type
        parametersGroup.setText(parameter.getName());
        
        // Add description label
        Label descLabel = new Label(parametersComposite, SWT.WRAP);
        descLabel.setText(parameter.getDescription());
        GridData descData = new GridData(SWT.FILL, SWT.TOP, true, false, numColumns, 1);
        descData.heightHint = 30;
        descLabel.setLayoutData(descData);
        
        // Add helper buttons for selection when we have many values
        if (values.length > 10) {
            Composite buttonComposite = new Composite(parametersComposite, SWT.NONE);
            GridData buttonData = new GridData(SWT.FILL, SWT.TOP, true, false, numColumns, 1);
            buttonComposite.setLayoutData(buttonData);
            buttonComposite.setLayout(new GridLayout(4, false));
            
            Button selectAllBtn = new Button(buttonComposite, SWT.PUSH);
            selectAllBtn.setText("Select All");
            Button selectNoneBtn = new Button(buttonComposite, SWT.PUSH);
            selectNoneBtn.setText("Clear All");
            Button selectRecommendedBtn = new Button(buttonComposite, SWT.PUSH);
            selectRecommendedBtn.setText("Recommended (" + Math.min(15, values.length) + ")");
            Label spacer = new Label(buttonComposite, SWT.NONE);
            spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }
        
        // Create checkboxes for parameter values
        parameterCheckboxes = new Button[values.length];
        for (int i = 0; i < values.length; i++) {
            parameterCheckboxes[i] = new Button(parametersComposite, SWT.CHECK);
            parameterCheckboxes[i].setText(formatParameterValue(values[i], mode));
            // Default: select recommended number of points for better performance
            parameterCheckboxes[i].setSelection(values.length <= 10 || i < 15); // Select first 15 for large sets
        }
        
        // Wire up helper button actions if they exist
        if (values.length > 10) {
            Control[] buttonCompositeChildren = ((Composite)parametersComposite.getChildren()[1]).getChildren();
            Button selectAllBtn = (Button)buttonCompositeChildren[0];
            Button selectNoneBtn = (Button)buttonCompositeChildren[1]; 
            Button selectRecommendedBtn = (Button)buttonCompositeChildren[2];
            
            selectAllBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (Button cb : parameterCheckboxes) cb.setSelection(true);
                }
            });
            
            selectNoneBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (Button cb : parameterCheckboxes) cb.setSelection(false);
                }
            });
            
            selectRecommendedBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    for (int i = 0; i < parameterCheckboxes.length; i++) {
                        parameterCheckboxes[i].setSelection(i < 15); // Select first 15
                    }
                }
            });
        }
        
        // Relayout the composite
        parametersComposite.layout();
        parametersGroup.layout();
    }
    
    private String formatParameterValue(Object value, AnalysisMode mode) {
        if (value instanceof Integer) {
            return String.format("%,d", (Integer) value);
        } else if (value instanceof Double) {
            return String.format("%.2f", (Double) value);
        } else {
            return value.toString();
        }
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Analyze", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected void okPressed() {
        // Get selected algorithm
        int selection = algorithmList.getSelectionIndex();
        if (selection < 0) {
            return;
        }
        
        String category = categoryCombo.getText();
        List<AlgorithmConfig> algorithms = AlgorithmRegistry.getAlgorithmsForCategory(category);
        selectedAlgorithm = algorithms.get(selection);
        
        // Check if any parameter values are selected
        if (parameterCheckboxes != null) {
            boolean anySelected = false;
            for (Button checkbox : parameterCheckboxes) {
                if (checkbox.getSelection()) {
                    anySelected = true;
                    break;
                }
            }
            if (!anySelected) {
                return; // No parameters selected
            }
        }
        
        // For backward compatibility, generate selectedSizes from parameter values if they are integers
        AnalysisParameter parameter = selectedAlgorithm.getAnalysisParameter();
        Object[] parameterValues = parameter.getDefaultValues();
        List<Integer> sizes = new ArrayList<>();
        
        if (parameterValues[0] instanceof Integer && parameterCheckboxes != null) {
            for (int i = 0; i < parameterCheckboxes.length; i++) {
                if (parameterCheckboxes[i].getSelection()) {
                    sizes.add((Integer) parameterValues[i]);
                }
            }
            selectedSizes = sizes.stream().mapToInt(i -> i).toArray();
        } else {
            // For non-integer parameters, create dummy sizes array
            selectedSizes = new int[]{1000}; // Default size for backward compatibility
        }
        
        // Get timeout value
        timeoutSeconds = timeoutSpinner.getSelection();
        
        super.okPressed();
    }
    
    public AlgorithmConfig getSelectedAlgorithm() {
        return selectedAlgorithm;
    }
    
    public int[] getSelectedInputSizes() {
        return selectedSizes;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}