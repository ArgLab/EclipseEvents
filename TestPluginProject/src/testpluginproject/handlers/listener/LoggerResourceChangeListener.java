package testpluginproject.handlers.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import testpluginproject.GlobalVars;
import testpluginproject.model.ResourceChangeData;
import testpluginproject.model.jsonModel.SequentialEventData;

public class LoggerResourceChangeListener implements IResourceChangeListener {
	

    public LoggerResourceChangeListener() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
                try {
                    delta.accept(new DeltaVisitor());
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	private boolean isClassFile(IFile file) {
        return file.getFileExtension() != null && file.getFileExtension().equals("class");
    }

    private String getFileContents(IFile file) throws IOException, CoreException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = file.getContents();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private class DeltaVisitor implements IResourceDeltaVisitor {

        @Override
        public boolean visit(IResourceDelta delta) {
        	SequentialEventData sed = null;
        	boolean defined = false;
        	if (delta.getKind() == IResourceDelta.ADDED) {
                // Check if the added resource is a file
                //if (delta.getResource().getType() == IResource.FILE) { //Currently just gets files. I could alternatively just capture the type and use that to inform data
            	defined = true;
				sed = new SequentialEventData("ResourceChange.Create", new ResourceChangeData(delta.getResource().getFullPath().toString(), ""));
            	//System.out.println("File created: " + delta.getResource().getFullPath());

            } else if (delta.getKind() == IResourceDelta.REMOVED) {
                // Check if the removed resource is a file
                if ((delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
                    // This is a deletion
                	defined = true;
					sed = new SequentialEventData("ResourceChange.Delete", new ResourceChangeData(delta.getResource().getFullPath().toString(), ""));
                    //System.out.println("File deleted: " + delta.getResource().getFullPath());
                }
            }
            else if (delta.getKind() == IResourceDelta.CHANGED) {
                // Check if the changed resource is a file and the content has been modified
                if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                	IFile file = (IFile) delta.getResource();
                    if (!isClassFile(file)) {
                        // If it's not a .class file, get its contents
                        try {
                            String contents = null;
							try {
								contents = getFileContents(file);
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							defined = true;
							sed = new SequentialEventData("ResourceChange.Save", new ResourceChangeData(delta.getResource().getFullPath().toString(), contents));
							
                            //System.out.println("File saved: " + file.getFullPath());
                            //System.out.println("File contents: \n" + contents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                    	defined = true;
						sed = new SequentialEventData("ResourceChange.Save", new ResourceChangeData(delta.getResource().getFullPath().toString(), ""));
                    }
                
                }
            }
        	if (defined) {
            	GlobalVars.listSequentialEvents.add(sed);
            }
            return true; // Continue visiting children
            
        }
        
    }
}
