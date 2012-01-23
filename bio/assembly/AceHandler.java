package bio.assembly;

public interface AceHandler {

	public void addConsensusName(String name);
	
	public void addConsensus(StringBuffer buffer);
	
	public void addReadName(String name);
	
	public void addRead(StringBuffer buffer);
	
	public void addStart(int start);
	
}
