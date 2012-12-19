package enderdom.eddie.bio.blast;

/**
 * 
 * @author dominic
 *
 * bp are 0-Based!
 * 
 * 0 <= x <querylen
 *
 */
public class UniVecRegion {

	private int start;
	private int stop;
	private int querylen;
	public static int ERROR = -1;
	public static int NONE = 0;
	public static int SUSPECT = 1;
	public static int WEAK = 2;
	public static int MODERATE = 3;
	public static int STRONG = 4;
	private int regionstrength;
	
	
	/**
	 * 
	 * @param start
	 * @param stop
	 * @param querylen
	 * @param bitscore set to -1 if it is a region between matches, this
	 * will then check if the region is suspect
	 */
	public UniVecRegion(int start, int stop, int querylen, double bitscore){
		setAll(start, stop, querylen, -1);
		if(bitscore != -1){
			this.regionstrength = calcStrength(start, stop, querylen, bitscore);
		}
		else if(calcSuspect(start, stop) == true) this.regionstrength = SUSPECT;
		else this.regionstrength = -1;
	}
	
	/**
	 * Assumes this is a region between match regions or between
	 * match and the start/end of sequence
	 * so checks if region if the region is 50 bp long
	 * ie it assumes you will break up the sequence based on matches
	 * and thus any region less that 50bp outside of matched regions are
	 * suspect
	 * 
	 * @param start
	 * @param stop
	 * @param querylen
	 * @return
	 */
	public static boolean calcSuspect(int start, int stop){
		return stop-start < 50; //IF it's length is less than 50 return true
	}
	
	/**
	 * Calculates the 'strength' of the region assumes to be a match
	 * 
	 * 0 <= start||stop < len(query)
	 * 
	 * @param start 0-based
	 * @param stop 0-based 
	 * @param querylen 0-based
	 * @param bitscore 
	 * @return
	 */
	public static int calcStrength(int start, int stop, int querylen, double bitscore){
		boolean terminal = isTerminal(start, stop, querylen);
		if(bitscore >= 30 || (bitscore >=24 && terminal) )return STRONG; 
		if(bitscore >= 25 || (bitscore >= 19 && terminal))return MODERATE;
		if(bitscore >= 23 || (bitscore >= 16 && terminal))return WEAK;
		else return NONE;
	}
	
	
	private void setAll(int start, int stop, int querylen, int regionstrength){
		this.start = start;
		this.stop = stop;
		this.querylen = querylen;
		this.regionstrength = regionstrength;
	}

	public boolean isRegionStrong(){
		return this.regionstrength == STRONG;
	}
	
	public boolean isRegionModerate(){
		return this.regionstrength == MODERATE;
	}
	
	public boolean isRegionWeak(){
		return this.regionstrength == WEAK;
	}
	
	public boolean isRegionSuspect(){
		return this.regionstrength == SUSPECT;
	}
	
	private static boolean isTerminal(int start, int stop, int querylen){
		return start < 25 ? true : stop+25 >= querylen;
	}
	
	public boolean isRightTerminal(){
		return stop+25>=querylen;
	}
	
	public boolean isLeftTerminal(){
		return start < 25;
	}

	public int getStart(int base) {
		return start+base;
	}

	public void setStart(int start, int base) {
		this.start = start-base;
	}

	public int getStop(int base) {
		return stop+base;
	}

	public void setStop(int stop, int base) {
		this.stop = stop-base;
	}

	public int getQuerylen() {
		return querylen;
	}

	public void setQuerylen(int querylen) {
		this.querylen = querylen;
	}

	public int getRegionstrength() {
		return regionstrength;
	}

	public void setRegionstrength(int regionstrength) {
		this.regionstrength = regionstrength;
	}

}
