package enderdom.eddie.bio.sequence;


public class GenericSequenceXT extends GenericSequence implements SequenceObjectXT{

	private int[] offsets;
	private char compliments;
	
	public GenericSequenceXT(String Identifier) {
		super(Identifier);
		
	}

	public GenericSequenceXT(String Identifier, int position){
		super(Identifier, position);
	}
	
	public GenericSequenceXT(String Identifier, String sequence, int position){
		super(Identifier, sequence, position);
	}
	
	public GenericSequenceXT(String Identifier, String sequence, String quality, int position){
		super(Identifier, sequence,quality, position);
	}
	
	public GenericSequenceXT(String Identifier, String sequence, String quality){
		super(Identifier, sequence, quality);
	}
	
	public GenericSequenceXT(String Identifier, String sequence){
		super(Identifier, sequence);
	}
	
	public void setOffset(int offset, int base){
		offCheck();
		offsets[0] = offset-base;
	}
	
	public void setRange(int r1, int r2, int base){
		offsets[1] = r1-base;
		offsets[2] = r2-base;
	}
	
	public void setPaddedRange(int r1, int r2, int base){
		offsets[3] = r1-base;
		offsets[4] = r2-base;
	}
	
	public void setCompliment(char c){
		this.compliments = c;
	}
	
	public int getOffset(int base){
		return offsets[0]+base;
	}
	
	public int[] getRange(int base){
		return new int[]{offsets[1]+base, offsets[2]+base};
	}
	
	public int[] getPaddedRange(int base){
		return new int[]{offsets[3]+base, offsets[4]+base};
	}
	
	public char getCompliments(){
		return compliments;
	}
	
	private void offCheck(){
		if(offsets == null)offsets = new int[5];
	}
	
}
