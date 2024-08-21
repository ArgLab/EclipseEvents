package testpluginproject.utils;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class Utils {
	
	public static String getUsernameFromPref() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("csc216.plugin.prefs.page");
		//deal with ill formatted usernames
		String username = preferences.get("USERNAME", "default").toLowerCase();
		String email = preferences.get("EMAIL", "default").toLowerCase();
		if(email.contains("@ncsu.edu")) {
			if(email.replace("@ncsu.edu", "").equals(username)) {
				// They match, we can just move on
				return username;
			}
			else {
				// One of them doesn't match, it's impossible to know which is the problem 
				// For now, we default to the email
				return email.replace("@ncsu.edu", "");
			}
		}
		else {
			// We know the email is illformated, so rely on the user name (if it is there)
			if(username.equals("")) {
				return "not entered";
			}
			else {
				return username;
			}
		}
	}
	
	public static String getIpAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
   }
	
	 public static String getMacAddress() {
	        // TODO Auto-generated method stub
	        try {
	            Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
	            while(networkInterface.hasMoreElements()) {
	                NetworkInterface ni = networkInterface.nextElement();
	                byte[] hardwareAddress = ni.getHardwareAddress();
	                if(hardwareAddress!=null) {
	                    StringBuilder macAddresBuilder = new StringBuilder();
	                    for(int i = 0; i< hardwareAddress.length;i++) {
	                        macAddresBuilder.append(String.format("%02X", hardwareAddress[i]));
	                        if(i!=hardwareAddress.length-1) {
	                            macAddresBuilder.append("-");
	                        }
	                    }
	                    return macAddresBuilder.toString();
	                }
	                
	            }
	        }catch (SocketException e) {
	            // TODO: handle exception
	            System.out.println("Exception happened due to. "+e.getMessage());
	        }
	        return null;
	    }

}
