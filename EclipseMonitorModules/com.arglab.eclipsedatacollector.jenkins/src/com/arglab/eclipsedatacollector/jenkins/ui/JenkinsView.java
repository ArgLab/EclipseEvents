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
import java.util.Map.Entry;

import javax.swing.event.TreeExpansionEvent;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JenkinsView  extends ViewPart{

	public static final String ID = "com.arglab.eclipsedatacollector.jenkins.ui.JenkinsView";

	private TreeViewer viewer;
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

		parent.setLayout(new FillLayout());

        viewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

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
	}


	private void fetchDataAndDisplay() {
		InputStream is = getClass().getResourceAsStream("JenkinsTest.json");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JsonElement rootElement = JsonParser.parseString(jsonContent.toString());

            viewer.setInput(rootElement);

            viewer.expandToLevel(2);

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		viewer.getControl().setFocus();
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
