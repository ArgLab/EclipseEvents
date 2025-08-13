package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;



public class MenuBarClickData {
	
	private String clikedMenuBar;
	private String eventTime;
	private String username;
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public MenuBarClickData(String menuBar) {
		this.clikedMenuBar = menuBar;
		this.eventTime = dateFormat.format(new Date()); 
		
		this.username = Utils.getUsernameFromPref();
		
	}
	public String getClikedMenuBar() {
		return clikedMenuBar;
	}
	public void setClikedMenuBar(String clikedMenuBar) {
		this.clikedMenuBar = clikedMenuBar;
	}
	public String getEventTime() {
		return eventTime;
	}
	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	@Override
	public String toString() {
		return "MenuBarClickData [clikedMenuBar=" + clikedMenuBar + ", eventTime=" + eventTime + ", username="
				+ username + "]";
	}
	
	
	

}