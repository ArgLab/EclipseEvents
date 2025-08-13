package com.arglab.eclipsedatacollector.core.eclipsemonitor.model;

import java.util.Date;

public class SubLogEntry {
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date date;
	private String plugin;
	private String message;
	private String stackTrace;
	private String severity;
	
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public SubLogEntry() {
		
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
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
	@Override
	public String toString() {
		return "SubLogEntry [date=" + date + ", plugin=" + plugin + ", message=" + message + ", stackTrace="
				+ stackTrace + ", severity=" + severity + "]";
	}
	
}
