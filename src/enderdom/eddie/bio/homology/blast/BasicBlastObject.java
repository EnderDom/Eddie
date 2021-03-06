package enderdom.eddie.bio.homology.blast;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import enderdom.eddie.exceptions.BlastOneBaseException;
import enderdom.eddie.exceptions.EddieGenericException;
import enderdom.eddie.exceptions.GeneralBlastException;
import enderdom.eddie.tools.Tools_String;


public class BasicBlastObject implements BlastObject{

	//All data is 1-based
	private int[] hits;
	protected Logger logger = Logger.getRootLogger();
	protected static String hit_id = "HIT";
	protected static String hsp_id = "HSP";
	private BlastParser parent;
	private Hashtable<String, String> cache;
	
	public BasicBlastObject(BlastParser parser){
		parent = parser;
		cache = new Hashtable<String, String>();
	}
	
	public String get(String key){
		return this.cache.get(key);
	}
	
	public void put(String key, String value){
		this.cache.put(key, value);
	}
	
	public int getIterationNumber(){
		Integer a = Tools_String.parseString2Int(this.get("Iteration_iter-num"));
		if(a == null){
			logger.error("Does not contain iteration number");
			return -1;
		}
		else return a.intValue();
	}

	
	public void initHits(int size){
		if(hits !=null){
			logger.warn("Overwriting hit array");
		}
		hits = new int[size];
	}
	
	/**
	 * 
	 * @param number one based number
	 * @param tag Tag name
	 * @param value Value at tag
	 * @throws BlastOneBaseException 
	 */
	public void putHitTag(int hitnumber, String tag, String value) throws BlastOneBaseException{
		if(hitnumber == 0){
			throw new BlastOneBaseException("You can't add hit zero, no hit zero for you");
		}
		if(hits==null){
			hits = new int[1];
		}
		if(hits.length < hitnumber){
			int[] temp = new int[hitnumber];
			for(int i=0; i < hits.length;i++)temp[i]=hits[i];			
			temp[hitnumber-1]=0;
			hits=temp;
		}
		this.put(this.generateHitTag(hitnumber, tag), value);
	}
	
	public void putHspTag(int hspnumber, int hitnumber, String tag, String value) throws BlastOneBaseException, GeneralBlastException{
		if(hspnumber == 0){
			throw new BlastOneBaseException("You can't add hsp zero, no hsp zero for you");
		}
		if(hits == null){
			throw new GeneralBlastException("Either there is a bug in parser, or the blast file is dodgy");
		}
		this.put(this.generateHspTag(hspnumber, hitnumber, tag), value);
		if(hits[hitnumber-1] < hspnumber)hits[hitnumber-1] = hspnumber;
	}
	
	/**
	 * @return the number of hits, this will be 50 for 50 hits and
	 * 0 for 0 hits. Remember to +1 to this for looping through the hits
	 * when loop parameter is < length 
	 * eg
	 * for (int i =1 ; i < getNoOfHits()+1 ; i++)
	 * 
	 * 
	 */
	public int getNoOfHits(){
		if(this.hits != null){
			return this.hits.length;
		}
		else{
			return 0;
		}
	}
	
	/**
	 * 
	 * @param hit_num
	 * @return the number of hsps attached to this hit
	 * 
	 * for getNoOfHsps(3) this will return the number
	 * of hsps for the Hit which has the <Hit_num>3</Hit_num>
	 * and will return the size ie 2
	 * Which corresponds to <Hsp_num>1</Hsp_num> & <Hsp_num>2</Hsp_num> 
	 * 
	 * So loops should be 
	 * for(int i=1 ; i <= getNoHits();i++){
	 * 		for(int j =1 ; j <=getNoHsps(i);j++){
	 * 			//Do something
	 * 		}	
	 * }
	 * 
	 */
	public int getNoOfHsps(int hit_num){
		return this.hits[hit_num-1]; 
	}
	
	/**
	 * @param Blast Record Tag
	 * @return Contents of Tag
	 */
	public String getBlastTagContents(String tag){
		return this.parent.get(tag);
	}

	/**
	 * 
	 * @param tag the XML tag exactly as written in the  xml
	 * this is case sensitive
	 * @param hit_num The number as would be written in the blast
	 * xml, ie base 1
	 * @return The value of that tag for that hit number
	 * @throws Exception if hit hum is 0 or less , or larger than hits.length

	 * Note, because of 'bioindexing' aka funtimes,
	 * Start looping at 1 not zero for hit_num,
	 * but length remains the same 
	 */
	public String getHitTagContents(String tag, int hit_num) throws Exception{
		if(hit_num ==0 || hit_num > this.getNoOfHits()){
			throw new Exception("You cannot retrieve a hit which doesn't exist!");
		}
		return this.get(this.generateHitTag(hit_num, tag));
	}
	
	/**
	 * 
	 * @param tag XML tag (see XML_Blastx.hsptags)
	 * @param hit_num 
	 * @param hsp_num
	 * @return the contents of the tag
	 * @throws Exception
	 */
	public String getHspTagContents(String tag, int hit_num, int hsp_num) throws EddieGenericException{
		if(hsp_num <= 0){
			throw new EddieGenericException("You cannot retrieve a hsp which doesn't exist!");
		}
		return this.get(this.generateHspTag(hsp_num, hit_num, tag));
	}
		
	protected String generateHitTag(int hitnumber, String tag){
		return new StringBuilder(hit_id +"_"+ hitnumber +"_" + tag).toString();
	}
	
	protected String generateHspTag(int hspnumber, int hitnumber, String tag){
		return new StringBuilder(hsp_id + hitnumber + "_" +hspnumber +"_"+ tag).toString();
	}
	
	/**
	 * Prints all keys to terminal, for debugging purposes
	 */
	public void keyDump(){
		System.out.println("Key Dump...");
		for(String key : this.cache.keySet()){
			System.out.println(key + " -> " + this.get(key));
		}
	}
	
	public String[] getKeys(){
		return this.cache.keySet().toArray(new String[0]);
	}
	
	/**
	 * 
	 * @return the largest range for the query sequence
	 * is the hsp_query-to minus from
	 */
	public int getLargestRange(){
		int r = 0;
		for(int i =1; i < this.getNoOfHits()+1;i++){
			for(int j =1; j < this.getNoOfHsps(i)+1;j++){
				try {
					Integer to = Tools_String.parseString2Int(this.getHspTagContents("Hsp_query-to", i, j));
					Integer from = Tools_String.parseString2Int(this.getHspTagContents("Hsp_query-from", i, j));
					if(to != null && from != null){
						int delta =Math.abs(to-from);
						if(delta > r){
							r = delta;
						}
					}
				}  catch (Exception e) {
					logger.error("Failed to do the simplest of tasks :(", e);
				}				
			}
		}
		return r;
	}
	
	/**
	 * 
	 * @param e upper limit of filter
	 * @return number of hits which have a e value lower than param e
	 */
	public int[] getNumberofHitsBelow(double e){
		int[] count = new int[]{0,0};
		for(int i =1; i < this.getNoOfHits()+1;i++){
			boolean hitcounted = false;
			for(int j =1; j < this.getNoOfHsps(i)+1;j++){
				try {
					Double to = Tools_String.parseString2Double(this.getHspTagContents("Hsp_evalue", i, j));
					if(to != null){
						if(to < e){
							if(!hitcounted)count[0]++;
							count[1]++;
							hitcounted=true;
						}
					}
				} 
				catch (Exception ex) {
					logger.error("Format Error with blast data, could not convet hsp_evalue to double", ex);
				}		
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @return returns the lowest e value found 
	 * in this blast record. Returns -1 if no evalue found,
	 * which shouldn't happen unless there are no hits
	 */
	public double getLowestEValue(){
		double e =999;
		for(int i =1; i < this.getNoOfHits()+1;i++){
			for(int j =1; j < this.getNoOfHsps(i)+1;j++){
				try {
					Double to = Tools_String.parseString2Double(this.getHspTagContents("Hsp_evalue", i, j));
					if(to != null){
						if(to < e){
							e=to;
						}
					}
					else{
						logger.error("Fail");
						System.exit(0);
					}
				} 
				catch (Exception ex) {
					logger.error("Format Error with blast data, could not convet hsp_evalue to double", ex);
				}
			}
		}
		if(e == 999)return -1;
		else return e;
	}
	
	public int getQueryLength(){
		Integer i = Tools_String.parseString2Int(get("Iteration_query-len"));
		if(i == null) return -1;
		else return i.intValue();
	}
	
}
