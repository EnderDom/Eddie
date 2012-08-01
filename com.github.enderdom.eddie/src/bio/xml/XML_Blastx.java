package bio.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.biojava3.core.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tools.Tools_String;
import tools.Tools_XML;

/**
 * Class parse Blastx output
 * in theory should work on  
 * blastn , but I haven't got round
 * to testing it yet 
 * 
 * 
 * Eddie3 method, but works
 */

public class XML_Blastx{

	Document blastDoc;
	String filepath;
	Hashtable<String, String> blastcache;
	int[] hits;
	Logger logger = Logger.getRootLogger();
	public static String[] recordtags = new String[]{"Iteration_iter-num", "Iteration_query-ID","Iteration_query-def","Iteration_query-len"};
	public static String[] hittags =new String[]{"Hit_num", "Hit_id", "Hit_def", "Hit_accession", "Hit_len"};
	public static String[] hsptags = new String[]{"Hsp_bit-score" , "Hsp_score" ,
		"Hsp_evalue" , "Hsp_query-from" , "Hsp_query-to" , "Hsp_hit-from" , "Hsp_hit-to" , "Hsp_query-frame" , 
		"Hsp_hit-frame" , "Hsp_identity" , "Hsp_positive" , "Hsp_gaps" , "Hsp_align-len" , "Hsp_qseq" , "Hsp_hseq" , "Hsp_midline"};
	private boolean dropDOM = true;
	
	public  XML_Blastx(String filepath) throws Exception{
		this(new File(filepath));
	}
	
	public  XML_Blastx(File file) throws Exception{
		this.filepath = file.getPath();
		blastDoc = XMLHelper.loadXML(filepath);
		parse2Cache();
	}
	
	
	private void parse2Cache(){
		if(blastcache == null){
			blastcache = new Hashtable<String, String>();
		}
		ArrayList<Element> elementList = null;
		try {
			/*TODO probably do not need to use XMLHelper,
			 * could remove the dependency
			 */
			elementList = XMLHelper.selectElements(blastDoc.getDocumentElement(), "BlastOutput_iterations/Iteration[Iteration_hits]");
		} catch (Exception e) {
			logger.error("XML parse issue or something..?", e);
		}
		if(elementList != null){
			if(elementList.size() == 1){
				Element element = elementList.get(0);
				parseLayerIterations(element);
			}
			else{
				logger.warn("Not yet supporting multi Blast result files");
			}
		}
		if(dropDOM){
			this.blastDoc=null;
		}
	}
	
	private void parseLayerIterations(Element element){
		String breakontag = "Iteration_hits";
		NodeList list = element.getChildNodes();
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				parseLayerTop(list.item(i).getChildNodes());
			}
			else if(list.item(i).getNodeName().contains("Parameters_")){
				this.blastcache.put(list.item(i).getNodeName(), list.item(i).getTextContent());
			}
			else{
				for(int j =0; j < recordtags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(recordtags[j])){
						this.blastcache.put(recordtags[j], list.item(i).getTextContent());
					}
				}
			}
		}
	}
	
	private void parseLayerTop(NodeList list){
		String breakontag = "Hit";
		LinkedList<Integer> hsp_sizes = new LinkedList<Integer>();
		hsp_sizes.add(-1); //Added to workaround bioindex issue
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				hsp_sizes = parseLayerHit(list.item(i).getChildNodes(), hsp_sizes);
			}
		}
		this.hits = new int[hsp_sizes.size()];
		for(int i =0; i < this.hits.length;i++) this.hits[i] = hsp_sizes.get(i);
	}

	
	/**
	 * Parse Layer Hit and Hsp both assume a structure where Hit/Hsp_num
	 * tag is the first one in the xml section. If this is not the case, the
	 * result is keys will be generic and overwritten. 
	 */
	private LinkedList<Integer> parseLayerHit(NodeList list, LinkedList<Integer> hsp_sizes){
		String[] keys = hittags.clone();
		String breakontag = "Hit_hsps";
		String idtag = "Hit_num";
		String id = "HIT";
		String hit_num = "";
		int hsp_count = 0;
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(idtag)){
				hit_num = list.item(i).getTextContent();
				for(int j =0 ; j < keys.length; j++){
					keys[j] = id + hit_num + "_"+keys[j];
				}
			}
			else if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				hsp_count = parseLayerIntraHitHsp(list.item(i).getChildNodes(), hit_num);
			}
			else{
				for(int j= 0; j < hittags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(hittags[j])){
						this.blastcache.put(keys[j], list.item(i).getTextContent());
					}
				}
			}
		}
		hsp_sizes.add(hsp_count);
		return hsp_sizes;
	}

	private int parseLayerIntraHitHsp(NodeList list, String hit_num){
		String breakontag = "Hsp";
		int hsp_count = 0;
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				parseLayerHsp(list.item(i).getChildNodes(), hit_num);
				hsp_count++;
			}
		}
		return hsp_count;
	}
	
	private void parseLayerHsp(NodeList list, String hit_num) {
		String[] keys = hsptags.clone();
		String idtag = "Hsp_num";
		String id = "HSP"+hit_num+"_";
		String hsp_num = "";
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(idtag)){
				hsp_num = list.item(i).getTextContent();
				for(int j =0 ; j < keys.length; j++){
					keys[j] = id+ hsp_num + "_"+keys[j];
				}
			}
			else{
				for(int j= 0; j < hsptags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(hsptags[j])){
						this.blastcache.put(keys[j], list.item(i).getTextContent());
					}
				}
			}
		}
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
		return this.blastcache.get(tag);
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
		return this.blastcache.get("HIT"+hit_num+"_"+tag);
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
		return this.blastcache.get("HSP"+hit_num+"_"+hsp_num+"_"+tag);
	}
		
	/**
	 * Prints all keys to terminal, for debugging purposes
	 */
	public void keyDump(){
		System.out.println("Key Dump...");
		for(String key : this.blastcache.keySet()){
			System.out.println(key + " -> " + this.blastcache.get(key));
		}
	}
	
	/**
	 * Change a tag, set boolean to true to change all by that tag,
	 * else only the first will be changed 
	 * @param tag
	 * @param value
	 * @param all
	 * 
	 * Never tested or used, may melt computer
	 */
	public void changeValue(String tag, String value, boolean all){
		if(!dropDOM){
			NodeList list = this.blastDoc.getElementsByTagName(tag);
			for(int i =0; i < list.getLength(); i++){
				logger.debug("Changing " + list.item(i).getTextContent() + " to " + value);
				list.item(i).setTextContent(value);
				logger.debug("Value now: " + list.item(i).getTextContent());
				if(!all) break;
			}
		}
		else{
			logger.error("Object set to strip specific values and drop the DOM element, cannot change any values once parsed");
		}
	}
	
	/**
	 * Saves XML document to filepath location
	 * @param filepath 
	 */
	public void save(String filepath){
		Tools_XML.Xml2File(this.blastDoc, new File(filepath));
	}
	/**
	 * Saves XML document to file
	 * @param filepath
	 */
	public void save(File filepath){
		Tools_XML.Xml2File(this.blastDoc, filepath);
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
					logger.error("Format Error with blast file " + this.filepath, ex);
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
					logger.error("Format Error with blast file " + this.filepath, ex);
				}
			}
		}
		if(e == 999)return -1;
		else return e;
	}
	
}
