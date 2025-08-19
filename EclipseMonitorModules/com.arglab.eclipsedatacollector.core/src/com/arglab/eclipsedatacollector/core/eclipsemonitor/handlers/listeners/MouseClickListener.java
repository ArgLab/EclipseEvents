package com.arglab.eclipsedatacollector.core.eclipsemonitor.handlers.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.MouseClickData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;



public class MouseClickListener implements MouseListener, Listener{
	
	IWorkbenchWindow window;
	KeyBoardClickListener keyBoardClickListener;
	List<SequentialEventData> mouseClickData;
	public MouseClickListener(IWorkbenchWindow window, KeyBoardClickListener keyBoardClickListener) {
		this.mouseClickData = new ArrayList<>();
		this.keyBoardClickListener = keyBoardClickListener;
		this.window = window;
	}


	public List<SequentialEventData> getMouseClickData() {
		return mouseClickData;
	}


	public void setMouseClickData(List<SequentialEventData> mouseClickData) {
		this.mouseClickData = mouseClickData;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
		System.out.println("Mouse click at point: "+e.getX()+" "+e.getY());
		System.out.println("Mouse click at button: "+e.getButton());
		System.out.println("Mouse click count: "+e.getClickCount());
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void handleEvent(Event event) {
	    System.out.println("Mouse");
	    keyBoardClickListener.immediateSave();

	    final Display display = window.getWorkbench().getDisplay();

	    // Defer to end of current UI cycle so caret/selection has updated
	    display.asyncExec(() -> {
	        if (display.isDisposed()) return;

	        String windowTitle = "";
	        Shell shell = null;

	        // 1) Resolve the owning Shell from the clicked widget (popup or main)
	        if (event.widget instanceof Control) {
	            Control c = (Control) event.widget;
	            if (c != null && !c.isDisposed()) {
	                shell = c.getShell();
	            }
	        }
	        if (shell == null || shell.isDisposed()) {
	            shell = display.getActiveShell(); // fallback
	        }
	        if (shell != null && !shell.isDisposed()) {
	            windowTitle = safeShellText(shell);
	        }

	        // Optional: classify popup vs general window if you want to store/type it
	        boolean isPopup = false;
	        if (shell != null && !shell.isDisposed()) {
	            int style = shell.getStyle();
	            isPopup = (shell.getParent() != null)
	                    || ( (style & (SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) != 0 );
	        }

	        // 2) Capture editor caret (line/column) if an editor is active
	        int lineNumber = 0;
	        int columnNumber = 0;
	        try {
	            IWorkbenchPage page = window.getActivePage();
	            if (page != null) {
	                IEditorPart editorPart = page.getActiveEditor();
	                if (editorPart instanceof ITextEditor) {
	                    ITextEditor textEditor = (ITextEditor) editorPart;
	                    IDocumentProvider docProvider = textEditor.getDocumentProvider();
	                    IDocument document = docProvider.getDocument(textEditor.getEditorInput());
	                    ISelectionProvider selectionProvider = textEditor.getSite().getSelectionProvider();
	                    ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection();
	                    lineNumber = textSelection.getStartLine();
	                    try {
	                        columnNumber = textSelection.getOffset()
	                                - document.getLineInformation(lineNumber).getOffset();
	                    } catch (org.eclipse.jface.text.BadLocationException e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        } catch (Throwable t) {
	            t.printStackTrace();
	        }

	        // 3) Log/store with the shell title (this is the precise popup/dialog title if inside one)
	        System.out.println("MouseClick window: " + windowTitle + (isPopup ? " [popup]" : ""));

	        MouseClickData mcd = new MouseClickData(
	                event.x,
	                event.y,
	                windowTitle,
	                lineNumber + 1,
	                columnNumber + 1
	        );
	        SequentialEventData sedMC = new SequentialEventData("MouseClickEvent", mcd);
	        mouseClickData.add(sedMC);
	    });
	}

	/** Safe shell title accessor to avoid SWTExceptions on disposed widgets. */
	private static String safeShellText(Shell shell) {
	    try {
	        return shell.getText();
	    } catch (Throwable t) {
	        return "";
	    }
	}
	
//	@Override
//	public void handleEvent(Event event) {
//		// TODO Auto-generated method stub
//		System.out.println("Mouse");
//		keyBoardClickListener.immediateSave();
//		if(window.getActivePage()!=null) {
//			if(window.getActivePage().getActivePart()!=null) {
//				try {
//					window.getWorkbench().getDisplay().timerExec(200, new Runnable() {
//						
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub			
//							if(window.getActivePage().getActivePart().getTitle()!=null) {
//								
//								IWorkbenchPage page = window.getActivePage(); //Get active page
//								int lineNumber = 0;
//								int columnNumber = 0;
//										
//							    if (page != null) {
//							        IEditorPart editorPart = page.getActiveEditor(); //Get editor
//							        if (editorPart instanceof ITextEditor) {
//							            ITextEditor textEditor = (ITextEditor) editorPart; //If textEditor then cast it to editorPart
//							            IDocumentProvider docProvider = textEditor.getDocumentProvider(); //Get document provider
//							            IDocument document = docProvider.getDocument(textEditor.getEditorInput()); //Get document
//							            ISelectionProvider selectionProvider = textEditor.getSite().getSelectionProvider(); //Get selection provider
//							            ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection(); //Get text selection
//							            lineNumber = textSelection.getStartLine(); //Get start line
//										try {
//											columnNumber = textSelection.getOffset()-document.getLineInformation(lineNumber).getOffset(); //Get offset by total offset-linenumber's offset
//											System.out.println("Line: " + (lineNumber + 1)); //Print line
//								            System.out.println("Column: " + (columnNumber + 1)); //Print Column number
//										} catch (org.eclipse.jface.text.BadLocationException e) {
//											// TODO Auto-generated catch block
//											e.printStackTrace();
//										}
//							        }
//							    }
//								
//								System.out.println("MouseClick to Window: "+window.getActivePage().getActivePart().getTitle());
//								MouseClickData mcd = new MouseClickData(event.x, event.y, window.getActivePage().getActivePart().getTitle(),lineNumber+1,columnNumber + 1);
//								SequentialEventData sedMC = new SequentialEventData("MouseClickEvent", mcd);
//								mouseClickData.add(sedMC);
//								
//							}
//							
//						}
//					});
//				}catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//				}			
//			}
//		}
////		System.out.println("event button: "+event.button);		
////		System.out.println("evnt has been handled "+event.x);
////		System.out.println("evnt has been handled "+event.y);
//		
//	}

	
	
}
