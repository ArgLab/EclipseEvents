package com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.GlobalVars;



public class SequentialEventData{
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String eventType;
	private String eventTime;
	private Object eventData;
	
	public SequentialEventData(String eventType1,Object eventData){
		eventType = eventType1;
		this.eventTime = dateFormat.format(new Date()); 
		this.eventData = eventData;
	}

	@Override
	public String toString() {
		String currentFile = "";
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			IEditorPart editor = page.getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			IPath path = ((IFileEditorInput) input).getFile().getFullPath();
			currentFile = path.toFile().toString();
		} catch (NullPointerException e) {
			currentFile = GlobalVars.lastOpenFile;
		}

		return "SequentialEventData [eventType=" + eventType 
								+ ", eventTime=" + eventTime 
								+ ", eventData=" + eventData.toString()
								+ ", fullPathFile=" + currentFile 
								+ "]";
	}

	
	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	

}