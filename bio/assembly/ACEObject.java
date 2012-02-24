package bio.assembly;

import java.util.LinkedHashMap;

public class ACEObject implements ACEHandler, Assembly {

	LinkedHashMap<Integer, String> contignumb;
	LinkedHashMap<String, String> contigs;
	LinkedHashMap<String, String> qualities;
	
	LinkedHashMap<String, String> reads;
	LinkedHashMap<String, String> read2contig;
	
	LinkedHashMap<String, Integer> offset;
	LinkedHashMap<String, Integer> rangeleft;
	LinkedHashMap<String, Integer> rangeright;
	LinkedHashMap<String, Integer> orient;
	
	int currentcontig = -1;
	
	public ACEObject(){
		contigs = new LinkedHashMap<String, String>();
		read2contig = new LinkedHashMap<String, String>();
		reads = new LinkedHashMap<String, String>();
	}
	
	public void setRefName(String name) {
		currentcontig++;
		contigs.put(name, "");
		contignumb.put(currentcontig, name);
	}

	public String getRefName(int i) {
		return contignumb.get(i);
	}

	public void setRefConsensus(String buffer) {
		contigs.put(contignumb.get(currentcontig), buffer);
	}

	public String getRefConsensus(String refname) {
		return contigs.get(refname);
	}

	public void setRefConsensusQuality(String buffer) { 
		qualities.put(contignumb.get(currentcontig), buffer);
	}

	public String getRefConsensusQuality(String refname) {
		return qualities.get(refname);
	}

	
	//TODO implement
	public void setRefLength(int i) {
		
	}

	public int getRefLength(String refname) {
		return 0;
	}

	public void setNoOfBases(int i) {

	}

	public void setNoOfReads(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setBaseSegments(int i) {
		// TODO Auto-generated method stub
		
	}

	public void setOrientation(char orient) {
		// TODO Auto-generated method stub
		
	}

	public void addQNAME(String name) {
		reads.put(name, "");	
	}

	public void addSEQ(String sequence, String qname) {
		reads.put(qname, sequence);
	}

	public void addPOS(int start, String qname) {
		offset.put(qname, start);
	}

	public void addOrientation(char orient, String qname) {
		
	}

	public void addRange(int start, int end, String qname) {
		// TODO Auto-generated method stub
	}

	public void addRangePadded(int start, int end, String qname) {
		// TODO Auto-generated method stub
		
	}

	public int getContigsSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getContigIndex(String contigname) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDepthofContigAtPos(int contigindex, int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getAverageCoverageDepth(int contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getGlobalAverageCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGlobalMedianCoverageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getReadsSize(int contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getAvgReadLength(int contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTotalNoOfbpContig(int contigindex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGlobalNoOfbp() {
		// TODO Auto-generated method stub
		return 0;
	}

}
