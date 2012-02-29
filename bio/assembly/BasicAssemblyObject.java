package bio.assembly;

import java.util.LinkedHashMap;


//TODO Complete
public class BasicAssemblyObject implements SAMHandler, ACEHandler{
	
	LinkedHashMap<String, Integer> contigs;
	LinkedHashMap<String, String> reads;
	LinkedHashMap<String, String> cigars;
	LinkedHashMap<String, String> rpos;
	
	public BasicAssemblyObject(){
		contigs = new LinkedHashMap<String, Integer>();
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

	public void setFormatVersion(String format) {
		// TODO Auto-generated method stub
		
	}

	public String getFormatVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSortType(int sorttype) {
		// TODO Auto-generated method stub
		
	}

	public int getSortType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setRefName(String format) {
		// TODO Auto-generated method stub
		
	}

	public String getRefName(int i) {
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

	public void setGenomeAssemblyID(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getGenomeAssemblyID(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSequenceMD5(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getSequenceMD5(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSpecies(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getSpecies(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setURI(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getURI(String refname) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProgramID(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getProgramID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProgramName(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getProgramName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCommandLine(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getCommandLine() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPGIDChain(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getPGIDChain() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProgramVersion(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getProgramVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setComment(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getComment() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addQNAME(String string) {
		// TODO Auto-generated method stub
	}

	public void addFLAG(int parseString2Int, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addRNAME(String string, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addPOS(int parseString2Int, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addMAPQ(int parseString2Int, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addRNEXT(String string, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addCIGAR(String string, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addPNEXT(int parseString2Int, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addTLEN(int parseString2Int, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addSEQ(String string, String qname) {
		// TODO Auto-generated method stub
		
	}

	public void addQUAL(String string, String qname) {
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

}
