package enderdom.eddie.bio.blast;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import enderdom.eddie.tools.Tools_String;

public class UniVecBlastObject extends BasicBlastObject implements Comparator<UniVecRegion> {

	/**
	 * Interpretations are based on univec documentation 
	 * {@link http://www.ncbi.nlm.nih.gov/VecScreen/VecScreen_docs.html}
	 * 
	 * All in values are converted to 0-based
	 */
	

	public static String hit_start = "Hsp_query-from";
	public static String hit_end = "Hsp_query-to";
	public static String Hsp_bit_score = "Hsp_bit-score";
	int c =-1;
	LinkedList<UniVecRegion> regions;
	boolean reversed = false;
	
	public UniVecBlastObject() throws Exception{
		super();
	}
	
	public boolean requiresTrim() throws Exception{
		if(regions == null)generateRegions();
		return this.getNoOfHits() != 0;
	}
	
	public UniVecRegion getRegion(int i) throws Exception{
		if(regions == null)generateRegions();
		return this.regions.get(i);
	}
	
	public List<UniVecRegion> getRegions() throws Exception{
		if(regions == null)generateRegions();
		return this.regions;
	}
	
	public int regionCount() throws Exception{
		if(regions == null)generateRegions();
		return regions.size();
	}	
	
	/**
	 * As always I've messed up basing, so
	 * this BlastObject treats works in 0-based
	 * 
	 * @throws Exception
	 */
	public void generateRegions() throws Exception{
		regions = new LinkedList<UniVecRegion>();
		int laststart = 0;
		int lastend =0;
		//Convert query length to 0-based
		int querylen = this.getQueryLength()-1;
		if(this.getNoOfHits() != 0){
			for(int i =1; i < this.getNoOfHits()+1;i++){	 //FUCK OFF 1-BASED crap!!!
				for(int j =1; j < this.getNoOfHsps(i)+1;j++){
					Integer start = Tools_String.parseString2Int(this.getHspTagContents(hit_start, i, j));
					Integer stop = Tools_String.parseString2Int(this.getHspTagContents(hit_end, i, j));
					Double score = Tools_String.parseString2Double(this.getHspTagContents(Hsp_bit_score, i, j));
					if(start != null && stop != null && score != null){
						//Convert start & stops to 0-based
						start-=1;
						stop-=1;
						UniVecRegion region = new UniVecRegion(start, stop, querylen, score);
						
						if(region.getRegionstrength() > 0 && laststart <start){
							regions.add(region);
							UniVecRegion region2 = new UniVecRegion(laststart, start, querylen, -1);
							if(region2.isRegionSuspect())regions.add(region2);
							laststart = start;
							lastend = stop;
						}
					}
					else{
						throw new Exception("Failed to parse hit_start/hit_stop/Hsp_bit-score tag(s)");
					}
				}
			}
			if(laststart != 0 && lastend != querylen){
				UniVecRegion region = new UniVecRegion(lastend, querylen, querylen, -1);
				if(region.isRegionSuspect())regions.add(region);
			}
		}
		mergeOverlaps();
	}

	private void mergeOverlaps(){
		Collections.sort(regions, this);
		for(int i =0;i < regions.size()-1; i++){
			logger.trace("Compare " + regions.get(i).getStop(0) +"("+regions.get(i).getStart(0)+") with " +regions.get(i+1).getStart(0));
			if(regions.get(i).getStop(0) >= regions.get(i+1).getStart(0)){
				regions.get(i).setStop(regions.get(i+1).getStop(0), 0);
				regions.get(i).setRegionstrength(Math.max(regions.get(i).getRegionstrength(), regions.get(i+1).getRegionstrength()));
				regions.remove(i+1);
				logger.trace("UniVec region overlapped, merged");
				
			}
		}
	}
	
	public int compare(UniVecRegion arg0, UniVecRegion arg1) {
		if(arg0.getStart(0) < arg1.getStart(0)){
			return reversed ? -1 : 1;
		}
		else if(arg0.getStart(0) > arg1.getStart(0)){
			return reversed ? 1 : -1;
		}
		else{
			if(arg0.getStop(0) < arg1.getStart(0)){
				return reversed ? -1 : 1;
			}
			else if(arg0.getStop(0) > arg1.getStop(0)){
				return reversed ? 1 : -1;
			}
			else return 0;
		}
	}
	
	public void reverseOrder() throws Exception{
		if(regions == null)generateRegions();
		reversed = true;
		Collections.sort(regions, this);
	}
	
}
