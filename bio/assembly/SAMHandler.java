package bio.assembly;

public interface SAMHandler {

	public static String[] sorttypes = {"unknown", "unsorted", "queryname", "coordinate"};
	
	/* 
	 * Header File Values
	 */
	public void setFormatVersion(String format);
	
	public String getFormatVersion();
	
	public void setSortType(int sorttype);
	
	public int getSortType();
	
	
	/*
	 * Reference sequence dictionary
	 */
	
	/*
	 * In most cases this is the contig title
	 * and is mandatory
	 */
	
	public void setRefName(String format);
	
	public String getRefName();
	
	/*
	 * Mandatory Contig Length
	 */
	public void setRefLength(int i);
	
	public int getRefLength();

	/*
	 * Non-Mandatory Fields
	 */
	public void setGenomeAssemblyID(String substring);
	
	public String getGenomeAssemblyID();

	public void setSequenceMD5(String substring);
	
	public String getSequenceMD5();

	public void setSpecies(String substring);
	
	public String getSpecies();

	/*
	 * Leave it to the Handler to convert this to 
	 * a URI object if it wants to.
	 */
	public void setURI(String substring);
	
	public String getURI();

	
	/*
	 * 
	 * Read Grouping
	 * TODO
	 * 
	 */
	
	/*
	 * 
	 * Program Details
	 * 
	 */

	public void setProgramID(String substring);
	
	public String getProgramID();
	
	public void setProgramName(String substring);
	
	public String getProgramName();

	/*
	 * Hmm... I guess this is the command line args for the
	 * above program? 
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

	public void addQNAME(String string);

	public void addFLAG(int parseString2Int);

	public void addRNAME(String string);

	public void addPOS(int parseString2Int);

	public void addMAPQ(int parseString2Int);

	public void addRNEXT(String string);

	public void addCIGAR(String string);

	public void addPNEXT(int parseString2Int);

	public void addTLEN(int parseString2Int);

	public void addSEQ(String string);

	public void addQUAL(String string);
	
	
}
