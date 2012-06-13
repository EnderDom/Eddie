package bio.sequence;

import org.apache.log4j.Logger;

/*
 * 
 * Experimental 4 bit sequence object
 * 
 * Inspired by BitSet Java class 
 * 
 * Holds sequence data in 4 bit sections inside 
 * a long array. The sequence can be reversed simply
 * by switching the direction of reading the longs 
 * and returning opposite characters for the values
 * mainly represented in hex for ease, but I am aware
 * that this is actually int values, which means you can't
 * set long values greater than 32bit with hex. 
 * 
 */

/*
 * NB: Maximum length of sequence 2^32
 * For larger sequences probably need 
 * a array/list containing object
 */

public class FourBitSequence implements CharSequence{

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
	
	//Length in bp of sequence inc. '*'
	int length;
	//Length without '*'/'-'
	int actlength;
	
	/*
	 * Read bits from left or right
	 * this will either consider the 
	 * sequence reversed and complimented
	 */
	boolean forward = true;
		
	long lmask;
	long rmask;
	
	public FourBitSequence(){
		init(16);
	}
	
	//Length of Sequence
	public FourBitSequence(int nlength){
		init(nlength);
	}
	
	public FourBitSequence(String sequence){
		init(sequence.length());
		parseString(sequence);
	}
	
	public void init(int nlength){
		if(nlength < 0) throw new NegativeArraySizeException();
		rmask = 0xf;
		lmask = 0xf;//Should replace with  0xF000000000000000L;
		lmask <<=60;
		
		this.length = nlength;
		/*
		 * Note: would shift only by 6
		 * but 4 bits per nucleotide so 2
		 * additional powers to be added
		 */
		int length_long = nlength >> bitoff;
		
		/*
		 * If the length is not a multiple of 
		 * 64 then we need an length+1
		 * to accommodate the size
		 */
		if ((nlength & bitmask) != 0)length_long++;
		dna = new long[length_long];
	}
	
	public char get(int pos){
		/* 
		 * This method will return '-' for any values outside the actual sequence
		 * This could potential lead to issues when bugs are left uncaught, as
		 * such if calling bp positions outside sequence is not needed use getInside()
		 */
		if(pos >= this.length || pos < this.length*-1) return getAsChar(0, this.forward);
		else{
			boolean lor = this.forward;
			if(pos < 0){
				/* Assuming that negative value refers to forward reversecomp char
				 * Also assumes that -pos is == the position from length
				 * ie -1 == the last index for sequence 
				 * thus for ATCG
				 * -1 == the end bp G, which will be complimented to C
				 */ 
				pos = this.length +pos;
				lor = !lor; /*
					This could potentially lead to confusion
					if downstream one forgets that the sequence is 
					already forward or reverse, as there is no way of keeping track of
					the sequence's original sense as yet
				*/
			}
			if(lor){
				pos = this.length -pos-1;
			}
			int offset = pos >> bitoff;
			int sub_numb = 60-(pos % 16)*4;
			return getAsChar(rmask&(this.dna[offset]>>sub_numb), lor);
		}
	}
	
	/*
	 * as with get, but throws exception if pos call is out of range
	 */
	public char getInside(int pos){
		if(pos >= this.length || pos < this.length*-1) throw new IndexOutOfBoundsException();
		return get(pos);
	}
	
	/*
	 *  Any information held by case will be lost
	 */
	public void parseString(String sequence){
		if (this.length == 0)init(sequence.length());
		this.actlength=this.length;
		sequence = sequence.toUpperCase();
		char[] arr = sequence.toCharArray();
		for(int  i =0; i < arr.length; i++){
			int offset = i >> bitoff;
			if(offset == dna.length)this.extend(16); //Should only be necessary if this is instantiated without string
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
				case '-' : dna[offset] <<= 4; dna[offset] |= 0x0; this.actlength--; break;
				case '*' : dna[offset] <<= 4; dna[offset] |= 0x0; this.actlength--; break;
				case '.' : dna[offset] <<= 4; dna[offset] |= 0x0; this.actlength--; break;
				default  : Logger.getRootLogger().warn("Input Non-DNA character: " + arr[i]);this.length--; break; 
			}
		}
		
		dna[dna.length-1]<<= (16-(sequence.length() % 16))*4; /*
		 													   * This shifts the incomplete 
		 													   * long fully to the right,
		 													   * there might be a simpler way
		 													   * but I can't think of one atm.
		 													   */ 
	}
	
	public String getAsStringRevComp(){
		return new String(getAsCharArray(!this.forward));
	}

	public String getAsString(){
		return new String(getAsCharArray(this.forward));
	}
	
	/*
	 * TODO
	 * I have realised it may be better to shift the data
	 * values rather than the mask, as I have done in the 
	 * get() method. This also simplifies reverse compliment
	 * sequence as the only change will be setting 
	 * length-arraycount-1 values so this is to improve
	 * 
	 */
	
	public char[] getAsCharArray(boolean forward){
		char[] array = new char[this.length];
		int arraycount=0;
		if(!forward){
			for(int i =0; i < dna.length; i++){
				long mask = 0xf; //Should replace with  0xF000000000000000L;
				mask <<=60;
				long charvalue = 0x0;
				for(int j = 0; j <64; j+=4){
					charvalue = (mask & dna[i])>>>(60-j);
					array[arraycount] = getAsChar(charvalue, forward);
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
					array[arraycount] = getAsChar(charvalue, forward);
					arraycount++;
					if(arraycount == this.length)break;
					mask<<=4;
				}
			}
		}
		return array;
	}
	
	private static char getAsChar(long charvalue, boolean forward){
		if(forward){
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
				 if(charvalue==0x1) return 'T';
			else if(charvalue==0x2) return 'G';
			else if(charvalue==0x4) return 'C';
			else if(charvalue==0x8) return 'A';
			
			else if(charvalue==0x5) return 'Y';
			else if(charvalue==0xa) return 'R';
			else if(charvalue==0x6) return 'S';
			else if(charvalue==0x9) return 'W';
			else if(charvalue==0xc) return 'M';
			else if(charvalue==0x3) return 'K';
			
			else if(charvalue==0xe) return 'V';
			else if(charvalue==0xd) return 'H';
			else if(charvalue==0xb) return 'D';
			else if(charvalue==0x7) return 'B';
			
			else if(charvalue==0xf) return 'N';
			else return '-';
		}
	}
	
	public void append(String input){
		FourBitSequence seq = new FourBitSequence(input);
		append(seq);
	}
	
	/*
	 * This method gets the bitshift value for the end of this 
	 * sequence then adds a the data from seq shifted to fit into the array
	 * 
	 * ie (simplified to 2byte for demo):
	 * this data(d1)  Len=2:
	 * 0010 0010 0000 0000
	 * to have this data appended (d2) Len=4
	 * 0001 0001 0010 0010
	 * 
	 * so the method does this:
	 * d2_rshift = 0001 0001 0010 0010 (>>>)--> 0000 0000 0001 0001
	 * d2_lshift = 0001 0001 0010 0010 (<<)--> 0010 0010 0000 0000  
	 * 
	 * new data[0] = d1[0] | d2_rshift ie 
	 * 0010 0010 0000 0000
	 * 0000 0000 0001 0001
	 *        ||
	 *        \/
	 * 0010 0010 0001 0001
	 * 
	 * new data[1] = 0010 0010 0000 0000  (dl2_lshift)
	 * 
	 * 
	 * As the final long is already shifted, the long doesn't need shifting
	 * again as in the parseString method
	 */
	
	public void append(FourBitSequence seq){
		/*
		 * Get the bitshift value from this seq
		 */
		int shiftval = (this.length % 16)*4;
		
		//Set new lengths
		this.length +=seq.getLength();
		this.actlength += seq.getActualLength();
		
		//Recreate dna data as in init()
		int length_long = this.length >> bitoff;			
		if ((this.length & bitmask) != 0)length_long++;
		long[] data = new long[length_long];
		
		//Add this object dna[i] to data[i]
		int i =0;
		for(; i < this.dna.length; i++){
			data[i] = this.dna[i];
		}
		//Add seq object data, this needs to be shifted accordingly
		int offset = i;
		for(; i < length_long; i++){
			data[i-1] |= seq.dna[i-offset]>>>shiftval;
			data[i] = seq.dna[i-offset]<<64-shiftval;
		}
		//Set this dna data as data
		this.dna = data;
	}
	/*
	 * Extends the length of the dna[]
	 */
	private void extend(int len){
		long[] newdna = new long[dna.length+len];
		for(int i =0; i < dna.length ; i++){
			newdna[i] = dna[i];
		}
		this.dna=newdna;
	}
	
	/*
	 * flips reversecomp flag
	 */
	public void toReverseComp(){
		this.forward = !this.forward;
	}
	
	public int getLength(){
		return this.length;
	}
	
	/*
	 * Returns length minus any '*'/'-' chars
	 */
	public int getActualLength(){
		return this.actlength;
	}
	
	/*
	 * String overlap methods
	 * 
	 */
	public int length(){
		return this.length;
	}

	public char charAt(int arg0) {
		return this.get(arg0);
	}

	//TODO replace with proper method
	public CharSequence subSequence(int arg0, int arg1) {
		return new FourBitSequence(this.getAsString().substring(arg0, arg1));
	}
	
	public String toString(){
		return this.getAsString();
	}
	
	
	/**
	 * 
	 * Currently only supports standard IUPAC notation
	 * for alternative codon translation, use BioJava
	 * 
	 * UNTESTED AS YET
	 * 
	 * @param frame is 0-based
	 * @param forward
	 * @return char array of protein values
	 */
	public char[] getProtein(int frame, boolean forward){
		Logger.getRootLogger().warn("Not yet implemented fully");
		char[] arr = new char[this.length+frame];
		long skit = 0x0000000000000000L;
		long mask = 0x0000000000000FFFL;
		int j=0;
		if(forward){
			for(int i =0; i < dna.length; i++){
				for(int y =64-(frame*4); y > -1;y-=12){
					skit = (dna[i]>>y)&mask;
					arr[j]= getAmino(skit, forward);
				}
				if(frame == 0 && dna.length-1 != i){
					skit = (dna[i]>>60)|(dna[i+1]&(mask>>4));
				}
				else if(frame == 2 && dna.length-1 != i){
					skit = (dna[i]>>56)|(dna[i+1]&(mask>>8));
				}
			}
		}
		return arr;
	}
	
	public static char getAmino(long skit, boolean forward){
		if(forward){
			if((skit & 0x000000000000042FL) == 0)return 'A'; //GCN
			if((skit & 0x000000000000024FL) == 0)return 'R'; //CGN
			if((skit & 0x0000000000000345L) == 0)return 'R';//MGR
			if((skit & 0x000000000000011AL) == 0)return 'N';//AAY
			if((skit & 0x000000000000041AL) == 0)return 'D';//GAY
			if((skit & 0x000000000000084AL) == 0)return 'C';//TGY
			if((skit & 0x0000000000000215L) == 0)return 'Q';//CAR
			if((skit & 0x0000000000000415L) == 0)return 'E';//GAR
			if((skit & 0x000000000000044FL) == 0)return 'G';//GGN
			if((skit & 0x000000000000018BL) == 0)return 'I';//ATH
			if((skit & 0x0000000000000184L) == 0)return 'M'; //ATG
			if((skit & 0x0000000000000A85L) == 0)return 'L';//YTR
			if((skit & 0x000000000000028FL) == 0)return 'L';//CTN
			if((skit & 0x0000000000000115L) == 0)return 'K';//AAR
			if((skit & 0x000000000000088AL) == 0)return 'F';//TTY
			if((skit & 0x000000000000022FL) == 0)return 'P';//CCN
			if((skit & 0x000000000000082FL) == 0)return 'S';//TCN
			if((skit & 0x000000000000014AL) == 0)return 'S';//AGY
			if((skit & 0x000000000000012FL) == 0)return 'T';//ACN
			if((skit & 0x0000000000000844L) == 0)return 'W'; //TGG
			if((skit & 0x000000000000081AL) == 0)return 'Y'; //TAY
			if((skit & 0x000000000000048FL) == 0)return 'V'; //GTN
			if((skit & 0x0000000000000815L) == 0)return '.'; //TAR
			if((skit & 0x0000000000000851L) == 0)return '.'; //TRA
			// Could add support for J/Z ??
			return 'X';
		}
		else{
			// I'm to lazy to actually right these out properly
			// just f&r 0x0000000000000 for ~0xFFFFFFFFFFFFF
			// this means an extra operation until I change it
			if((skit & ~0xFFFFFFFFFFFFF42FL) == 0)return 'A'; //GCN 
			if((skit & ~0xFFFFFFFFFFFFF24FL) == 0)return 'R'; //CGN
			if((skit & ~0xFFFFFFFFFFFFF345L) == 0)return 'R';//MGR
			if((skit & ~0xFFFFFFFFFFFFF11AL) == 0)return 'N';//AAY
			if((skit & ~0xFFFFFFFFFFFFF41AL) == 0)return 'D';//GAY
			if((skit & ~0xFFFFFFFFFFFFF84AL) == 0)return 'C';//TGY
			if((skit & ~0xFFFFFFFFFFFFF215L) == 0)return 'Q';//CAR
			if((skit & ~0xFFFFFFFFFFFFF415L) == 0)return 'E';//GAR
			if((skit & ~0xFFFFFFFFFFFFF44FL) == 0)return 'G';//GGN
			if((skit & ~0xFFFFFFFFFFFFF18BL) == 0)return 'I';//ATH
			if((skit & ~0xFFFFFFFFFFFFF184L) == 0)return 'M'; //ATG
			if((skit & ~0xFFFFFFFFFFFFFA85L) == 0)return 'L';//YTR
			if((skit & ~0xFFFFFFFFFFFFF28FL) == 0)return 'L';//CTN
			if((skit & ~0xFFFFFFFFFFFFF115L) == 0)return 'K';//AAR
			if((skit & ~0xFFFFFFFFFFFFF88AL) == 0)return 'F';//TTY
			if((skit & ~0xFFFFFFFFFFFFF22FL) == 0)return 'P';//CCN
			if((skit & ~0xFFFFFFFFFFFFF82FL) == 0)return 'S';//TCN
			if((skit & ~0xFFFFFFFFFFFFF14AL) == 0)return 'S';//AGY
			if((skit & ~0xFFFFFFFFFFFFF12FL) == 0)return 'T';//ACN
			if((skit & ~0xFFFFFFFFFFFFF844L) == 0)return 'W'; //TGG
			if((skit & ~0xFFFFFFFFFFFFF81AL) == 0)return 'Y'; //TAY
			if((skit & ~0xFFFFFFFFFFFFF48FL) == 0)return 'V'; //GTN
			if((skit & ~0xFFFFFFFFFFFFF815L) == 0)return '.'; //TAR
			if((skit & ~0xFFFFFFFFFFFFF851L) == 0)return '.'; //TRA
		}
		return '!';
	}
	
}

