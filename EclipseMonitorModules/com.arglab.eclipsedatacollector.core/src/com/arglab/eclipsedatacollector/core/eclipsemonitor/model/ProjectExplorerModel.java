package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;

public class ProjectExplorerModel {
	private String onMouseClick;
	private String PathClick;
	private String username;
	
	public ProjectExplorerModel() {
		this.username = Utils.getUsernameFromPref();
	}
	public String getMouseClick() {
		return onMouseClick;
	}
	public void setMouseClick(String mouseClick) {
		onMouseClick = mouseClick;
	}
	public String getPathClick() {
		return PathClick;
	}
	public void setPathClick(String pathClick) {
		PathClick = pathClick;
	}
	@Override
	public String toString() {
		return "ProjectExplorerModel [MouseClick=" + onMouseClick + ", PathClick=" + PathClick + ", username=" + username
				+ "]";
	}

	
	
	
	

}
