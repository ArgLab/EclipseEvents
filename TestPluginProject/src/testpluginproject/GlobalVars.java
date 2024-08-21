package testpluginproject;

import java.util.ArrayList;
import java.util.List;

import testpluginproject.model.jsonModel.SequentialEventData;

public class GlobalVars {
	public static String activeProject = "unknown";
	public static String lastOpenFile = "undefined";
	public static List<SequentialEventData> listSequentialEvents = new ArrayList<>();
}
