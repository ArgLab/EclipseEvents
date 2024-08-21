package testpluginproject.utils;
/**
 * This class is authored by Saminur Islam and is owned by Dr. Collin Lynch.
 * It is licensed under the terms of the GNU Affero General Public License (AGPL) version 3.0 or later.
 */
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import testpluginproject.model.jsonModel.SequentialEventData;

public class EventTimeComparator implements Comparator<SequentialEventData>{
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	@Override
	public int compare(SequentialEventData o1, SequentialEventData o2) {
		// TODO Auto-generated method stub
		try {
            Date date1 = dateFormat.parse(o1.getEventTime());
            Date date2 = dateFormat.parse(o2.getEventTime());
            
            // Compare the parsed dates to determine the order
            return date1.compareTo(date2);
        } catch (Exception e) {
            // Handle the ParseException (e.g., by throwing an exception or logging an error)
            System.out.println("exception happened "+e.getMessage());
            return 0; // Return 0 if there is an error, indicating equal ordering
        }
	}

}
