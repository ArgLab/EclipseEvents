package testpluginproject.handlers.listener;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class WorkspaceChangeListener implements IResourceChangeListener{

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// TODO Auto-generated method stub
		 if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
	            try {
	                event.getDelta().accept(new IResourceDeltaVisitor() {

						@Override
						public boolean visit(IResourceDelta delta) throws CoreException {
							// TODO Auto-generated method stub
	                        if (delta.getKind() == IResourceDelta.ADDED) {
	                            // Check if a Java project was created
	                            if (delta.getResource().getType() == IResource.PROJECT) {
	                                // You can access the project name and other details here
	                                String projectName = delta.getResource().getName();
	                                System.out.println("Java project created: " + projectName);
	                            }
	                            if(delta.getResource().getType()==IResource.FILE) {
	                            		System.out.println("Created new File. "+delta.getResource().getFullPath());
	                            	
	                            }
	                            
	                        }
							else if(delta.getKind()==IResourceDelta.REMOVED) {
								if (delta.getResource().getType() == IResource.PROJECT) {
	                                // You can access the project name and other details here
	                                String projectName = delta.getResource().getName();
	                                System.out.println("Java project removed: " + projectName);
	                            }
	                            if(delta.getResource().getType()==IResource.FILE) {
	                            		System.out.println("Removed File. "+delta.getResource().getFullPath());
	                            	
	                            }
	                		
							}
	                        return true; // Continue visiting resources
						}
	                });
	            } catch (CoreException e) {
	                // Handle the exception
	                System.out.println("Exception happended. "+e.getMessage());
	            }
	        }
	}

}
