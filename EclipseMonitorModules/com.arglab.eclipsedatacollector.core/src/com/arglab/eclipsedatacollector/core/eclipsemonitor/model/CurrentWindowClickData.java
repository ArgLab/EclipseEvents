package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;



public class CurrentWindowClickData {
	
	private String activeWindow;
	private String eventTime;
	private String username;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public CurrentWindowClickData(String activWindow) {
		this.activeWindow = activWindow;
		this.eventTime = dateFormat.format(new Date()); 
		this.username = Utils.getUsernameFromPref();
	}

	public String getActiveWindow() {
		return activeWindow;
	}

	public void setActiveWindow(String activeWindow) {
		this.activeWindow = activeWindow;
	}

	@Override
	public String toString() {
		return "CurrentWindowClickData [activeWindow=" + activeWindow + ", eventTime=" + eventTime + ", username="
				+ username + "]";
	}

	
	

}
