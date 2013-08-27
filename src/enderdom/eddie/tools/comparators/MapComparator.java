package enderdom.eddie.tools.comparators;

import java.util.Comparator;
import java.util.Map;

/**
 * 
 * @see http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
 *
 */
public class MapComparator implements Comparator<String> {

	Map<String, Integer> base;
	boolean lowtohigh;
	
	public MapComparator(Map<String, Integer> base, boolean lowtohigh){
		this.base = base;
	}
	
	public int compare(String a, String b) {
		if(lowtohigh){
			return base.get(a) >= base.get(b) ? -1 : 1;
		}
		else return base.get(a) >= base.get(b) ? 1 : -1;
	}

}
