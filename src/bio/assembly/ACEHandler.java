package bio.assembly;

@Deprecated //Use ACEFileParser/ACERecord
public interface ACEHandler {

	
	/*
	 * 
	 * Reference sequence information
	 * 
	 */
	public void setRefName(String name);
	
	public String getRefName(int i);
	
	public void setRefConsensus(String buffer);
	
	public String getRefConsensus(String refname);
	
	public void setRefConsensusQuality(String buffer);
	
	public String getRefConsensusQuality(String refname);
		
	public int getRefLength(String refname);
	
	public void setNoOfBases(int i);
	
	public void setNoOfReads(int i);
	
	public void setBaseSegments(int i);
	
	public void setOrientation(char orient);
	
	/*
	 * Individual read data
	 */
	
	/*
	 * NOTE returns string, which is the name used by the handler,
	 * this may be necessary if the handler requires unique read names.
	 * This String should be used in place of qname for all future method
	 * calls, else an error will occur.
	 */
	public String addQNAME(String name);
	
	public void addSEQ(String sequence, String qname);
	
	public void addPOS(int start, String qname);
	
	/*
	 * Additional read data
	 */
	
	public void addOrientation(char orient, String qname);
	
	public void addRange(int start, int end, String qname);
	
	public void addRangePadded(int start, int end, String qname);
	
}
