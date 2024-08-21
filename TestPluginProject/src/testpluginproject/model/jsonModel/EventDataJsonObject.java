package testpluginproject.model.jsonModel;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */

import java.util.ArrayList;
import java.util.List;

import testpluginproject.model.CurrentWindowClickData;
import testpluginproject.model.UserActionData;
import testpluginproject.model.KeyBoardClickData;
import testpluginproject.model.MenuBarClickData;
import testpluginproject.model.MouseClickData;
import testpluginproject.model.WorkSpaceLog;

public class EventDataJsonObject {
	
//	private List<String> consoleOutput;
//	private List<CutCopyPasteEvent> cutcopyPasteEvents;
//	private List<MenuBarClickData> MenuBarClickActions;
//	private List<MouseClickData> mouseClickData;
//	private KeyBoardClickData keyBoardClickEvents;
	private List<SequentialEventData> sequentialEventData;
	private List<WorkSpaceLog> errorLogList;	
//	private List<CurrentWindowClickData> activewindowList;
	private String IPAddress;
	private String MACAddress;
	private String PluginVersion;
	
	public EventDataJsonObject(List<SequentialEventData> sequentialEventData,List<WorkSpaceLog> errorLogList) {
		this.errorLogList = errorLogList;
		this.sequentialEventData = sequentialEventData;
		
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


	@Override
	public String toString() {
		return "EventDataJsonObject [sequentialEventData=" + sequentialEventData + ", errorLogList=" + errorLogList
				+ ", IPAddress=" + IPAddress + ", MACAddress=" + MACAddress + ", PluginVersion=" + PluginVersion + "]";
	}




	
	

}
