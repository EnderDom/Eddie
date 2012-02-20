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
	
	int currentdebug=0;
	
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
	
	public FourBitSequence(String sequence){
		this( sequence.length());
		parseString(sequence);
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
		for(int  i =0; i < sequence.length(); i=+16){
			int offset = i >> bitoff;
			for(int j=i; j < i+16; j++){
				switch (arr[j]) {
					case 'A' : dna[offset] <<= 2; dna[offset] |= 0x1; break;
					case 'C' : dna[offset] <<= 2; dna[offset] |= 0x2; break;
					case 'G' : dna[offset] <<= 2; dna[offset] |= 0x4; break;
					case 'T' : dna[offset] <<= 2; dna[offset] |= 0x8; break;
					
					case 'R' : dna[offset] <<= 2; dna[offset] |= 0x5; break;
					case 'Y' : dna[offset] <<= 2; dna[offset] |= 0xa; break;
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
				debugSeq(offset);
				if(j >= sequence.length())break;
			}
			
		}
	}

	public String getAsString(){
		return new String(getAsCharArray());
	}
	
	public char[] getAsCharArray(){
		char[] array = new char[this.length];
		int arraycount=0;
		if(LorR){
			for(int i =0; i < dna.length; i++){
				long mask = 0xf;
				long charvalue = 0x0;
				for(int j = 0; j < 64; j<<=4){
					charvalue = (mask & dna[i]) >> j;
					if(charvalue==0x1)array[arraycount] = 'A';
					else if(charvalue==0x2) array[arraycount] = 'C';
					else if(charvalue==0x4) array[arraycount] = 'G';
					else if(charvalue==0x8) array[arraycount] = 'T';
					
					else if(charvalue==0x5) array[arraycount] = 'R';
					else if(charvalue==0xa) array[arraycount] = 'Y';
					else if(charvalue==0x6) array[arraycount] = 'S';
					else if(charvalue==0x9) array[arraycount] = 'W';
					else if(charvalue==0xc) array[arraycount] = 'K';
					else if(charvalue==0x3) array[arraycount] = 'M';
					
					else if(charvalue==0xe) array[arraycount] = 'B';
					else if(charvalue==0xd) array[arraycount] = 'D';
					else if(charvalue==0xb) array[arraycount] = 'H';
					else if(charvalue==0x7) array[arraycount] = 'V';
					
					else if(charvalue==0xf) array[arraycount] = 'N';
					else if(charvalue==0xf) array[arraycount] = 'X';
					else if(charvalue==0x0) array[arraycount] = '-';
					else if(charvalue==0x0) array[arraycount] = '*';
					arraycount++;
					if(arraycount == this.length)break;
					mask<<=4;
				}
			}
		}
		else{
			
		}
		return null;
	}
	
	public void debugSeq(int i){
		if(i != currentdebug){
			System.out.println(print(dna[currentdebug]));
		}
		String r = print(dna[i]);
		System.out.print("\r" + r);
		currentdebug = i;
	}
	
	public String print(long p){
		String st = new String();
		long mask = 0x1;
		long z = p;
		for(int i =0; i < 64; i ++){
			st = st + (int)(mask&z);
			z>>=1;
		}
		return st;
	}
}
