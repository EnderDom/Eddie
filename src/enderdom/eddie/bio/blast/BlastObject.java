package enderdom.eddie.bio.blast;


public interface BlastObject {

	public void put(String key, String value);
	
	public String get(String key);
	
	public void putHitTag(int hitnumber, String tag, String value) throws BlastOneBaseException;
	
	public void putHspTag(int hspnumber, int hitnumber, String tag, String value) throws BlastOneBaseException, GeneralBlastException;
	
	public int getNoOfHits();
	
	public int getNoOfHsps(int hit_num);
	
	public String getBlastTagContents(String tag);
	
	public String getHitTagContents(String tag, int hit_num) throws Exception;
	
	public String getHspTagContents(String tag, int hit_num, int hsp_num) throws Exception;
	
	public void putIterationTag(String tag, String value);
	
	public double getLowestEValue();
	
	public int getQueryLength();
	
	public int[] getNumberofHitsBelow(double e);
	
	public int getLargestRange();
	
	public String[] getKeys();
}
