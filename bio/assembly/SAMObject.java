package bio.assembly;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

public class SAMObject implements SAMHandler{
	
	LinkedHashMap<String, Integer> contigs;
	LinkedHashMap<String, String> reads;
	LinkedHashMap<String, String> cigars;
	LinkedHashMap<String, String> rpos;
	
	public SAMObject(){
		contigs = new LinkedHashMap<String, Integer>();
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

	public String getRefName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRefLength(int i) {
		// TODO Auto-generated method stub
		
	}

	public int getRefLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setGenomeAssemblyID(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getGenomeAssemblyID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSequenceMD5(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getSequenceMD5() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSpecies(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getSpecies() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setURI(String substring) {
		// TODO Auto-generated method stub
		
	}

	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProgramID(String substring) {
		Logger.getRootLogger().info("Program ID for this assemlby is" + substring);
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

	public void addFLAG(int parseString2Int) {
		// TODO Auto-generated method stub
		
	}

	public void addRNAME(String string) {
		// TODO Auto-generated method stub
		
	}

	public void addPOS(int parseString2Int) {
		// TODO Auto-generated method stub
		
	}

	public void addMAPQ(int parseString2Int) {
		// TODO Auto-generated method stub
		
	}

	public void addRNEXT(String string) {
		// TODO Auto-generated method stub
		
	}

	public void addCIGAR(String string) {
		// TODO Auto-generated method stub
		
	}

	public void addPNEXT(int parseString2Int) {
		// TODO Auto-generated method stub
		
	}

	public void addTLEN(int parseString2Int) {
		// TODO Auto-generated method stub
		
	}

	public void addSEQ(String string) {
		// TODO Auto-generated method stub
		
	}

	public void addQUAL(String string) {
		// TODO Auto-generated method stub
		
	}

}
