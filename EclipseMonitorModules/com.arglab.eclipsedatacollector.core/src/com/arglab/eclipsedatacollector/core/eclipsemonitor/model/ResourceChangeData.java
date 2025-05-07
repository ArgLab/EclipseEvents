package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import java.text.SimpleDateFormat;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;



public class ResourceChangeData {
	
	//Type, Files affected, Code? (Save/Load only?)
	private String FileAffected;
	private String code;
	private String username;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public ResourceChangeData(String fileAffected, String code) {
		super();
		FileAffected = fileAffected;
		this.code = code;
		
		this.username = Utils.getUsernameFromPref();
	}

	public String getFileAffected() {
		return FileAffected;
	}
	public void setFileAffected(String fileAffected) {
		FileAffected = fileAffected;
	}

	
	@Override
	public String toString() {
		return "ResourceChangeData [FileAffected=" + FileAffected + ", code=" + code + ", username=" + username + "]";
	}
	
	

}

