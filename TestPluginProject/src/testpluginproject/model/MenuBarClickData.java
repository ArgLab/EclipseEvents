package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

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
		return "MenuBarClickData [clikedMenuBar=" + clikedMenuBar + ", eventTime=" + eventTime + "]";
	}
	
	

}
