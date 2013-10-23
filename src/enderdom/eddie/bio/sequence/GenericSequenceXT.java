package enderdom.eddie.bio.sequence;


public class GenericSequenceXT extends GenericSequence implements SequenceObjectXT{

	private int[] offsets;
	private char compliments;
	
	public GenericSequenceXT(String Identifier) {
		super(Identifier);
		init();
	}	
	public GenericSequenceXT(String Identifier, String sequence, String quality){
		super(Identifier, sequence, quality);
		init();
	}
	
	public GenericSequenceXT(String Identifier, String sequence){
		super(Identifier, sequence);
		init();
	}
	
	public void init(){
		compliments = 'U';
		offsets = new int[]{0,0,0,0,0};
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
