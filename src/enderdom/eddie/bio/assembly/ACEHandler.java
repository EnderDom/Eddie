package enderdom.eddie.bio.assembly;

/**
 * @author dominic
 * Class is deprecated, use ACEFileParser Instead
 *
 */
@Deprecated
public interface ACEHandler {

	
	/**
	 * 
	 * @param name
	 */
	public void setRefName(String name);
	
	/**
	 * 
	 * @param i
	 * @return Name of contig at that index
	 */
	public String getRefName(int i);
	
	/**
	 *  Set contig Sequence
	 * @param buffer
	 */
	public void setRefConsensus(String buffer);
	
	/**
	 * 
	 * @param refname
	 * @return Consensus sequences with refname
	 */
	public String getRefConsensus(String refname);
	
	/**
	 * 
	 * @param buffer
	 */
	public void setRefConsensusQuality(String buffer);
	
	/**
	 * 
	 * @param refname
	 * @return Consensus quality string
	 */
	public String getRefConsensusQuality(String refname);
	
	/**
	 * 
	 * @param refname
	 * @return length of sequence attached to refname
	 */
	public int getRefLength(String refname);
	
	/**
	 * @see ACEParser
	 * @param i
	 */
	public void setNoOfBases(int i);
	
	/**
	 * @see ACEParser
	 * @param i
	 */
	public void setNoOfReads(int i);
	
	/**
	 * @see ACEParser
	 * @param i
	 */
	public void setBaseSegments(int i);
	
	/**
	 * 
	 * @param orient
	 * @see ACEParser
	 */
	public void setOrientation(char orient);
	
	/**
	 * Individual read data
	 * 
	 * @param name Add a read's name  
	 * @return the internal read for name,
	 * which is the name used by the handler,
	 * this may be necessary if the handler requires unique read names.
	 * This String should be used in place of qname for all future method
	 * calls, else an error will occur.
	 */
	public String addQNAME(String name);
	
	/**
	 * Add read sequence, use name returned by addQname
	 * 
	 * @param sequence
	 * @param qname
	 * 
	 */
	public void addSEQ(String sequence, String qname);
	
	/**
	 * Use name returned by addQname
	 * 
	 * @see ACEParser
	 * @param start
	 * @param qname
	 */
	public void addPOS(int start, String qname);
	
	/**
	 * Additional read data
	 * @param orient orientation should be 'C' or 'U'
	 * ie complimented or uncomplimented
	 * @param qname the name returned by add qName
	 */
	public void addOrientation(char orient, String qname);
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @param qname
	 */
	public void addRange(int start, int end, String qname);
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @param qname
	 */
	public void addRangePadded(int start, int end, String qname);
	
}
