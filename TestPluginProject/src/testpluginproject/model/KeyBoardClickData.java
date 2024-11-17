package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import testpluginproject.utils.Utils;

public class KeyBoardClickData {
	
	private String contents;
	private String username;
	private String currentWindow;
	
	public KeyBoardClickData(String windowName,String contents) {
		this.contents = contents;
		this.username = Utils.getUsernameFromPref();
		this.currentWindow = windowName;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(char contents) {
		this.contents += contents;
	}
	
	

	public String getCurrentWindow() {
		return currentWindow;
	}

	public void setCurrentWindow(String currentWindow) {
		this.currentWindow = currentWindow;
	}

	@Override
	public String toString() {
		return "KeyBoardClickData [contents=" + contents + ", username=" + username + ", currentWindow=" + currentWindow
				+ "]";
	}

	

	

}
