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
	protected long[] dna;
	
	/*
	 * Mask:
	 * needs to equal to the amount
	 * offset by the right bitwise shift
	 * ie ..1111 
	 */
	protected int bitmask = 0xf;
	
	protected int bitoff = 4; //2 less powers from BitSet's 6 as DNA is 4 bits large
	
	//Length in bp of sequence inc. '*'
	protected int length;
	//Length without '*'/'-'
	protected int actlength;
	
	/*
	 * Read bits from left or right
	 * this will either consider the 
	 * sequence reversed and complimented
	 */
	protected boolean forward = true;
		
	protected static long lmask = 0xF000000000000000L;
	protected static long rmask = 0x000000000000000FL;  
	

	
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
	
	protected void init(int nlength){
		if(nlength < 0) throw new NegativeArraySizeException();
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
	
	
	protected long getLong(int pos){
		/* 
		 * This method will return '-' for any values outside the actual sequence
		 * This could potential lead to issues when bugs are left uncaught, as
		 * such if calling bp positions outside sequence is not needed use getInside()
		 */
		if(pos >= this.length || pos < this.length*-1) return 0x0L;
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
			return (rmask&(this.dna[offset]>>sub_numb));
		}
	}
	
	public char get(int pos){
		return getAsChar(getLong(pos), this.forward);
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
				case 'A' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000001L; break;
				case 'C' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000002L; break;
				case 'G' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000004L; break;
				case 'T' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000008L; break;
				
				case 'R' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000005L; break;
				case 'Y' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000aL; break;
				case 'S' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000006L; break;
				case 'W' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000009L; break;
				case 'K' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000cL; break;
				case 'M' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000003L; break;
				
				case 'B' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000eL; break;
				case 'D' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000dL; break;
				case 'H' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000bL; break;
				case 'V' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000007L; break;
				
				case 'N' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000fL; break;
				case 'X' : dna[offset] <<= 4; dna[offset] |= 0x000000000000000fL; break;
				case '-' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000000L; this.actlength--; break;
				case '*' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000000L; this.actlength--; break;
				case '.' : dna[offset] <<= 4; dna[offset] |= 0x0000000000000000L; this.actlength--; break;
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
				long charvalue = 0x0000000000000000L;
				for(int j = 0; j <64; j+=4){
					charvalue = (lmask & dna[i])>>>(60-j);
					array[arraycount] = getAsChar(charvalue, forward);
					arraycount++;
					if(arraycount == this.length)break;
					lmask>>>=4;
				}
			}
		}
		else{
			for(int i =dna.length-1; i > -1; i--){
				long charvalue =  0x0000000000000000L;
				int shift = 64;
				int jshift = 0;
				if(i==dna.length-1){ 
					shift = (this.length % 16)*4;
					jshift = (64-shift);
					rmask<<=jshift;
				}
				for(int j = 0; j <shift; j+=4){
					charvalue = (rmask & dna[i])>>j+jshift;
					//System.out.println(Tools_Bit.LongAsBitString(charvalue));
					array[arraycount] = getAsChar(charvalue, forward);
					arraycount++;
					if(arraycount == this.length)break;
					rmask<<=4;
				}
			}
		}
		return array;
	}
	
	protected static char getAsChar(long charvalue, boolean forward){
		if(forward){
				 if(charvalue==0x0000000000000001L) return 'A';
			else if(charvalue==0x0000000000000002L) return 'C';
			else if(charvalue==0x0000000000000004L) return 'G';
			else if(charvalue==0x0000000000000008L) return 'T';
			
			else if(charvalue==0x0000000000000005L) return 'R';
			else if(charvalue==0x000000000000000aL) return 'Y';
			else if(charvalue==0x0000000000000006L) return 'S';
			else if(charvalue==0x0000000000000009L) return 'W';
			else if(charvalue==0x000000000000000cL) return 'K';
			else if(charvalue==0x0000000000000003L) return 'M';
			
			else if(charvalue==0x000000000000000eL) return 'B';
			else if(charvalue==0x000000000000000dL) return 'D';
			else if(charvalue==0x000000000000000bL) return 'H';
			else if(charvalue==0x0000000000000007L) return 'V';
			
			else if(charvalue==0x000000000000000fL) return 'N';
			else return '-';
		}
		else{
				 if(charvalue==0x0000000000000001L) return 'T';
			else if(charvalue==0x0000000000000002L) return 'G';
			else if(charvalue==0x0000000000000004L) return 'C';
			else if(charvalue==0x0000000000000008L) return 'A';
			
			else if(charvalue==0x0000000000000005L) return 'Y';
			else if(charvalue==0x000000000000000aL) return 'R';
			else if(charvalue==0x0000000000000006L) return 'S';
			else if(charvalue==0x0000000000000009L) return 'W';
			else if(charvalue==0x000000000000000cL) return 'M';
			else if(charvalue==0x0000000000000003L) return 'K';
			
			else if(charvalue==0x000000000000000eL) return 'V';
			else if(charvalue==0x000000000000000dL) return 'H';
			else if(charvalue==0x000000000000000bL) return 'D';
			else if(charvalue==0x0000000000000007L) return 'B';
			
			else if(charvalue==0x000000000000000fL) return 'N';
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
	protected void extend(int len){
		long[] newdna = new long[dna.length+len];
		for(int i =0; i < dna.length ; i++){
			newdna[i] = dna[i];
		}
		this.dna=newdna;
	}
	
	public String insertString(int pos){
		//TODO
		return null;
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
	
	public long[] getDataArray(){
		return this.dna;
	}

	public  boolean compareBasic(FourBitSequence comparison){
		long[] p = comparison.dna;
		for(int i = 0; i < this.dna.length ; i++){
			if(dna[i] != p[i]) return false;
		}
		return true;
	}
	
}
