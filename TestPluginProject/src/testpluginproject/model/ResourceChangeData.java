package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

public class ResourceChangeData {
	
	//Type, Files affected, Code? (Save/Load only?)
	private String FileAffected;
	private String code;
	private String username;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public ResourceChangeData(String fileAffected, String code) {
		super();
		FileAffected = fileAffected;
		this.code = code;
		
		this.username = Utils.getUsernameFromPref();
	}

	public String getFileAffected() {
		return FileAffected;
	}
	public void setFileAffected(String fileAffected) {
		FileAffected = fileAffected;
	}
	
	

}
