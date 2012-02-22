package bio.sequence;

import org.apache.log4j.Logger;

import tools.Tools_Bit;

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
	 * ie ..1111 
	 */
	int bitmask = 0xf;
	
	int bitoff = 4; //2 less powers from BitSet's 6 as DNA is 4 bits large
	
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
		if ((nlength & bitmask) != 0)length_long++;
		Logger.getRootLogger().debug("Length of Sequence "+ nlength + " fits into array of "+length_long+ " longs");
		dna = new long[length_long];
	}
	
	public FourBitSequence(String sequence){
		this( sequence.length());
		parseString(sequence);
	}
	
	public char get(int pos){
		//TODO add if revo
		if(pos >= this.length) throw new ArrayIndexOutOfBoundsException();
		if(!LorR){
			int offset = pos >> bitoff;
			System.out.println("OFFSET : " + offset);
			int sub_numb = (pos % 16)*4;
			System.out.println("SUBNUM : " + sub_numb);
			System.out.println(Tools_Bit.LongAsBitString(this.dna[offset]));
			System.out.println(Tools_Bit.LongAsBitString(this.dna[offset]>>sub_numb));
			System.out.println(Tools_Bit.LongAsBitString(0xf<<60));
			System.out.println(Tools_Bit.LongAsBitString((0xf&(this.dna[offset]>>sub_numb))));
			System.out.println("");
			return getAsChar(0xf&(this.dna[offset]>>sub_numb), this.LorR);
		}
		else{
			int offset = pos >> bitoff;
			int sub_numb = (64-(pos % 16))*4;
			return getAsChar(0xf&(this.dna[this.dna.length-offset-1])<<sub_numb, this.LorR);
		}
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
				case 'A' : dna[offset] <<= 4; dna[offset] |= 0x1; break;
				case 'C' : dna[offset] <<= 4; dna[offset] |= 0x2; break;
				case 'G' : dna[offset] <<= 4; dna[offset] |= 0x4; break;
				case 'T' : dna[offset] <<= 4; dna[offset] |= 0x8; break;
				
				case 'R' : dna[offset] <<= 4; dna[offset] |= 0x5; break;
				case 'Y' : dna[offset] <<= 4; dna[offset] |= 0xa; break;
				case 'S' : dna[offset] <<= 4; dna[offset] |= 0x6; break;
				case 'W' : dna[offset] <<= 4; dna[offset] |= 0x9; break;
				case 'K' : dna[offset] <<= 4; dna[offset] |= 0xc; break;
				case 'M' : dna[offset] <<= 4; dna[offset] |= 0x3; break;
				
				case 'B' : dna[offset] <<= 4; dna[offset] |= 0xe; break;
				case 'D' : dna[offset] <<= 4; dna[offset] |= 0xd; break;
				case 'H' : dna[offset] <<= 4; dna[offset] |= 0xb; break;
				case 'V' : dna[offset] <<= 4; dna[offset] |= 0x7; break;
				
				case 'N' : dna[offset] <<= 4; dna[offset] |= 0xf; break;
				case 'X' : dna[offset] <<= 4; dna[offset] |= 0xf; break;
				case '-' : dna[offset] <<= 4; dna[offset] |= 0x0; break;
				case '*' : dna[offset] <<= 4; dna[offset] |= 0x0; break;
				case '.' : dna[offset] <<= 4; dna[offset] |= 0x0; break;
				default  : Logger.getRootLogger().warn("Input Non-DNA character" + arr[i]); break;
			}
			//debugSeq(offset);
			//System.out.print(arr[i] + " ");
		}
		
		dna[dna.length-1]<<= (16-(sequence.length() % 16))*4; /*
		 													   * This shifts the incomplete 
		 													   * long fully to the right,
		 													   * there most be a simpler way
		 													   * but I can't think of one atm.
		 													   */ 
		//debugSeq(dna.length-1);
		//System.out.print("\n");
	}
	
	public String getAsStringRevComp(){
		return new String(getAsCharArray(!this.LorR));
	}

	public String getAsString(){
		return new String(getAsCharArray(this.LorR));
	}
	
	public char[] getAsCharArray(boolean leftorright){
		char[] array = new char[this.length];
		int arraycount=0;
		if(!leftorright){
			for(int i =0; i < dna.length; i++){
				long mask = 0xf;			
				mask<<=60;				
				long charvalue = 0x0;
				for(int j = 0; j <64; j+=4){
					charvalue = (mask & dna[i])>>(60-j);
					//System.out.println(Tools_Bit.LongAsBitString(charvalue));
					array[arraycount] = getAsChar(charvalue, leftorright);
					arraycount++;
					if(arraycount == this.length)break;
					mask>>>=4;
				}
			}
		}
		else{
			for(int i =dna.length-1; i > -1; i--){
				long mask = 0xf;			
				long charvalue = 0x0;
				int shift = 64;
				int jshift = 0;
				if(i==dna.length-1){ 
					shift = (this.length % 16)*4;
					jshift = (64-shift);
					mask<<=jshift;
				}
				for(int j = 0; j <shift; j+=4){
					charvalue = (mask & dna[i])>>j+jshift;
					//System.out.println(Tools_Bit.LongAsBitString(charvalue));
					array[arraycount] = getAsChar(charvalue, leftorright);
					arraycount++;
					if(arraycount == this.length)break;
					mask<<=4;
				}
			}
		}
		return array;
	}
	
	private static char getAsChar(long charvalue, boolean invert){
		if(!invert){
			if(charvalue==0x1) return 'A';
			else if(charvalue==0x2) return 'C';
			else if(charvalue==0x4) return 'G';
			else if(charvalue==0x8) return 'T';
			
			else if(charvalue==0x5) return 'R';
			else if(charvalue==0xa) return 'Y';
			else if(charvalue==0x6) return 'S';
			else if(charvalue==0x9) return 'W';
			else if(charvalue==0xc) return 'K';
			else if(charvalue==0x3) return 'M';
			
			else if(charvalue==0xe) return 'B';
			else if(charvalue==0xd) return 'D';
			else if(charvalue==0xb) return 'H';
			else if(charvalue==0x7) return 'V';
			
			else if(charvalue==0xf) return 'N';
			else return '-';
		}
		else{
				 if(charvalue==0x1) return'T';
			else if(charvalue==0x2) return'G';
			else if(charvalue==0x4) return'C';
			else if(charvalue==0x8) return'A';
			
			else if(charvalue==0x5) return'Y';
			else if(charvalue==0xa) return'R';
			else if(charvalue==0x6) return'S';
			else if(charvalue==0x9) return'W';
			else if(charvalue==0xc) return'M';
			else if(charvalue==0x3) return'K';
			
			else if(charvalue==0xe) return'V';
			else if(charvalue==0xd) return'H';
			else if(charvalue==0xb) return'D';
			else if(charvalue==0x7) return'B';
			
			else if(charvalue==0xf) return'N';
			else return '-';
		}
	}
	
	
	@SuppressWarnings("unused")
	private void debugSeq(int i){
		if(i != currentdebug){
			System.out.print("\n");
		}
		String r = Tools_Bit.LongAsBitString(dna[i]);
		System.out.print("\n" + r);
		currentdebug = i;
	}
	
}
