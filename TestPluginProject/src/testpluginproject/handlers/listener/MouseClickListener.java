package testpluginproject.handlers.listener;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import testpluginproject.model.MouseClickData;
import testpluginproject.model.jsonModel.SequentialEventData;

public class MouseClickListener implements MouseListener, Listener{
	
	IWorkbenchWindow window;
	List<SequentialEventData> mouseClickData;
	public MouseClickListener(IWorkbenchWindow window) {
		this.mouseClickData = new ArrayList<>();
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
		// TODO Auto-generated method stub
		if(window.getActivePage()!=null) {
			if(window.getActivePage().getActivePart()!=null) {
				try {
					window.getWorkbench().getDisplay().timerExec(200, new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub			
							if(window.getActivePage().getActivePart().getTitle()!=null) {
								
								IWorkbenchPage page = window.getActivePage(); //Get active page
								int lineNumber = 0;
								int columnNumber = 0;
										
							    if (page != null) {
							        IEditorPart editorPart = page.getActiveEditor(); //Get editor
							        if (editorPart instanceof ITextEditor) {
							            ITextEditor textEditor = (ITextEditor) editorPart; //If textEditor then cast it to editorPart
							            IDocumentProvider docProvider = textEditor.getDocumentProvider(); //Get document provider
							            IDocument document = docProvider.getDocument(textEditor.getEditorInput()); //Get document
							            ISelectionProvider selectionProvider = textEditor.getSite().getSelectionProvider(); //Get selection provider
							            ITextSelection textSelection = (ITextSelection) selectionProvider.getSelection(); //Get text selection
							            lineNumber = textSelection.getStartLine(); //Get start line
										try {
											columnNumber = textSelection.getOffset()-document.getLineInformation(lineNumber).getOffset(); //Get offset by total offset-linenumber's offset
											System.out.println("Line: " + (lineNumber + 1)); //Print line
								            System.out.println("Column: " + (columnNumber + 1)); //Print Column number
										} catch (org.eclipse.jface.text.BadLocationException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
							        }
							    }
								
								System.out.println("MouseClick to Window: "+window.getActivePage().getActivePart().getTitle());
								MouseClickData mcd = new MouseClickData(event.x, event.y, window.getActivePage().getActivePart().getTitle(),lineNumber+1,columnNumber + 1);
								SequentialEventData sedMC = new SequentialEventData("MouseClickEvent", mcd);
								mouseClickData.add(sedMC);
								
							}
							
						}
					});
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}			
			}
		}
//		System.out.println("event button: "+event.button);		
//		System.out.println("evnt has been handled "+event.x);
//		System.out.println("evnt has been handled "+event.y);
		
	}

	
	
}
