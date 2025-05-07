package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;

public class KeyBoardClickData {
	
	private String contents;
	private String username;
	private String currentWindow;
	
	public KeyBoardClickData(String windowName,String contents) {
		this.contents = contents;
		this.username = Utils.getUsernameFromPref();
		this.currentWindow = windowName;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(char contents) {
		this.contents += contents;
	}
	
	

	public String getCurrentWindow() {
		return currentWindow;
	}

	public void setCurrentWindow(String currentWindow) {
		this.currentWindow = currentWindow;
	}

	@Override
	public String toString() {
		return "KeyBoardClickData [contents=" + contents + ", username=" + username + ", currentWindow=" + currentWindow
				+ "]";
	}


}
