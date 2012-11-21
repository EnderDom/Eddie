package enderdom.eddie.bio.blast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.biojava3.core.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import enderdom.eddie.bio.objects.BlastObject;
import enderdom.eddie.tools.Tools_XML;

/**
 * Class parse Blastx output
 * in theory should work on  
 * blastn , but I haven't got round
 * to testing it yet 
 * 
 * 
 * Eddie3 method, but works
 */
public class BlastxDocumentParser{

	Document blastdoc;
	String filepath;
	BlastObject obj;
	//Hashtable<String, String> blastcache;
	int[] hits;
	Logger logger = Logger.getRootLogger();
	public static String[] recordtags = new String[]{"Iteration_iter-num", "Iteration_query-ID","Iteration_query-def","Iteration_query-len"};
	public static String[] hittags =new String[]{"Hit_num", "Hit_id", "Hit_def", "Hit_accession", "Hit_len"};
	public static String[] hsptags = new String[]{"Hsp_bit-score" , "Hsp_score" ,
		"Hsp_evalue" , "Hsp_query-from" , "Hsp_query-to" , "Hsp_hit-from" , "Hsp_hit-to" , "Hsp_query-frame" , 
		"Hsp_hit-frame" , "Hsp_identity" , "Hsp_positive" , "Hsp_gaps" , "Hsp_align-len" , "Hsp_qseq" , "Hsp_hseq" , "Hsp_midline"};
	private boolean dropDOM = true;
	
	public BlastxDocumentParser(String filepath) throws Exception{
		this(new File(filepath));
	}
	
	public BlastxDocumentParser(File file) throws Exception{
		this.filepath = file.getPath();
		blastdoc = XMLHelper.loadXML(filepath);
		obj = parse2Cache(blastdoc);
		if(dropDOM){
			blastdoc=null;
		}
	}
	
	
	public BlastObject parse2Cache(Document d){
		
		BlastObject blastcache = new BlastObject();
		
		ArrayList<Element> elementList = null;
		try {
			/*TODO probably do not need to use XMLHelper,
			 * could remove the dependency
			 */
			elementList = XMLHelper.selectElements(d.getDocumentElement(), "BlastOutput_iterations/Iteration[Iteration_hits]");
		} catch (Exception e) {
			logger.error("XML parse issue or something..?", e);
		}
		if(elementList != null){
			if(elementList.size() == 1){
				Element element = elementList.get(0);
				obj = parseLayerIterations(blastcache, element);
			}
			else{
				logger.warn("Not yet supporting multi Blast result files");
			}
		}
		return blastcache;
	}
	
	private BlastObject parseLayerIterations(BlastObject blastcache, Element element){
		String breakontag = "Iteration_hits";
		NodeList list = element.getChildNodes();
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				parseLayerTop(blastcache, list.item(i).getChildNodes());
			}
			else if(list.item(i).getNodeName().contains("Parameters_")){
				blastcache.put(list.item(i).getNodeName(), list.item(i).getTextContent());
			}
			else{
				for(int j =0; j < recordtags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(recordtags[j])){
						blastcache.put(recordtags[j], list.item(i).getTextContent());
					}
				}
			}
		}
		return blastcache;
	}
	
	private void parseLayerTop(BlastObject blastcache, NodeList list){
		String breakontag = "Hit";
		LinkedList<Integer> hsp_sizes = new LinkedList<Integer>();
		hsp_sizes.add(-1); //Added to workaround bioindex issue
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				hsp_sizes = parseLayerHit(blastcache, list.item(i).getChildNodes(), hsp_sizes);
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
	private LinkedList<Integer> parseLayerHit(BlastObject blastcache, NodeList list, LinkedList<Integer> hsp_sizes){
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
				hsp_count = parseLayerIntraHitHsp(blastcache, list.item(i).getChildNodes(), hit_num);
			}
			else{
				for(int j= 0; j < hittags.length; j++){
					if(list.item(i).getNodeName().equalsIgnoreCase(hittags[j])){
						blastcache.put(keys[j], list.item(i).getTextContent());
					}
				}
			}
		}
		hsp_sizes.add(hsp_count);
		return hsp_sizes;
	}

	private int parseLayerIntraHitHsp(BlastObject blastcache, NodeList list, String hit_num){
		String breakontag = "Hsp";
		int hsp_count = 0;
		for(int i=0; i < list.getLength(); i++){
			if(list.item(i).getNodeName().equalsIgnoreCase(breakontag)){
				parseLayerHsp(blastcache, list.item(i).getChildNodes(), hit_num);
				hsp_count++;
			}
		}
		return hsp_count;
	}
	
	private void parseLayerHsp(BlastObject blastcache, NodeList list, String hit_num) {
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
						blastcache.put(keys[j], list.item(i).getTextContent());
					}
				}
			}
		}
	}
	
	
	/**
	 * Saves XML document to filepath location
	 * @param filepath 
	 */
	public void save(String filepath){
		Tools_XML.Xml2File(this.blastdoc, new File(filepath));
	}
	/**
	 * Saves XML document to file
	 * @param filepath
	 */
	public void save(File filepath){
		Tools_XML.Xml2File(this.blastdoc, filepath);
	}
	
	public BlastObject getBlastObject(){
		return this.obj;
	}
	
}
