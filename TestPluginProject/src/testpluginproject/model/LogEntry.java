package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.util.ArrayList;
import java.util.Date;

import testpluginproject.utils.Utils;

//import com.fasterxml.jackson.annotation.JsonFormat;



public class LogEntry {
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date date;
    private String severity;
    private String plugin;
    private String message;
    private String stackTrace;
    private String sessionId;
    private String username;
    private ArrayList<SubLogEntry> sublogEntry = new ArrayList<SubLogEntry>();

    public LogEntry(Date date, String severity, String plugin, String message, String stackTrace, String sessionId) {
        this.date = date;
        this.severity = severity;
        this.plugin = plugin;
        this.message = message;
        this.stackTrace = stackTrace;
        this.sessionId = sessionId;
        
        this.username = Utils.getUsernameFromPref();
    }
    public ArrayList<SubLogEntry> getSublogEntry() {
		return sublogEntry;
	}
	public void setSublogEntry(ArrayList<SubLogEntry> sublogEntry) {
		this.sublogEntry = sublogEntry;
	}
	public LogEntry() {
    	
    }

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
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

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	@Override
	public String toString() {
		return "LogEntry [date=" + date + ", severity=" + severity + ", plugin=" + plugin + ", message=" + message
				+ ", stackTrace=" + stackTrace + ", sessionId=" + sessionId + ", sublogEntry=" + sublogEntry + "]";
	}
	
	
}
