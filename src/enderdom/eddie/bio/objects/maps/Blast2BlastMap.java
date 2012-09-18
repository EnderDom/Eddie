package enderdom.eddie.bio.objects.maps;

import java.io.File;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.bio.xml.XML_Blastx;

public class Blast2BlastMap {

	
	private XML_Blastx xml1;
	private XML_Blastx xml2;
	private int hits = -1;
	private double evalue= -1;
	private int hits1;
	private int hits2;
	
	private Logger logger = Logger.getRootLogger();
	LinkedHashMap<String, Integer> map;
	
	public Blast2BlastMap(XML_Blastx xml1, XML_Blastx xml2){
		this.xml1 = xml1;
		this.xml2 = xml2;
	}
	
	public Blast2BlastMap(File xml1, File xml2) throws Exception{
		this.xml1 = new XML_Blastx(xml1);
		this.xml2 = new XML_Blastx(xml2);
	}
	
	/**
	 * 
	 * @return true if built without error
	 * returns false if built with error, or one
	 * of the xmls contains no hits and thus
	 * a map cannot be built
	 */
	public boolean build(){
		boolean errorless = true;
		if(xml1.getNoOfHits() != 0 && xml2.getNoOfHits() != 0){
			map = new LinkedHashMap<String, Integer>();
			for(int k =1; k < xml1.getNoOfHits();k ++){
				try{
					Double ev=-1.0;
					ev = Tools_String.parseString2Double(xml1.getHspTagContents("Hsp_evalue", k, 1));
					if(ev == null)ev=-1.0;
					if((hits == -1 || k < hits) && (evalue == -1 || ev < evalue)){
						map.put(xml1.getHitTagContents("Hit_accession", k), 1);
						hits1++;
					}
				}
				catch(Exception e){
					logger.error("Failed to get blast tag");
					errorless = false;
				}
			}
			for(int k =1; k < xml2.getNoOfHits();k ++){
				try{
					Double ev=-1.0;
					ev = Tools_String.parseString2Double(xml1.getHspTagContents("Hsp_evalue", k, 1));
					if(ev == null)ev=-1.0;
					if((hits == -1 || k < hits) && (evalue == -1 || ev < evalue)){
						String accession =xml2.getHitTagContents("Hit_accession", k);
						if(map.containsKey(accession)){
							map.put(accession, 2);
						}
						hits2++;
					}
//					else{ //DOn't actually use this information as yet
//						map.put(accession, 1);
//					}
				}
				catch(Exception e){
					logger.error("Failed to get blast tag");
					errorless = false;
				}
			}
			return errorless;
		}
		else{
			logger.error("Warn or more of the Blast XMLs does not contain any hits");
			return false;
		}
	}
	
	/**
	 * 
	 * @returns the number of accessions shared between the
	 * two blast files. array is length 3, index
	 *  0 are the number of shared blasts in xml1 & xml2
	 */
	
	public int getAccessionOverlap(){
		int t =0;
		if(map != null){
			for(String acc : map.keySet()){
				if(map.get(acc) == 2){
					t++;
				}
			}
		}
		return t;
	}
	
	/**
	 * 
	 * @return an array of l=2 
	 * with 0 the no of hits in the first
	 * blast xml and 1 the second
	 */
	public int[] getNoOfHits(){
		return new int[]{hits1, hits2};
	}
	
	/**
	 * 
	 * @param number of hits to return 
	 * @return returns a string array containing the hit_def for the top
	 * x for XML1
	 * @throws Exception when hit tag doesn't exist
	 */
	public String[] getTopXHitDefs4one(int i) throws Exception{
		if(hits1 < i){
			i = hits1;
		}
		return getTopXHitDefs(i, xml1);
	}
	
	/**
	 * 
	 * @param number of hits to return 
	 * @return returns a string array containing the hit_def for the top
	 * x for XML2
	 * @throws Exception when hit tag doesn't exist
	 */
	public String[] getTopXHitDefs4two(int i) throws Exception{
		if(hits2 < i){
			i = hits2;
		}
		return getTopXHitDefs(i, xml2);
	}
	
	/**
	 * At the moment this is kind of lazy
	 * assumes the top most hits will have the 
	 * highest evalue and thus not evalue filter
	 * is needed if evalue != -1
	 * 
	 * 
	 * @param number of hits to return 
	 * @return returns a string array containing the hit_def for the top
	 * x for any XML_Blastx object
	 * @throws Exception when hit tag doesn't exist
	 */
	public static String[] getTopXHitDefs(int i, XML_Blastx xml) throws Exception{
		String[] srs = new String[i];
		int c=1;
		while(c <= i){
			srs[c-1] = xml.getHitTagContents("Hit_def", c);
			c++;
		}
		return srs;
	}

	public void limit(int i, double d) {
		this.hits=i;
		this.evalue=d;
	}
	
	
}
