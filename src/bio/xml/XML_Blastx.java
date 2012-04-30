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
	
	public  XML_Blastx(String filepath) throws Exception{
		this(new File(filepath));
	}
	
	public  XML_Blastx(File file) throws Exception{
		this.filepath = file.getPath();
		blastDoc = XMLHelper.loadXML(filepath);
		parse2Cache(true);
	}
	
	private void parse2Cache(boolean dropDOM){
		if(blastcache == null){
			blastcache = new Hashtable<String, String>();
		}
		ArrayList<Element> elementList = null;
		try {
			elementList = XMLHelper.selectElements(blastDoc.getDocumentElement(), "BlastOutput_iterations/Iteration[Iteration_hits]");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(elementList != null){
			if(elementList.size() == 1){
				Element element = elementList.get(0);
				parseLayerIterations(element);
			}
			else{
				Logger.getRootLogger().warn("Not yet supporting multi Blast result files");
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
			else{
				if(list.item(i).getNodeName().equalsIgnoreCase("Iteration_iter-num")){
					this.blastcache.put("Iteration_iter-num", list.item(i).getTextContent());
				}
				else if(list.item(i).getNodeName().equalsIgnoreCase("Iteration_query-ID")){
					this.blastcache.put("Iteration_query-ID", list.item(i).getTextContent());
				}
				else if(list.item(i).getNodeName().equalsIgnoreCase("Iteration_query-def")){
					this.blastcache.put("Iteration_query-def", list.item(i).getTextContent());
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

	
	/*
	 * Parse Layer Hit and Hsp both assume a structure where Hit/Hsp_num
	 * tag is the first one in the xml section. If this is not the case, the
	 * result is keys will be generic and overwritten. 
	 */
	
	private LinkedList<Integer> parseLayerHit(NodeList list, LinkedList<Integer> hsp_sizes){
		String[] tags = new String[]{"Hit_num", "Hit_id", "Hit_def", "Hit_accession", "Hit_len"};
		String[] keys = tags.clone();
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
				for(int j= 0; j < tags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(tags[j])){
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
		String[] tags = new String[]{"Hsp_bit-score" , "Hsp_score" ,
        		"Hsp_evalue" , "Hsp_query-from" , "Hsp_query-to" , "Hsp_hit-from" , "Hsp_hit-to" , "Hsp_query-frame" , 
        		"Hsp_hit-frame" , "Hsp_identity" , "Hsp_positive" , "Hsp_gaps" , "Hsp_align-len" , "Hsp_qseq" , "Hsp_hseq" , "Hsp_midline"};
		String[] keys = tags.clone();
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
				for(int j= 0; j < tags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(tags[j])){
						this.blastcache.put(keys[j], list.item(i).getTextContent());
					}
				}
			}
		}
	}
	
	public int getNoOfHits(){
		if(this.hits != null){
			return this.hits.length-1;
		}
		else{
			return 0;
		}
	}
	
	/*
	 * Get Number of hsps at Hit Numb
	 * Hit_num = 0 will return -1 as 
	 * there is not hit 0
	 */
	public int getNoOfHsps(int hit_num){
		return this.hits[hit_num]; 
	}
	
	public String getBlastTagContents(String tag){
		return this.blastcache.get(tag);
	}
	
	/*
	 * Note, because of 'bioindexing' aka funtimes,
	 * Start looping at 1 not zero for hit_num,
	 * but length remains the same 
	 * 
	 * 
	 */
	
	public String getHitTagContents(String tag, int hit_num) throws Exception{
		if(hit_num <=0 || hit_num > this.hits.length){
			throw new Exception("You cannot retrieve a hit which doesn't exist!");
		}
		return this.blastcache.get("HIT"+hit_num+"_"+tag);
	}
	
	public String getHspTagContents(String tag, int hit_num, int hsp_num) throws Exception{
		if(hsp_num <= 0){
			throw new Exception("You cannot retrieve a hsp which doesn't exist!");
		}
		return this.blastcache.get("HSP"+hit_num+"_"+hsp_num+"_"+tag);
	}
		
	//Method mainly For Debugging
	public void keyDump(){
		System.out.println("Key Dump...");
		for(String key : this.blastcache.keySet()){
			System.out.println(key + " -> " + this.blastcache.get(key));
		}
	}
	
	//Change a tag, set boolean to true to change all by that tag, 
	//else only the first will be changed
	public void changeValue(String tag, String value, boolean all){
		NodeList list = this.blastDoc.getElementsByTagName(tag);
		for(int i =0; i < list.getLength(); i++){
			System.out.println("Changing " + list.item(i).getTextContent() + " to " + value);
			list.item(i).setTextContent(value);
			System.out.println("Value now: " + list.item(i).getTextContent());
			if(!all) break;
		}
	}
	
	public void save(String filepath){
		Tools_XML.Xml2File(this.blastDoc, new File(filepath));
	}
	public void save(File filepath){
		Tools_XML.Xml2File(this.blastDoc, filepath);
	}
	
	public int getLargestRange(){
		int r = 0;
		for(int i =1; i < this.getNoOfHits();i++){
			for(int j =1; j < this.getNoOfHsps(i);j++){
				try {
					Integer to = Tools_String.parseString2Int(this.getHspTagContents("Hsp_query-to", i, j));
					Integer from = Tools_String.parseString2Int(this.getHspTagContents("Hsp_query-from", i, j));
					if(to != null && from != null){
						int delta =Math.abs(to-from);
						if(delta > r){
							r = delta;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		return r;
	}
	
	public double getLowestEValue(){
		double e =99;
		for(int i =1; i < this.getNoOfHits();i++){
			for(int j =1; j < this.getNoOfHsps(i);j++){
				try {
					Double to = Tools_String.parseString2Double(this.getHspTagContents("Hsp_evalue", i, j));
					if(to != null){
						if(to < e){
							e=to;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}				
			}
		}
		return e;
	}
	
}
