package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import testpluginproject.utils.Utils;

public class KeyBoardClickData {
	
	private String contents;
	private String username;
	
	public KeyBoardClickData(String contents) {
		this.contents = contents;
		this.username = Utils.getUsernameFromPref();
	}

	public String getContents() {
		return contents;
	}

	public void setContents(char contents) {
		this.contents += contents;
	}

	@Override
	public String toString() {
		return "KeyBoardClickData [contents=" + contents + "]";
	}
	

}
