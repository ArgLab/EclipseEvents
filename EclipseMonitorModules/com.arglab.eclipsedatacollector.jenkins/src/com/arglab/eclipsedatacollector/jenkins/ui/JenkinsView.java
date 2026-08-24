package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.event.TreeExpansionEvent;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.net.Proxy;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.GlobalVars;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;

public class JenkinsView  extends ViewPart{

	public static final String ID = "com.arglab.eclipsedatacollector.jenkins.ui.JenkinsView";
	
	private Browser browser;
	
	private IPartListener2 partListener;
	private String lastProjectName = null;
	
	private TreeViewer viewer;
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
//		
//		Composite container = new Composite(parent, SWT.NONE);
//	    container.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
//	    
//	    Button fetchButton = new Button(container, SWT.PUSH);
//	    fetchButton.setText("Fetch");
//	    fetchButton.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//	    fetchButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
//	        @Override
//	        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
//	        	fetchDataAndDisplay();
//	        }
//	    });
//	    
//	    public void createPartControl(Composite parent) {
//	    	
//	    }
//
//	    
////		parent.setLayout(new FillLayout());
//
//        viewer = new TreeViewer(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
//        viewer.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));
//
//
//        viewer.setContentProvider(new JSONContentProvider());
//
//        viewer.setLabelProvider(new JSONLabelProvider());
//
//        Tree tree = viewer.getTree();
//        tree.setHeaderVisible(true);
//
//        TreeColumn column = new TreeColumn(tree, SWT.LEFT);
//        column.setText("JSON file");
//        column.setWidth(400);
//
//        viewer.addTreeListener(new ITreeViewerListener() {
//            
//        	@Override
//            public void treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent event) {
//                Object element = event.getElement();
//                if (element instanceof JSONTreeNode) {
//                    JSONTreeNode node = (JSONTreeNode) element;
//                    System.out.println("Expanded: " + node.getKey());
//                }
//            }
//
//        	@Override
//            public void treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent event) {
//                Object element = event.getElement();
//                if (element instanceof JSONTreeNode) {
//                    JSONTreeNode node = (JSONTreeNode) element;
//                    System.out.println("Collapsed: " + node.getKey());
//                }
//            }
//        });
//
//        fetchDataAndDisplay();
//        setupFileChangeListener();
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));

		Button fetchButton = new Button(container, SWT.PUSH);
		fetchButton.setText("Fetch");
		fetchButton.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		fetchButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
		    @Override
		    public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
		        loadReport();
		    }
		});

		browser = new Browser(container, SWT.NONE);
		browser.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));
		
		new BrowserFunction(browser, "javaLog") {
		    @Override
		    public Object function(Object[] arguments) {
		        String msg = (arguments.length > 0 && arguments[0] != null) ? arguments[0].toString() : "";
		        Utils.logDashboardClick(msg);
		        System.out.println("[JS click] " + msg);
		        return null;
		    }
		};

		loadReport();
		setupFileChangeListener();
	}
	
	private void loadReport() {
	    Display.getDefault().asyncExec(() -> {
	        try {
	        	String repo = JenkinsUtil.detectActiveGitRepoName();
	            if (repo == null) {
	                System.out.println("No active project detected");
	                return;
	            }
	            else {
	            	System.out.println("Repo is "+ repo);
	            }

	            String unityId = Utils.getUsernameFromPref();
	            String json = JenkinsUtil.fetchLatestBuildJson(repo, unityId);
//	            String json = fetchJsonLocal();
	            if (json == null || json.isEmpty()) throw new RuntimeException("Empty response");

	            String url = buildReportUrl(json);
	            if (!browser.isDisposed()) browser.setUrl(url);
	        } catch (Exception e) {
	            if (!browser.isDisposed()) {
	                browser.setText("<html><body style='font-family:sans-serif;padding:16px'>"
	                    + "<h3>Could not load report</h3><p>" + e.getMessage() + "</p></body></html>");
	            }
	            System.out.println("Report load failed: " + e.getMessage());
	        }
	    });
	}
	
	private String fetchJsonLocal() throws Exception {
	    URL url = new URL("http://127.0.0.1:8000/sample.json");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Accept", "application/json");
	    conn.setConnectTimeout(10000);
	    conn.setReadTimeout(10000);
	    if (conn.getResponseCode() != 200) throw new RuntimeException("HTTP " + conn.getResponseCode());

	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) sb.append(line);
	    reader.close();
	    conn.disconnect();
	    return sb.toString();
	}

	private String buildReportUrl(String json) throws Exception {
	    Bundle bundle = FrameworkUtil.getBundle(getClass());
	    File indexFile = new File(FileLocator.toFileURL(bundle.getEntry("web/index.html")).toURI());
	    File webDir = indexFile.getParentFile();

	    String html = new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8);

	    // swap the report-data island's contents with the freshly fetched JSON
	    html = html.replaceFirst(
	        "(?s)(<script id=\"report-data\"[^>]*>).*?(</script>)",
	        "$1" + Matcher.quoteReplacement(json) + "$2");
	    

	    // write a rendered copy next to styles.css / app.js so relative paths resolve
	    File rendered = new File(webDir, "_rendered.html");
	    Files.write(rendered.toPath(), html.getBytes(StandardCharsets.UTF_8));
	    
	    return rendered.toURI().toString();
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


//	private void fetchDataAndDisplay() {
//	    Display.getDefault().asyncExec(() -> {
//	        try {
//	        	
//	        	
//	            String repo = JenkinsUtil.detectActiveProjectName();
//	            if (repo == null) {
//	                System.out.println("No active project detected");
//	                return;
//	            }
//	            else {
//	            	System.out.println("Repo is "+ repo);
//	            }
//
//
//	            String unityId = Utils.getUsernameFromPref();
//	            String json = JenkinsUtil.fetchLatestBuildJson(repo, unityId);
//	            if (json == null) throw new RuntimeException("Empty response");
//	            
//	            JsonElement root = JsonParser.parseString(json);
//				JsonObject transformedData = transformDataByClassName(root.getAsJsonObject());
//				  
//				Display.getDefault().asyncExec(() -> {
//				    if (!viewer.getControl().isDisposed()) {
//				        viewer.setInput(transformedData);
//				        viewer.expandToLevel(2);
//				        getSite().getShell().setCursor(null);
//				        viewer.getControl().setEnabled(true);
//				    }
//				});
//
//	        } catch (Exception e) {
//	        	String msg = "Exception Happened here due to "+e.getMessage();
//	        	String userMsg = "Please check the repo exists with the name and you are connected to eduroam or NCSU VPN";
//	        	JsonObject errorRoot = new JsonObject();
//	            errorRoot.addProperty("Error", userMsg);
//	            
//	            Display.getDefault().asyncExec(() -> {
//				    if (!viewer.getControl().isDisposed()) {
//				        viewer.setInput(errorRoot);
//				        viewer.expandToLevel(2);
//				        getSite().getShell().setCursor(null);
//				        viewer.getControl().setEnabled(true);
//				    }
//				});
//	            System.out.println(msg);
//	        }
//	    });
//	}
	
	private void fetchDataAndDisplay() {
	    Display.getDefault().asyncExec(() -> {
	        try {
	            String urlStr = "http://127.0.0.1:8000/sample.json";

	            URL url = new URL(urlStr);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Accept", "application/json");
	            conn.setConnectTimeout(10000);
	            conn.setReadTimeout(10000);

	            int status = conn.getResponseCode();
	            if (status != 200) {
	                throw new RuntimeException("HTTP " + status);
	            }

	            BufferedReader reader =
	                new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line);
	            }
	            reader.close();
	            conn.disconnect();

	            String json = sb.toString();
	            if (json == null || json.isEmpty()) throw new RuntimeException("Empty response");

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
	            String msg = "Exception Happened here due to " + e.getMessage();
	            String userMsg = "Please check the local server is running on 127.0.0.1:8000 and sample.json exists";
	            JsonObject errorRoot = new JsonObject();
	            errorRoot.addProperty("Error", userMsg);

	            Display.getDefault().asyncExec(() -> {
	                if (!viewer.getControl().isDisposed()) {
	                    viewer.setInput(errorRoot);
	                    viewer.expandToLevel(2);
	                    getSite().getShell().setCursor(null);
	                    viewer.getControl().setEnabled(true);
	                }
	            });
	            System.out.println(msg);
	        }
	    });
	}
	
	private JsonObject transformDataByClassName(JsonObject originalData) {
		JsonObject result = new JsonObject();
		
		Map<String, JsonObject> classBuckets = new TreeMap<>();
		

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
		// TODO Auto-generated method stub
		browser.setFocus();
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

    // Label provider to display appropriate text for each JSON node
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
                    return key;
                } else if (value.isJsonNull()) {
                    return key;
                }
            }
            return super.getText(element);
        }
    }

    // Helper class to represent JSON tree nodes
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