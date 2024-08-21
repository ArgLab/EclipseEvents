package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

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
		return "CurrentWindowClickData [activeWindow=" + activeWindow + ", eventTime=" + eventTime + "]";
	}
	
	

}
