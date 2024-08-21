package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

//import com.fasterxml.jackson.annotation.JsonFormat;

public class WorkSpaceLog implements Comparable<WorkSpaceLog>{
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private String date;
    private String severity;
    private String plugin;
    private String message;
    private String sessionInfo;
    private String username;
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    public WorkSpaceLog(Date date, String severity, String plugin, String message, String sessionInfo) {
        this.date = dateFormat.format(date);
        this.severity = severity;
        this.plugin = plugin;
        this.message = message;
        this.sessionInfo = sessionInfo;
        
        this.username = Utils.getUsernameFromPref();
    }

	public WorkSpaceLog() {
    	
    }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "LogEntry [date=" + date + ", severity=" + severity + ", plugin=" + plugin + ", message=" + message
				+ ", sessionInfo=" + sessionInfo + "]";
	}

	public String getSessionInfo() {
		return sessionInfo;
	}

	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	@Override
	public int compareTo(WorkSpaceLog otherLog) {
		// TODO Auto-generated method stub
		try {
            Date thisDate = dateFormat.parse(this.date);
            Date otherDate = dateFormat.parse(otherLog.date);
            // Compare the dates
            return thisDate.compareTo(otherDate);
        } catch (ParseException e) {
            // Handle the exception (e.g., log it or throw a RuntimeException)
            throw new RuntimeException("Error parsing dates during comparison", e);
        }
	}

}
