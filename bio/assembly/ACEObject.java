package bio.assembly;

public class ACEObject implements ACEHandler, Assembly {

	
	public void setRefName(String name) {
		// TODO Auto-generated method stub
		
	}

	public String getRefName(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRefConsensus(String buffer) {
		// TODO Auto-generated method stub
		
	}

	public String getRefConsensus(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRefConsensusQuality(String buffer) {
		// TODO Auto-generated method stub
		
	}

	public String getRefConsensusQuality(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRefLength(int i) {
		// TODO Auto-generated method stub
		
	}

	public int getRefLength(String refname) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setNoOfBases(int i) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	public void addSEQ(String sequence, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addPOS(int start, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addOrientation(char orient, String qname) {
		// TODO Auto-generated method stub
		
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
