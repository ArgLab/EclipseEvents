package testpluginproject.handlers.listener;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import testpluginproject.GlobalVars;
import testpluginproject.handlers.ProjectStateSaveHandler;
import testpluginproject.model.CurrentWindowClickData;
import testpluginproject.model.jsonModel.SequentialEventData;

public class WindowClickListener implements IPartListener {
	
	List<SequentialEventData> activeWindow;
	public WindowClickListener(List<SequentialEventData> activeWindowList, IWorkbenchWindow window) {
		this.activeWindow = activeWindowList;
		System.out.println("inside the menubar listener!");
		System.out.println("Current Active Window is: "+window.getPartService().getActivePart().getTitle());
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		/*
		 * If the part is an editor, set it as the new 
		 * */
		if(part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			if(input != null && input instanceof IFileEditorInput) {
				IPath path = ((IFileEditorInput) input).getFile().getFullPath();
				GlobalVars.lastOpenFile = path.toFile().toString();
			}
		}
		
		
		this.activeWindow.add(new SequentialEventData("WindowClickEvent",part.getTitle()));
		System.out.println("Activated Window: "+part.getSite().getWorkbenchWindow().getPartService().getActivePart().getTitle());
//		System.out.println("part Activated: "+part.getClass());
		
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		String ProjectName = null;
		String fileName = null;
		if(part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			if(input != null && input instanceof IFileEditorInput) {
				IPath path = ((IFileEditorInput) input).getFile().getFullPath();
				GlobalVars.lastOpenFile = path.toFile().toString();
	        	/***Added code for checking first time opened a file. ***/
				System.out.println(path.toFile().toString());
				//String[] parts = path.toFile().toString().split("\\\\");
				String dirPath = path.toFile().getAbsolutePath();
				String[] parts  = dirPath.split(Matcher.quoteReplacement(System.getProperty("file.separator")));
				ProjectName = parts[1];
				fileName = parts[parts.length-1];
//	        	}
			}
		}
		
		if(part instanceof AbstractTextEditor) {
			new FileChangeEventListener((AbstractTextEditor) part,ProjectName,fileName);
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		if(part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			if(input != null && input instanceof IFileEditorInput) {
				IPath path = ((IFileEditorInput) input).getFile().getFullPath();
				if(GlobalVars.lastOpenFile.equals(path.toFile().toString()));
					GlobalVars.lastOpenFile = "undefined";
			}
		}
//		part.dispose();
//		if (part instanceof org.eclipse.ui.IWorkbenchPart) {
//			part.dispose();
////            org.eclipse.ui.internal.WorkbenchWindow workbenchWindow = (org.eclipse.ui.internal.WorkbenchWindow) part;
////            if (workbenchWindow.getMenuBarManager() == null) {
////                // Main menu bar closed, handle the event here
////                System.out.println("Menu bar closed");
////            }
//        }
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		if(part instanceof IEditorPart) {
			IEditorPart editor = (IEditorPart) part;
			IEditorInput input = editor.getEditorInput();
			if(input != null && input instanceof IFileEditorInput) {
				IPath path = ((IFileEditorInput) input).getFile().getFullPath();
				GlobalVars.lastOpenFile = path.toFile().toString();

//	        	ProjectStateSaveHandler pssh = new ProjectStateSaveHandler();
//				pssh.saveProjectState(projectName,fileName);
			}
		}
		if (part instanceof org.eclipse.ui.internal.WorkbenchWindow) {
            org.eclipse.ui.internal.WorkbenchWindow workbenchWindow = (org.eclipse.ui.internal.WorkbenchWindow) part;
            if (workbenchWindow.getMenuBarManager() != null) {
                // Main menu bar opened, handle the event here
            	if(workbenchWindow.getMenuBarManager().getId()!=null) {
            		System.out.println("Menu bar opened. "+workbenchWindow.getMenuBarManager().getId());
            	}
            }
        }
	}

}
