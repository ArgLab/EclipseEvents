package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

public class UserActionData {
	
//	private String EventType;
	private String content;
	private String eventTime;
	private String username;
	private String fileName;
	private String ProjectName;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public UserActionData(String content, String fileName,String projectName) {
		this.content = content;
		this.eventTime = dateFormat.format(new Date()); 
		this.username = Utils.getUsernameFromPref();
		this.fileName = fileName;
		this.ProjectName = projectName;
	}
	
//	public String getEventType() {
//		return EventType;
//	}
//	public void setEventType(String eventType) {
//		EventType = eventType;
//	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getProjectName() {
		return ProjectName;
	}

	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}

	@Override
	public String toString() {
		return "UserActionData [content=" + content + ", eventTime=" + eventTime + ", username=" + username
				+ ", fileName=" + fileName + ", ProjectName=" + ProjectName + "]";
	}


	
	
	
}
