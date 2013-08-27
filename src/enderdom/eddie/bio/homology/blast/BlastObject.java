package enderdom.eddie.bio.homology.blast;

/**
 * 
 * @author Dominic Matthew Wood
 *
 * Everything is 1-based as blast is 1-based
 * similar to SQL. Bit of a mindfuck i know.
 *
 */
public interface BlastObject {

	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, String value);
	
	/**
	 * 
	 * @param key
	 * @return value for key
	 */
	public String get(String key);
	
	/**
	 * 
	 * @param hitnumber 1-based so 1 is the initial index and number of hits the last
	 * @param tag hit specific tag such as definition
	 * @param value set value
	 * @throws BlastOneBaseException
	 */
	public void putHitTag(int hitnumber, String tag, String value) throws BlastOneBaseException;
	
	/**
	 * 
	 * @param hspnumber 1-based 
	 * @param hitnumber 1-based so 1 is the initial index and number of hits the last
	 * @param tag hsp specific blast
	 * @param value set value
	 * @throws BlastOneBaseException
	 * @throws GeneralBlastException
	 */
	public void putHspTag(int hspnumber, int hitnumber, String tag, String value) throws BlastOneBaseException, GeneralBlastException;
	
	/**
	 * 1 Based!
	 * @return the number of hits (1-based), so 50 hits returns 50.
	 * As such use <=getNoOfHits() for loops and start at i=1
	 * 
	 */
	public int getNoOfHits();
	
	/**
	 * 1 Based!
	 * @return the number of hsps (1-based), so 3 hsps returns 3.
	 * As such use <=getNoOfHsps() for loops and start at i=1
	 * 
	 */
	public int getNoOfHsps(int hit_num);
	
	/**
	 * Tags which have blast in them
	 * @param tag
	 * @return tag contents
	 */
	public String getBlastTagContents(String tag);
	
	/**
	 * 
	 * @param tag
	 * @param hit_num 1-based
	 * @return contents of tag
	 * @throws Exception
	 */
	public String getHitTagContents(String tag, int hit_num) throws Exception;
	
	/**
	 * 
	 * @param tag
	 * @param hit_num 1-based
	 * @param hsp_num 1-based
	 * @return
	 * @throws Exception
	 */
	public String getHspTagContents(String tag, int hit_num, int hsp_num) throws Exception;
	
	/**
	 * 
	 * @return the lowest e-value found in the blast object
	 */
	public double getLowestEValue();
	
	/**
	 * 
	 * @return length of sequence used as query (Your sequence)
	 */
	public int getQueryLength();
	
	/**
	 * 
	 * @param e
	 * @return number of hits (1-based) below the given e-value
	 */
	public int[] getNumberofHitsBelow(double e);
	
	/**
	 * 
	 * @return  the largest range for the query sequence
	 *  the range being the difference between the hsp_query-to and hsp_query-from
	 */
	public int getLargestRange();
	
	/**
	 * 
	 * @return list of keys used (mainly for debugging)
	 */
	public String[] getKeys();
}
