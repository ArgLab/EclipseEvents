package com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.GlobalVars;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.PopupWindowModel;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;



public class PopupWindowListener {

	 private final Display display;
	    private Listener popupFilter; // kept here; Activator doesn't need to manage it

	    public PopupWindowListener(Display display) {
	        this.display = display;
	    }

	    /** Register filters; safe to call multiple times (no-ops if already started). */
	    public void startTrackingPopups() {
	        if (display == null || display.isDisposed() || popupFilter != null) return;

	        display.asyncExec(() -> {
	            if (display.isDisposed() || popupFilter != null) return;

	            // Cache main shell to exclude it
	            Shell mainShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
	                    ? PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
	                    : null;

	            popupFilter = new Listener() {
	                @Override public void handleEvent(Event event) {
	                    if (!(event.widget instanceof Shell)) return;
	                    final Shell shell = (Shell) event.widget;

	                    if (shell.isDisposed() || !shell.isVisible()) return;
	                    if (mainShell != null && shell == mainShell) return;

	                    int style = shell.getStyle();
	                    boolean isModal = (style & (SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) != 0;
	                    boolean looksLikeDialog = (style & SWT.DIALOG_TRIM) != 0 || isModal || shell.getParent() != null;
	                    if (!looksLikeDialog) return;

	                    display.timerExec(50, new Runnable() {
	                        @Override public void run() {
	                            if (display.isDisposed() || shell.isDisposed()) return;

	                            String title = safeShellText(shell);
	                            System.out.println("Popup window: " + title);

	                            // If you need tabs inside this dialog, search within the shell only:
	                            CTabFolder tabFolder = findTabFolderRecursively(shell);
	                            String activeTab = null;
	                            if (tabFolder != null && !tabFolder.isDisposed()
	                                    && tabFolder.getSelection() != null
	                                    && !tabFolder.getSelection().isDisposed()) {
	                                activeTab = tabFolder.getSelection().getText();
	                                System.out.println("Active Tab name is: " + activeTab);
	                            }

	                            PopupWindowModel pwm = new PopupWindowModel();
	                            pwm.setPopupwindow(title);
	                            pwm.setCurrentTab(activeTab);
	                            SequentialEventData sed = new SequentialEventData(
	                                    activeTab != null ? "PopUp Mouse Click" : "PopUp Opened", pwm);
	                            GlobalVars.listSequentialEvents.add(sed);
	                        }
	                    });

	                    shell.addListener(SWT.Dispose, e -> display.timerExec(-1, (Runnable) null));
	                }
	            };

	            display.addFilter(SWT.Show, popupFilter);
	            display.addFilter(SWT.Activate, popupFilter);
	        });
	    }

	    /** Remove filters; safe to call multiple times. */
	    public void stopTrackingPopups() {
	        if (display == null || display.isDisposed()) { popupFilter = null; return; }
	        if (popupFilter == null) return;

	        display.asyncExec(() -> {
	            if (!display.isDisposed() && popupFilter != null) {
	                display.removeFilter(SWT.Show, popupFilter);
	                display.removeFilter(SWT.Activate, popupFilter);
	                popupFilter = null;
	            }
	        });
	    }

	    private static String safeShellText(Shell shell) {
	        try { return shell.getText(); } catch (Throwable t) { return ""; }
	    }

	    private static CTabFolder findTabFolderRecursively(Control root) {
	        if (root == null || root.isDisposed()) return null;
	        if (root instanceof CTabFolder) return (CTabFolder) root;
	        if (root instanceof Composite) {
	            for (Control child : ((Composite) root).getChildren()) {
	                CTabFolder found = findTabFolderRecursively(child);
	                if (found != null) return found;
	            }
	        }
	        return null;
	    }


//	private CTabFolder findTabFolderRecursively(Control parent) {
//	    // Check if this control is a CTabFolder
//	    if (parent instanceof CTabFolder) {
//	        return (CTabFolder) parent;
//	    }
//
//	    // If the control is a Composite, check all its children recursively
//	    if (parent instanceof Composite) {
//	        for (Control child : ((Composite) parent).getChildren()) {
//	            CTabFolder tabFolder = findTabFolderRecursively(child);
//	            if (tabFolder != null) {
//	                return tabFolder;
//	            }
//	        }
//	    }
//
//	    // Return null if no CTabFolder is found
//	    return null;
//	}

}
