package enderdom.eddie.bio.sequence;

//SequenceObject with extra meta data (for contigs)
public interface SequenceObjectXT extends SequenceObject{

	public void setOffset(int offset, int base);
	
	public void setRange(int r1, int r2, int base);
	
	public void setPaddedRange(int r1, int r2, int base);
	
	public void setCompliment(char c);
	
	public int getOffset(int base);
	
	public int[] getRange(int base);
	
	public int[] getPaddedRange(int base);
	
	public char getCompliments();

}
