package com.arglab.eclipsedatacollector.core.eclipsemonitor;

import java.util.ArrayList;
import java.util.List;

import com.arglab.eclipsedatacollector.core.eclipsemonitor.model.jsonmodel.SequentialEventData;



public class GlobalVars {

	public static String activeProject = "unknown";
	public static String lastOpenFile = "undefined";
	public static List<SequentialEventData> listSequentialEvents = new ArrayList<>();
}
