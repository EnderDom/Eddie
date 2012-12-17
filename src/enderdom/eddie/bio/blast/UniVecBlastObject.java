package enderdom.eddie.bio.blast;

import java.util.LinkedList;

import enderdom.eddie.bio.objects.BlastObject;

public class UniVecBlastObject extends BlastObject {

	/**
	 * Interpretations are based on univec documentation 
	 * {@link http://www.ncbi.nlm.nih.gov/VecScreen/VecScreen_docs.html}
	 * 
	 * All in values are converted to 0-based
	 */
	
	private static final long serialVersionUID = 6544711804692232673L;

	public static String hit_start = "Hsp_query-from";
	public static String hit_end = "Hsp_query-to";
	int c =-1;
	LinkedList<UniVecRegion> regions;
	
	public boolean requiresTrim(){
		return this.getNoOfHits() != 0;
	}
	
	public UniVecRegion getRegion(int i){
		if(regions == null)generateRegions();
		return null;
	}
	
	public int regionCount(){
		if(regions == null)generateRegions();
		return regions.size();
	}

	public void generateRegions(){
		 regions = new LinkedList<UniVecRegion>();
		//Integer start = (int) Tools_String.parseString2Int(this.getHspTagContents(hit_start, s[0], s[1]));
		//Integer end = (int) Tools_String.parseString2Int(this.getHspTagContents(hit_end, s[0], s[1]));
	}
	
}
