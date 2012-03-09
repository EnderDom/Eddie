package bio.assembly;

public interface SAMHandler {

	public static String[] sorttypes = {"unknown", "unsorted", "queryname", "coordinate"};
	
	/* 
	 * Header File Values @HD
	 */
	public void setFormatVersion(String format);
	
	public String getFormatVersion();
	
	public void setSortType(int sorttype);
	
	public int getSortType();
	
	
	/*
	 * Reference sequence dictionary @SQ
	 */
	
	/*
	 * In most cases this is the contig title
	 * and is mandatory
	 */
	
	public void setRefName(String format);
	
	public String getRefName(int i);
	
	/*
	 * Mandatory Contig Length
	 */
	public void setRefLength(int i);
	
	public int getRefLength(String refname);

	/*
	 * Non-Mandatory Fields
	 */
	public void setGenomeAssemblyID(String substring);
	
	public String getGenomeAssemblyID(String refname);

	public void setSequenceMD5(String substring);
	
	public String getSequenceMD5(String refname);

	public void setSpecies(String substring);
	
	public String getSpecies(String refname);

	/*
	 * Leave it to the Handler to convert this to 
	 * a URI object if it wants to.
	 */
	public void setURI(String substring);
	
	public String getURI(String refname);

	
	/*
	 * 
	 * Read Grouping @RG
	 * TODO
	 * 
	 */
	
	/*
	 * 
	 * Program Details @PG
	 * 
	 */

	public void setProgramID(String substring);
	
	public String getProgramID();
	
	public void setProgramName(String substring);
	
	public String getProgramName();

	/*
	 * Hmm... I guess this is the command line args
	 * for the above program? 
	 */
	
	public void setCommandLine(String substring);
	
	public String getCommandLine();

	/*
	 * Hah, like i'll ever implement this
	 */
	public void setPGIDChain(String substring);
	
	public String getPGIDChain();

	public void setProgramVersion(String substring);
	
	public String getProgramVersion();
	
	public void setComment(String substring);
	
	public String getComment();
	
	//Read Data

	public String addQNAME(String string);

	public void addFLAG(int parseString2Int, String qname);

	public void addRNAME(String string, String qname);

	public void addPOS(int parseString2Int, String qname);

	public void addMAPQ(int parseString2Int, String qname);

	public void addRNEXT(String string, String qname);

	public void addCIGAR(String string, String qname);

	public void addPNEXT(int parseString2Int, String qname);

	public void addTLEN(int parseString2Int, String qname);

	public void addSEQ(String string, String qname);

	public void addQUAL(String string, String qname);
	
	
}
