package testpluginproject.model;

import testpluginproject.utils.Utils;

public class PopupWindowModel {
	private String Popupwindow;
	private String CurrentTab;
	private String username;
	
	public PopupWindowModel()
	{
		username = Utils.getUsernameFromPref();
	}
	
	public String getPopupwindow() {
		return Popupwindow;
	}


	public void setPopupwindow(String popupwindow) {
		Popupwindow = popupwindow;
	}


	public String getCurrentTab() {
		return CurrentTab;
	}


	public void setCurrentTab(String currentTab) {
		CurrentTab = currentTab;
	}


	@Override
	public String toString() {
		return "PopupWIndowModel [Popupwindow=" + Popupwindow + ", CurrentTab=" + CurrentTab + ", username=" + username
				+ "]";
	}
	
	

}
