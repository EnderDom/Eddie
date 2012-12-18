package enderdom.eddie.tools.comparators;

import java.util.Comparator;

public class StringLength_Comparator implements Comparator<String>{
	/*
	 * Code lifted from
	 * http://stackoverflow.com/questions/3408976/sort-array-first-by-length-then-alphabetically-in-java
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
