package com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel;

import java.util.List;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.WorkSpaceLog;
import com.arglab.eclipsedatacollector.core.eclipsemonitor.utils.Utils;



public class EventDataJsonObject {
	
//	private List<String> consoleOutput;
//	private List<CutCopyPasteEvent> cutcopyPasteEvents;
//	private List<MenuBarClickData> MenuBarClickActions;
//	private List<MouseClickData> mouseClickData;
//	private KeyBoardClickData keyBoardClickEvents;
	private List<SequentialEventData> sequentialEventData;
	private List<WorkSpaceLog> errorLogList;	
//	private List<CurrentWindowClickData> activewindowList;
	private String username;
	private String IPAddress;
	private String MACAddress;
	private String PluginVersion;
	private String osName;
	private String osVersion;
	private String osArch;
	private String javaVersion;
	private String javaVendor;
	private String eclipseVersion;
	
	public EventDataJsonObject(List<SequentialEventData> sequentialEventData,List<WorkSpaceLog> errorLogList) {
		this.errorLogList = errorLogList;
		this.sequentialEventData = sequentialEventData;
		this.username = Utils.getUsernameFromPref();
		
	}

	
	public String getPluginVersion() {
		return PluginVersion;
	}


	public void setPluginVersion(String pluginVersion) {
		PluginVersion = pluginVersion;
	}


	public List<SequentialEventData> getSequentialEventData() {
		return sequentialEventData;
	}

	public void setSequentialEventData(List<SequentialEventData> sequentialEventData) {
		this.sequentialEventData = sequentialEventData;
	}

	public List<WorkSpaceLog> getErrorLogList() {
		return errorLogList;
	}

	public void setErrorLogList(List<WorkSpaceLog> errorLogList) {
		this.errorLogList = errorLogList;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
	}

	public String getMACAddress() {
		return MACAddress;
	}

	public void setMACAddress(String mACAddress) {
		MACAddress = mACAddress;
	}
	
	public void setOSInfo(String osName, String osVersion, String osArch) {
		this.osName = osName;
		this.osVersion = osVersion;
		this.osArch = osArch;
	}
	
	public void setJavaInfo(String javaVersion, String javaVendor) {
		this.javaVersion = javaVersion;
		this.javaVendor = javaVendor;
	}
	
	public void setEclipseInfo(String eclipseVersion) {
		this.eclipseVersion = eclipseVersion;
	}


	@Override
	public String toString() {
		return "EventDataJsonObject [sequentialEventData=" + sequentialEventData + ", errorLogList=" + errorLogList
				+ ", username=" + username + ", IPAddress=" + IPAddress + ", MACAddress=" + MACAddress
				+ ", PluginVersion=" + PluginVersion + ", osName=" + osName + ", osVersion=" + osVersion + ", osArch="
				+ osArch + ", javaVersion=" + javaVersion + ", javaVendor=" + javaVendor + ", eclipseVersion="
				+ eclipseVersion + "]";
	}


	




	
	

}
