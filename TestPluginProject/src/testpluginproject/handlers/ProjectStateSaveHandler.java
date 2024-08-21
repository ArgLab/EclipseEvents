package testpluginproject.handlers;

/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import testpluginproject.model.FileStateModel;

public class ProjectStateSaveHandler {
	
	private FileStateModel fileStateModel = null;

    public FileStateModel saveProjectState(String ProjectName, String fileName) {
        // Your code to save project state goes here
    	IPath workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation();
    	
    	// Get all projects in the workspace
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        
        for (IProject project: projects) {
        	try {
        		System.out.println("Project Name:"+project.getName());
//        		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
        		if (project.getName().equalsIgnoreCase(ProjectName)) {
                    IJavaProject javaProject = JavaCore.create(project);

                    // Iterate over source folders
                    for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
                        if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                            for (IJavaElement element : root.getChildren()) {
                                if (element instanceof IPackageFragment) {
                                    IPackageFragment pkg = (IPackageFragment) element;
                                    System.out.println("Package Name:"+pkg.getElementName());
                                    // Iterate over compilation units (files) in the package
                                    for (IJavaElement pkgElement : pkg.getChildren()) {
                                    	System.out.println("Package Element:"+pkgElement.getElementName());
                                        if (pkgElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
                                            // Read the content of the file
                                            String content = ((org.eclipse.jdt.core.ICompilationUnit) pkgElement).getSource();
                                            if(fileName.equalsIgnoreCase(pkgElement.getElementName())) {
                                            	System.out.println("Content: "+content);
                                            	FileStateModel fsm = new FileStateModel(project.getName(), pkg.getElementName(), pkgElement.getElementName(), content);
                                            	this.fileStateModel = fsm;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Exception occure due to:"+e.getMessage());
				return null;
			}
        }
        System.out.println("Project state saved successfully.");
        System.out.println(workspaceRoot.toOSString());
        return this.fileStateModel;
    }
}
