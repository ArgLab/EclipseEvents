package testpluginproject.handlers.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import testpluginproject.GlobalVars;
import testpluginproject.model.PopupWindowModel;
import testpluginproject.model.jsonModel.SequentialEventData;

public class PopupWindowListener {

	public void trackPopupWindows() {
	    Display display = Display.getDefault();

	    // Schedule the UI operation on the UI thread
	    display.asyncExec(new Runnable() {
	        @Override
	        public void run() {
	            // Add an SWT.Activate filter to track when a shell is activated
	            display.addFilter(SWT.Activate, event -> {
	                if (event.widget instanceof Shell) {
	                    Shell shell = (Shell) event.widget;
	                    System.out.println("Popup window: " + shell.getText());

	                    // Introducing a delay to allow the selection to update
	                    display.timerExec(200, new Runnable() {
	                        @Override
	                        public void run() {
	                            // Retrieve the tab list
	                            Control[] tablist = shell.getTabList();
	                            for (Control control : tablist) {
	                                if (control instanceof Composite) {
	                                    // Find the CTabFolder recursively within the Composite
	                                    CTabFolder tabfolder = findTabFolderRecursively(control);
	                                    if (tabfolder != null) {
	                                        // Get the currently active tab
	                                        CTabItem activeTab = tabfolder.getSelection();
	                                        if (activeTab != null) {
	                                            System.out.println("Active Tab name is: " + activeTab.getText());
	                                            PopupWindowModel pwm = new PopupWindowModel();
	                                            pwm.setPopupwindow(shell.getText());
	                                            pwm.setCurrentTab(activeTab.getText());
	                                            SequentialEventData sed = new SequentialEventData("PopUp Mouse Click",pwm);
	                                            GlobalVars.listSequentialEvents.add(sed);
	                                            
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    });
	                }
	            });
	        }
	    });
	}

	private CTabFolder findTabFolderRecursively(Control parent) {
	    // Check if this control is a CTabFolder
	    if (parent instanceof CTabFolder) {
	        return (CTabFolder) parent;
	    }

	    // If the control is a Composite, check all its children recursively
	    if (parent instanceof Composite) {
	        for (Control child : ((Composite) parent).getChildren()) {
	            CTabFolder tabFolder = findTabFolderRecursively(child);
	            if (tabFolder != null) {
	                return tabFolder;
	            }
	        }
	    }

	    // Return null if no CTabFolder is found
	    return null;
	}

}