package bio.sequence;

/*
 * 
 * Experimental 4 bit sequence object
 * 
 * Uses and inspired by BitSet Java class 
 * 
 */


public class FourBitSequence {

	/*
	 * The actual DNA store
	 */
	long[] dna;
	
	/*
	 * Mask:
	 * needs to equal to the amount
	 * offset by the right bitwise shift
	 * ie ..11111111 
	 */
	int bitmask = 0xff;
	
	int bitoff = 8; //2 more powers from BitSet's 6 as DNA is 4 bits large
	
	int length;
	
	/*
	 * Read bits from left or right
	 * this will either consider the 
	 * sequence reversed and complimented
	 */
	boolean LorR;
	
	public FourBitSequence(){
		this(16);
	}
	
	//Length of Sequence
	public FourBitSequence(int nlength){
		
		if(nlength < 0) throw new NegativeArraySizeException();
		this.length = nlength;
		/*
		 * Note: would shift only by 6
		 * but 4 bits per nucleotide so to
		 *  additional powers to be added
		 */
		int length_long = nlength >> bitoff;
		
		/*
		 * If the length is not a multiple of 
		 * 64 then we need an length+1
		 * to accommodate the size
		 */
		if ((nlength & bitmask) != 0)	++length_long;
		dna = new long[length_long];
	}
	
	//TODO complete
	public boolean get(int pos){
		int offset = pos >> bitoff;
		if (offset >= dna.length){
			
		}
		return false;
	}
	
	/*
	 *  Any information held by case will be lost
	 */
	public void parseString(String sequence){
		sequence = sequence.toUpperCase();
		char[] arr = sequence.toCharArray();
		for(int  i =0; i < sequence.length(); i++){
			int offset = i >> bitoff;
			switch (arr[i]) {
				case 'A' : dna[offset] <<= 2; dna[offset] |= 0x1; break;
				case 'C' : dna[offset] <<= 2; dna[offset] |= 0x2;break;
				case 'G' : dna[offset] <<= 2; dna[offset] |= 0x4; break;
				case 'T' : dna[offset] <<= 2; dna[offset] |= 0x8; break;
				
				case 'R' : dna[offset] <<= 2; dna[offset] |= 0x5; break;
				case 'Y' : dna[offset] <<= 2; dna[offset] |= 0xa;break;
				case 'S' : dna[offset] <<= 2; dna[offset] |= 0x6; break;
				case 'W' : dna[offset] <<= 2; dna[offset] |= 0x9; break;
				case 'K' : dna[offset] <<= 2; dna[offset] |= 0xc; break;
				case 'M' : dna[offset] <<= 2; dna[offset] |= 0x3; break;
				
				case 'B' : dna[offset] <<= 2; dna[offset] |= 0xe; break;
				case 'D' : dna[offset] <<= 2; dna[offset] |= 0xd; break;
				case 'H' : dna[offset] <<= 2; dna[offset] |= 0xb; break;
				case 'V' : dna[offset] <<= 2; dna[offset] |= 0x7; break;
				
				case 'N' : dna[offset] <<= 2; dna[offset] |= 0xf; break;
				case 'X' : dna[offset] <<= 2; dna[offset] |= 0xf; break;
				case '-' : dna[offset] <<= 2; dna[offset] |= 0x0; break;
				case '*' : dna[offset] <<= 2; dna[offset] |= 0x0; break;
			}
		}
	}

	public String getAsString(){
		return new String(getAsCharArray());
	}
	
	public char[] getAsCharArray(){
		
		if(LorR){
			for(int  i =0; i < this.length; i++){
				int offset = i >> bitoff;
				
			}
		}
		else{
			
		}
		return null;
	}
}
