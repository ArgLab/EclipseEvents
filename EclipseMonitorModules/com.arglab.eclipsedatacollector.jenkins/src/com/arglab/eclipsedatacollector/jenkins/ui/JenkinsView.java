package com.arglab.eclipsedatacollector.jenkins.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JenkinsView  extends ViewPart{

	public static final String ID = "com.arglab.eclipsedatacollector.jenkins.ui.JenkinseView";

	private TableViewer viewer;
	private List<Post> posts = new ArrayList<>();
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		 parent.setLayout(new FillLayout());
	        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
	        viewer.setContentProvider(ArrayContentProvider.getInstance());

	        TableViewerColumn titleColumn = new TableViewerColumn(viewer, SWT.NONE);
	        titleColumn.getColumn().setText("Title");
	        titleColumn.getColumn().setWidth(400);
	        titleColumn.setLabelProvider(new ColumnLabelProvider() {
	            @Override
	            public String getText(Object element) {
	                return ((Post) element).title;
	            }
	        });

	        viewer.getTable().setHeaderVisible(true);
	        viewer.getTable().setLinesVisible(true);

	        fetchDataAndDisplay();
		
	}

	private void fetchDataAndDisplay() {
		  try {
		        // Modern way using Java 11+ HttpClient
		        HttpClient client = HttpClient.newHttpClient();
		        HttpRequest request = HttpRequest.newBuilder()
		                .uri(new URI("https://jsonplaceholder.typicode.com/posts"))
		                .GET()
		                .build();

		        HttpResponse<String> response = client.send(request,
		                HttpResponse.BodyHandlers.ofString());

		        String json = response.body();

		        // Parse JSON with Gson
		        Gson gson = new Gson();
		        List<Post> postList = gson.fromJson(json,
		                new TypeToken<List<Post>>(){}.getType());

		        posts.addAll(postList);
		        viewer.setInput(posts);

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		viewer.getControl().setFocus();
		
	}

	static class Post {
	    int userId;
	    int id;
	    String title;
	    String body;
	}
	
}
