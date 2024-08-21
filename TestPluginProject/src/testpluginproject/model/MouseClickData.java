package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import testpluginproject.utils.Utils;

public class MouseClickData {
	
	private int X;
	private int Y;
	private int Line;
	private int CharOffset;
	private String fileName;
	private String time;
	private String username;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public MouseClickData(int X,int Y,String fileName,int Line, int charOffset) {
		this.X = X;
		this.Y= Y;
		this.fileName = fileName;
		this.time = dateFormat.format(new Date());
		this.Line = Line;
		this.CharOffset = CharOffset;
		
		this.username = Utils.getUsernameFromPref();
	}
	



	@Override
	public String toString() {
		return "MouseClickData [X=" + X + ", Y=" + Y + ", Line=" + Line + ", CharOffset=" + CharOffset + ", fileName="
				+ fileName + ", time=" + time + ", username=" + username + "]";
	}


	public int getX() {
		return X;
	}
	public void setX(int x) {
		X = x;
	}
	public int getY() {
		return Y;
	}
	public void setY(int y) {
		Y = y;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	

}
