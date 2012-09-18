package enderdom.eddie.tools;

import java.util.Comparator;

public class Tools_String_Comparator implements Comparator<String>{
	/*
	 * Code lifted from
	 * http://stackoverflow.com/questions/3408976/sort-array-first-by-length-then-alphabetically-in-java
	 * by user: Samuel_xL
	 * 
	 */
	
	public int compare(String o1, String o2) {
		if (o1.length() > o2.length()){
			return 1;
		}
		else if(o1.length() < o2.length()) {
			return -1;
		}
		else{ 
			return o1.compareTo(o2);
		}
	}
}
