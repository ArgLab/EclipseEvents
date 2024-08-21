package testpluginproject.handlers.listener;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import testpluginproject.GlobalVars;
import testpluginproject.handlers.ProjectStateSaveHandler;
import testpluginproject.model.FileStateModel;
import testpluginproject.model.jsonModel.SequentialEventData;

public class FileChangeEventListener implements IDocumentListener{
	
	private int charCount = 0;
	private static final int THRESHOLD = 100;
	
	private AbstractTextEditor editor;
	private String projectName;
	private String fileName;
	
	public FileChangeEventListener(AbstractTextEditor editor,String ProjectName,String fileName) {
		this.editor = editor;
		this.projectName = ProjectName;
		this.fileName = fileName;
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		document.addDocumentListener(this);
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		// TODO Auto-generated method stub
		charCount += event.getText().length();
		if (charCount >= THRESHOLD) {
			ProjectStateSaveHandler pssh = new ProjectStateSaveHandler();
			FileStateModel fsm = pssh.saveProjectState(this.projectName,this.fileName);
			if(fsm!=null) {
				SequentialEventData sed = new SequentialEventData("Save File State",fsm);
				GlobalVars.listSequentialEvents.add(sed);
			}
            charCount = 0; // Reset the counter
        }
		
		
	}

}
