package enderdom.eddie.bio.lists;

public interface FastaHandler {	
	
	public void addSequence(String title, String sequence);
	
	public void addQuality(String title, String quality);
	
	public void addAll(String title, String sequence, String quality);
	
	public void setFastq(boolean fastq);

	public boolean isFastq();
	
}
