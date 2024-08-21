package testpluginproject.model;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileStateModel {

	private String ProjectName;
	private String packageName;
	private String fileName;
	private String content;
	private String time;
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public FileStateModel(String ProjectName, String packageName, String fileName, String content) {
		this.ProjectName = ProjectName;
		this.packageName = packageName;
		this.fileName = fileName;
		this.content = content;
		this.time = dateFormat.format(new Date());
	}

	public String getProjectName() {
		return ProjectName;
	}

	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "FileStateModel [ProjectName=" + ProjectName + ", packageName=" + packageName + ", fileName=" + fileName
				+ ", content=" + content + ", time=" + time + "]";
	}
	
	

}
