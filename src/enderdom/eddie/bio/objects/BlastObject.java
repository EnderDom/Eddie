package enderdom.eddie.bio.objects;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;


public class BlastObject extends Hashtable<String, String>{

	private static final long serialVersionUID = -5499555843635862257L;
	int[] hits;
	Logger logger = Logger.getRootLogger();
	
	public BlastObject(){
		super();
	}

	
	/**
	 * The method actually returns the lenght of the hits array-1
	 * This is becuase the first integer is -1 as there is no
	 * hit0. This was done to preserve the 1-base and hopefully avoid
	 * confusion. (The latter being unsuccessful)
	 * 
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
			return this.hits.length-1;
		}
		else{
			return 0;
		}
	}
	
	/**
	 * 
	 * NOTE: blast values are all base-1
	 * as such loops should be started at 1 and
	 * should stop at this return value +1
	 * 
	 * @param hit_num
	 * @return the number of hsps attached to this hit
	 * Get Number of hsps at Hit Numb
	 * Hit_num = 0 will return -1 as 
	 * there is not hit 0
	 */
	public int getNoOfHsps(int hit_num){
		return this.hits[hit_num]; 
	}
	
	/**
	 * See XML_Blastx.recordtags static string array for the
	 * tags that have been cached
	 * @param Blast Record Tag
	 * @return Contents of Tag
	 */
	public String getBlastTagContents(String tag){
		return this.get(tag);
	}
	
	/**
	 * 
	 * @param tag the XML tag exactly as written in the  xml
	 * this is case sensitive
	 * @param hit_num The number as would be written in the blast
	 * xml, ie base-1
	 * @return The value of that tag for that hit number
	 * @throws Exception if hit hum is 0 or less , or larger than hits.length

	 * Note, because of 'bioindexing' aka funtimes,
	 * Start looping at 1 not zero for hit_num,
	 * but length remains the same 
	 */
	public String getHitTagContents(String tag, int hit_num) throws Exception{
		if(hit_num <=0 || hit_num > this.hits.length){
			throw new Exception("You cannot retrieve a hit which doesn't exist!");
		}
		return this.get("HIT"+hit_num+"_"+tag);
	}
	
	/**
	 * 
	 * @param tag XML tag (see XML_Blastx.hsptags)
	 * @param hit_num 
	 * @param hsp_num
	 * @return the contents of the tag
	 * @throws Exception
	 */
	public String getHspTagContents(String tag, int hit_num, int hsp_num) throws Exception{
		if(hsp_num <= 0){
			throw new Exception("You cannot retrieve a hsp which doesn't exist!");
		}
		return this.get("HSP"+hit_num+"_"+hsp_num+"_"+tag);
	}
		
	/**
	 * Prints all keys to terminal, for debugging purposes
	 */
	public void keyDump(){
		System.out.println("Key Dump...");
		for(String key : this.keySet()){
			System.out.println(key + " -> " + this.get(key));
		}
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
				} 
				catch (Exception ex) {
					logger.error("Format Error with blast data, could not convet hsp_evalue to double", ex);
				}
			}
		}
		if(e == 999)return -1;
		else return e;
	}
	
}
