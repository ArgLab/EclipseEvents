package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
<<<<<<< Updated upstream
import java.util.TreeMap;
=======
>>>>>>> Stashed changes
import java.util.Map.Entry;
import java.util.TreeMap;

<<<<<<< Updated upstream
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
=======
import javax.swing.event.TreeExpansionEvent;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
>>>>>>> Stashed changes
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.*;
<<<<<<< Updated upstream
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
=======
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
>>>>>>> Stashed changes
import org.eclipse.ui.part.ViewPart;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JenkinsView extends ViewPart implements IResourceChangeListener{

	public static final String ID = "com.arglab.eclipsedatacollector.jenkins.ui.JenkinsView";

	private TreeViewer viewer;
	
<<<<<<< Updated upstream
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);

        // Watch workspace so we can close the view if ABC disappears/closes
        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            this,
            IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
        );
        
    }
	
	private boolean isAbcPresent() {
        IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject("csc216-GP3-TS");
        return p.exists() && p.isOpen();
    }

	@Override
	public void createPartControl(Composite parent) {
=======
	private IPartListener2 partListener;
	private String lastProjectName = null;
	
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
>>>>>>> Stashed changes
		
		Composite container = new Composite(parent, SWT.NONE);
	    container.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
	    
	    // Add the fetch button
	    Button fetchButton = new Button(container, SWT.PUSH);
	    fetchButton.setText("Fetch");
	    fetchButton.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	    fetchButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
	        @Override
	        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
<<<<<<< Updated upstream
	            if (isAbcPresent()) {
	                fetchDataAndDisplay();
	            } else {
	                clearViewer();
	                // Optionally show a message
	                System.out.println("Project 'ABC' is not open. Please open it first.");
	            }
=======
	        	fetchDataAndDisplay();
	        }
	    });

        viewer = new TreeViewer(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        viewer.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.setContentProvider(new JSONContentProvider());
        viewer.setLabelProvider(new JSONLabelProvider());

        Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);

        TreeColumn column = new TreeColumn(tree, SWT.LEFT);
        column.setText("JSON file");
        column.setWidth(400);

        viewer.addTreeListener(new ITreeViewerListener() {
            
        	@Override
            public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
                Object element = event.getElement();
                if (element instanceof JSONTreeNode) {
                    JSONTreeNode node = (JSONTreeNode) element;
                    System.out.println("Expanded: " + node.getKey());
                }
            }

        	@Override
            public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
                Object element = event.getElement();
                if (element instanceof JSONTreeNode) {
                    JSONTreeNode node = (JSONTreeNode) element;
                    System.out.println("Collapsed: " + node.getKey());
                }
            }
        });

        fetchDataAndDisplay();
        setupFileChangeListener();
	}
	
	private void setupFileChangeListener() {
	    IWorkbenchPage page = getSite().getPage();
	    
	    partListener = new IPartListener2() {
	        @Override
	        public void partActivated(IWorkbenchPartReference partRef) {
	            IWorkbenchPart part = partRef.getPart(false);
	            if (part instanceof IEditorPart) {
	                IEditorPart editor = (IEditorPart) part;
	                IEditorInput input = editor.getEditorInput();
	                
	                if (input instanceof IFileEditorInput) {
	                    IFileEditorInput fileInput = (IFileEditorInput) input;
	                    IFile file = fileInput.getFile();
	                    String currentProjectName = file.getProject().getName();
	                    
	                    // Only fetch if project changed
	                    if (!currentProjectName.equals(lastProjectName)) {
	                        lastProjectName = currentProjectName;
	                        System.out.println("Project changed to: " + currentProjectName);
	                        fetchDataAndDisplay();
	                    }
	                }
	            }
	        }
	    };
	    
	    page.addPartListener(partListener);
	}

	@Override
	public void dispose() {
	    // Clean up listener when view is closed
	    if (partListener != null) {
	        getSite().getPage().removePartListener(partListener);
	    }
	    super.dispose();
	}


	private void fetchDataAndDisplay() {
	    Display.getDefault().asyncExec(() -> {
	        try {
//	            String repo = "csc216-2025-fall-P2-002-003";//JenkinsUtil.detectActiveProjectName();
	            String repo = JenkinsUtil.detectActiveProjectName();
	            if (repo == null) {
	                System.out.println("No active project detected");
	                return;
	            }
	            else {
	            	System.out.println("Repo is "+ repo);
	            }
	            
	            String unityId = Utils.getUsernameFromPref();
	            String json = JenkinsUtil.fetchLatestBuildJson(repo, unityId);
	            if (json == null) throw new RuntimeException("Empty response");

                JsonElement root = JsonParser.parseString(json);
                JsonObject transformedData = transformDataByClassName(root.getAsJsonObject());
                
                Display.getDefault().asyncExec(() -> {
                    if (!viewer.getControl().isDisposed()) {
                        viewer.setInput(transformedData);
                        viewer.expandToLevel(2);
                        getSite().getShell().setCursor(null);
                        viewer.getControl().setEnabled(true);
                    }
                });

	        } catch (Exception e) {
	            System.out.println("Exception Happened here due to "+e.getMessage());
>>>>>>> Stashed changes
	        }
	    });
	    
	    // Create the tree viewer
	    viewer = new TreeViewer(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
	    viewer.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));

	    viewer.setContentProvider(new JSONContentProvider());
	    viewer.setLabelProvider(new JSONLabelProvider());

	    Tree tree = viewer.getTree();
	    tree.setHeaderVisible(true);

	    TreeColumn column = new TreeColumn(tree, SWT.LEFT);
	    column.setText("JSON file");
	    column.setWidth(400);

	    viewer.addTreeListener(new ITreeViewerListener() {
	        
	        @Override
	        public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
	            Object element = event.getElement();
	            if (element instanceof JSONTreeNode) {
	                JSONTreeNode node = (JSONTreeNode) element;
	                System.out.println("Expanded: " + node.getKey());
	            }
	        }

	        @Override
	        public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
	            Object element = event.getElement();
	            if (element instanceof JSONTreeNode) {
	                JSONTreeNode node = (JSONTreeNode) element;
	                System.out.println("Collapsed: " + node.getKey());
	            }
	        }
	    });

	    // Only fetch data initially if ABC is present
	    if (isAbcPresent()) {
	        fetchDataAndDisplay();
	    }
	}
<<<<<<< Updated upstream

	private void fetchDataAndDisplay() {
//		InputStream is = getClass().getResourceAsStream("sample.json");
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//			StringBuilder jsonContent = new StringBuilder();
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				jsonContent.append(line);
//			}
//
//			JsonElement rootElement = JsonParser.parseString(jsonContent.toString());
//			
//			JsonObject transformedData = transformDataByClassName(rootElement.getAsJsonObject());
//			
//			viewer.setInput(transformedData);
//			viewer.expandToLevel(2);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
//		String UserId = Utils.getUsernameFromPref();
		String UserId = "sesmith5";
		String database = "miner";
		String repoName = "csc216-GP3-TS";//JenkinsUtil.getActiveProjectAndFile();
		
		String userToken = JenkinsUtil.getUserHash(repoName, UserId, database);
        if (userToken == null) {
            MessageBox mb = new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
            mb.setText("Token error");
            mb.setMessage("Failed to generate user token.");
            mb.open();
            return;
        }
        
        String url = "http://127.0.0.1:8080/api/build-info/latest/" + JenkinsUtil.encodePath(repoName);

        getSite().getShell().setCursor(getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        viewer.getControl().setEnabled(false);
        
        // Do network off the UI thread
        new Thread(() -> {
            try {
                String json = JenkinsUtil.fetchJsonFromApi(url, JenkinsUtil.buildRequestBody(userToken));
                if (json == null) throw new RuntimeException("Empty response");

                JsonElement root = JsonParser.parseString(json);
                JsonObject transformedData = transformDataByClassName(root.getAsJsonObject());
                
                Display.getDefault().asyncExec(() -> {
                    if (!viewer.getControl().isDisposed()) {
                        viewer.setInput(transformedData);
                        viewer.expandToLevel(2);
                        getSite().getShell().setCursor(null);
                        viewer.getControl().setEnabled(true);
                    }
                });

            } catch (Throwable t) {
                t.printStackTrace();
                Display.getDefault().asyncExec(() -> {
                    if (!getSite().getShell().isDisposed()) {
                        getSite().getShell().setCursor(null);
                        viewer.getControl().setEnabled(true);
                        MessageBox mb = new MessageBox(getSite().getShell(), SWT.ICON_ERROR | SWT.OK);
                        mb.setText("Request failed");
                        mb.setMessage("Could not fetch data:\n" + t.getMessage());
                        mb.open();
                    }
                });
            }
        }, "JenkinsView-Fetch").start();
	}

=======
	
>>>>>>> Stashed changes
	private JsonObject transformDataByClassName(JsonObject originalData) {
		JsonObject result = new JsonObject();
		
		Map<String, JsonObject> classBuckets = new TreeMap<>();
		
<<<<<<< Updated upstream

		String[] classArrays = {"checkstyleNotifications", "pmdNotifications", "coverageData", 
								"studentUnitTests", "tsUnitTests", "countsData"};
		
		for (String arrayName : classArrays) {
			if (originalData.has(arrayName) && originalData.get(arrayName).isJsonArray()) {
				JsonArray array = originalData.getAsJsonArray(arrayName);
				
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						JsonObject obj = element.getAsJsonObject();
						String className = extractClassName(obj, arrayName);
						
						if (className != null && !className.trim().isEmpty()) {
							if (!classBuckets.containsKey(className)) {
								classBuckets.put(className, new JsonObject());
							}
							
							JsonObject classBucket = classBuckets.get(className);

							if (arrayName.equals("studentUnitTests") || arrayName.equals("tsUnitTests")) {
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonObject());
								}
								
								JsonObject testContainer = classBucket.getAsJsonObject(arrayName);
								addTestMethod(testContainer, obj);
							} else {
								// Regular handling for other arrays
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonArray());
								}
								classBucket.getAsJsonArray(arrayName).add(element);
							}
						}
					}
				}
			}
		}
		
		for (Map.Entry<String, JsonObject> entry : classBuckets.entrySet()) {
			result.add(entry.getKey(), entry.getValue());
		}
		
		JsonObject otherDetails = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : originalData.entrySet()) {
			String key = entry.getKey();
			boolean isClassArray = false;
			
			for (String arrayName : classArrays) {
				if (key.equals(arrayName)) {
					isClassArray = true;
					break;
				}
			}
			
			if (!isClassArray) {
				otherDetails.add(key, entry.getValue());
			}
		}
		
		if (otherDetails.size() > 0) {
			result.add("Other Details", otherDetails);
		}
		
		return result;
	}
	
	private void addTestMethod(JsonObject testContainer, JsonObject testObj) {
		String methodName = testObj.has("methodName") ? testObj.get("methodName").getAsString() : "unknown";
		boolean failed = testObj.has("fail") ? testObj.get("fail").getAsBoolean() : false;
		String failMsg = testObj.has("failMsg") ? testObj.get("failMsg").getAsString() : "";
		
		String status = failed ? "fail" : "pass";
		if (failed && !failMsg.trim().isEmpty()) {
			status += " - " + failMsg;
		}
		
		testContainer.addProperty(methodName, status);
	}
	
	private String extractClassName(JsonObject obj, String arrayType) {
		String className = null;

		switch (arrayType) {
			case "checkstyleNotifications":
			case "pmdNotifications":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "coverageData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			case "studentUnitTests":
			case "tsUnitTests":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "countsData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			default:
				return null;
		}
		

		return normalizeClassName(className);
	}
	
	private String normalizeClassName(String className) {
		if (className == null || className.trim().isEmpty()) {
			return className;
		}
		
		if (className.toLowerCase().startsWith("ts") && className.length() > 2) {
			if (Character.isUpperCase(className.charAt(2))) {
				return className.substring(2);
			}
		}

		
		return className;
	}
=======
>>>>>>> Stashed changes

		String[] classArrays = {"checkstyleNotifications", "pmdNotifications", "coverageData", 
								"studentUnitTests", "tsUnitTests", "countsData"};
		
		for (String arrayName : classArrays) {
			if (originalData.has(arrayName) && originalData.get(arrayName).isJsonArray()) {
				JsonArray array = originalData.getAsJsonArray(arrayName);
				
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						JsonObject obj = element.getAsJsonObject();
						String className = extractClassName(obj, arrayName);
						
						if (className != null && !className.trim().isEmpty()) {
							if (!classBuckets.containsKey(className)) {
								classBuckets.put(className, new JsonObject());
							}
							
							JsonObject classBucket = classBuckets.get(className);

							if (arrayName.equals("studentUnitTests") || arrayName.equals("tsUnitTests")) {
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonObject());
								}
								
								JsonObject testContainer = classBucket.getAsJsonObject(arrayName);
								addTestMethod(testContainer, obj);
							} else {
								// Regular handling for other arrays
								if (!classBucket.has(arrayName)) {
									classBucket.add(arrayName, new JsonArray());
								}
								classBucket.getAsJsonArray(arrayName).add(element);
							}
						}
					}
				}
			}
		}
		
		
		
		for (Map.Entry<String, JsonObject> entry : classBuckets.entrySet()) {
			result.add(entry.getKey(), entry.getValue());
		}
		
		JsonObject otherDetails = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : originalData.entrySet()) {
			String key = entry.getKey();
			boolean isClassArray = false;
			
			for (String arrayName : classArrays) {
				if (key.equals(arrayName)) {
					isClassArray = true;
					break;
				}
			}
			
			if (!isClassArray) {
				otherDetails.add(key, entry.getValue());
			}
		}
		
		if (otherDetails.size() > 0) {
			result.add("Other Details", otherDetails);
		}
		
		return result;
	}
	
	private void addTestMethod(JsonObject testContainer, JsonObject testObj) {
		String methodName = testObj.has("methodName") ? testObj.get("methodName").getAsString() : "unknown";
		boolean failed = testObj.has("fail") ? testObj.get("fail").getAsBoolean() : false;
		String failMsg = testObj.has("failMsg") ? testObj.get("failMsg").getAsString() : "";
		
		String status = failed ? "fail" : "pass";
		if (failed && !failMsg.trim().isEmpty()) {
			status += " - " + failMsg;
		}
		
		testContainer.addProperty(methodName, status);
	}
	
	private String extractClassName(JsonObject obj, String arrayType) {
		String className = null;

		switch (arrayType) {
			case "checkstyleNotifications":
			case "pmdNotifications":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "coverageData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			case "studentUnitTests":
			case "tsUnitTests":
				className = obj.has("className") ? obj.get("className").getAsString() : null;
				break;
			case "countsData":
				className = obj.has("classname") ? obj.get("classname").getAsString() : null;
				break;
			default:
				return null;
		}
		

		return normalizeClassName(className);
	}
	
	private String normalizeClassName(String className) {
		if (className == null || className.trim().isEmpty()) {
			return className;
		}
		
		if (className.toLowerCase().startsWith("ts") && className.length() > 2) {
			if (Character.isUpperCase(className.charAt(2))) {
				return className.substring(2);
			}
		}

		
		return className;
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // If ABC is no longer present/open, clear the viewer
        if (!isAbcPresent()) {
            Display.getDefault().asyncExec(() -> {
                clearViewer();
            });
        }
    }
    
    private void clearViewer() {
        if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.setInput(null);
            viewer.refresh();
        }
    }
    
	class JSONContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) inputElement;
				List<JSONTreeNode> elements = new ArrayList<>();

				for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					elements.add(new JSONTreeNode(entry.getKey(), entry.getValue()));
				}

				return elements.toArray();
			} else if (inputElement instanceof JsonArray) {
				JsonArray jsonArray = (JsonArray) inputElement;
				List<JSONTreeNode> elements = new ArrayList<>();

				for (int i = 0; i < jsonArray.size(); i++) {
					elements.add(new JSONTreeNode("[" + i + "]", jsonArray.get(i)));
				}

				return elements.toArray();
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) parentElement;
				JsonElement value = node.getValue();

				if (value.isJsonObject()) {
					JsonObject jsonObject = value.getAsJsonObject();
					List<JSONTreeNode> children = new ArrayList<>();

					for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
						children.add(new JSONTreeNode(entry.getKey(), entry.getValue()));
					}

					return children.toArray();
				} else if (value.isJsonArray()) {
					JsonArray jsonArray = value.getAsJsonArray();
					List<JSONTreeNode> children = new ArrayList<>();

					for (int i = 0; i < jsonArray.size(); i++) {
						children.add(new JSONTreeNode("[" + i + "]", jsonArray.get(i)));
					}

					return children.toArray();
				}
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) element;
				JsonElement value = node.getValue();
				return value.isJsonObject() || value.isJsonArray();
			}
			return false;
		}
	}

	class JSONLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof JSONTreeNode) {
				JSONTreeNode node = (JSONTreeNode) element;
				String key = node.getKey();
				JsonElement value = node.getValue();

				if (value.isJsonPrimitive()) {
					JsonPrimitive primitive = value.getAsJsonPrimitive();
					if (primitive.isString()) {
						return key + ": \"" + primitive.getAsString() + "\"";
					} else {
						return key + ": " + primitive.toString();
					}
				} else if (value.isJsonObject()) {
					return key;
				} else if (value.isJsonArray()) {
					JsonArray array = value.getAsJsonArray();
					return key + " (" + array.size() + " items)";
				} else if (value.isJsonNull()) {
					return key + ": null";
				}
			}
			return super.getText(element);
		}
	}

	class JSONTreeNode {
		private String key;       
		private JsonElement value; 

		public JSONTreeNode(String key, JsonElement value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public JsonElement getValue() {
			return value;
		}
	}
}