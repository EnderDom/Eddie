package enderdom.eddie.bio.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Symbol;

import enderdom.eddie.bio.interfaces.SequenceObject;


/**
 * 
 * @author dominic
 *
 * Partial Implementation of the biojava Sequence,
 * god knows why really, I'm am trying to 
 * intergrate better into biojava, use there
 * stuff, honest.... :(
 *
 */
public class FourBitNuclear extends FourBitSequence implements SequenceObject, Iterator<Symbol>{
	
	int iter_pos;
	
	private String name;
	
	LinkedList<Feature> features;
	
	private String quality;
	
	//Major issues with 0-based to 1-based
	
	private static char[] aminoChars = new char[]{'A','R','R',
		'N','D','C','Q', 'H','E',
		'G','I','M','L','L',
		'K','F','P','S','S',
		'T','W','Y','V','.',
		'.'};
	
	private static long aminoCodes[] = new long[]{0x000000000000042FL, 0x000000000000024FL,0x0000000000000345L,
		0x000000000000011AL,0x000000000000041AL,0x000000000000084AL,0x0000000000000215L,0x000000000000021AL,0x0000000000000415L,
		0x000000000000044FL,0x000000000000018BL,0x0000000000000184L,0x0000000000000A85L,0x000000000000028FL,
		0x0000000000000115L,0x000000000000088AL,0x000000000000022FL,0x000000000000082FL,0x000000000000014AL,
		0x000000000000012FL,0x0000000000000844L,0x000000000000081AL,0x000000000000048FL,0x0000000000000815L,
		0x0000000000000851L};
	
	// I'm to lazy to actually right these out properly
	// just f^r 0x0000000000000 for ~0xFFFFFFFFFFFFF
	// this means an extra operation until I change it
	private static long revAminoCodes[] = new long[]{~0xFFFFFFFFFFFFF42FL, ~0xFFFFFFFFFFFFF24FL,~0xFFFFFFFFFFFFF345L,
		~0xFFFFFFFFFFFFF11AL,~0xFFFFFFFFFFFFF41AL,~0xFFFFFFFFFFFFF84AL,~0xFFFFFFFFFFFFF215L,~0xFFFFFFFFFFFFF21AL,~0xFFFFFFFFFFFFF415L,
		~0xFFFFFFFFFFFFF44FL,~0xFFFFFFFFFFFFF18BL,~0xFFFFFFFFFFFFF184L,~0xFFFFFFFFFFFFFA85L,~0xFFFFFFFFFFFFF28FL,
		~0xFFFFFFFFFFFFF115L,~0xFFFFFFFFFFFFF88AL,~0xFFFFFFFFFFFFF22FL,~0xFFFFFFFFFFFFF82FL,~0xFFFFFFFFFFFFF14AL,
		~0xFFFFFFFFFFFFF12FL,~0xFFFFFFFFFFFFF844L,~0xFFFFFFFFFFFFF81AL,~0xFFFFFFFFFFFFF48FL,~0xFFFFFFFFFFFFF815L,
		~0xFFFFFFFFFFFFF851L};
	
	public FourBitNuclear(int nlength){
		super(nlength);
	}
	
	public FourBitNuclear(String s){
		super(s);
	}
	
	/**
	 * 
	 * @param name Sequence name
	 * @param s Sequence string
	 */
	public FourBitNuclear(String name, String s){
		super(s);
		this.name = name;
	}
	
	/**
	 * 
	 * @param name Sequence name
	 * @param s 
	 * @param q
	 */
	public FourBitNuclear(String name, String s, String q){
		super(s);
		this.name =name;
		this.quality = q;
	}
	
	/**
	 * Temp hack as can't cast sequence to nuclear
	 * Need to sort out
	 * @param seq 
	 */
	public FourBitNuclear(FourBitSequence seq){
		this.dna = seq.dna;
		this.length = seq.length;
		this.actlength = seq.actlength;
		this.forward = seq.forward;
		this.isRNA = seq.isRNA;
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
	@SuppressWarnings("unused")
	private char[] getProtein(int frame, boolean forward){
		if(frame > 2)Logger.getRootLogger().warn("Frame is 0-based, should not be using >2 frame");
		Logger.getRootLogger().warn("Not yet implemented fully");
		int lint = (this.getActualLength()+frame)/3;
		if((this.getActualLength()+frame)%3 != 0)lint++;
		char[] arr = new char[lint];
		long skit = 0x0000000000000000L;
		long mask = 0x0000000000000FFFL;
		int j=0;
		int y = 12+(frame*4);
		
		if(forward){
			for(int i =0; i < dna.length; i++){
//				System.out.println("----------------");
//				System.out.println(Tools_Bit.LongAsHexString(dna[i]));
//				System.out.println("----------------");
				for(; y <65;y+=12){
					if(y < 11 && i != 0){
						skit = (dna[i]>>>64-y | dna[i-1]<<y) &mask;
					}
					else{
						skit = (dna[i]>>>64-y)&mask;
					}
					if(j==lint)break;
					arr[j]= getAmino(skit, forward);
//					System.out.println(y + " : " +new String(getAsChar((skit>>8)&0xF, true) +"" ) 
//					+ new String(getAsChar((skit>>4)&0xF,true) +"") + new String(getAsChar(skit&0xF, true)+"")
//					+ " : "+ Tools_Bit.LongAsHexString(skit) + " : " + arr[j]);
					j++;
					
				}
				y-=64;
			}
		}
		return arr;
	}
	
	/**
	 * Cycles through the amino acid codes, bitshifts 2,1,0
	 * both the skit and the comparison long which is AND'd 
	 * to rmask so basically each 4bit nucleotide is compared in
	 * turn. If a comparison does not yield a value other than 0
	 * then the loop is broken, else the equivalent amino is returned
	 * no matches returns 'X' character code.
	 * 
	 * This works, but I'm not sure it's that efficient...?
	 * 
	 * WORK IN PROGRESS
	 * 
	 * @param skit - long containing 12 bits of data at the right hand end of a long
	 * @param forward whether the sequence is reverse or forward
	 * @return a char representing the correct amino acid for that portion of the data array
	 */
	private static char getAmino(long skit, boolean forward){
		boolean[] code = new boolean[aminoCodes.length];
		int codecounter = aminoCodes.length;
		if(forward){
			for(int j = 8 ; j >-1; j-=4){
				for(int i = 0; i < aminoCodes.length; i++){
					if(!code[i]){
						long temp = aminoCodes[i]>>>j&rmask;
						if((skit>>>j&temp) != 0);
						else if (temp == 0);//In case this is a blank nucleotide (-)
						else{
							code[i]=true;
							codecounter--;
						}
						if(codecounter==1)break;
					}
					if(codecounter==1)break;
				}
			}
			if(codecounter==1){
				for(int i = 0; i < aminoCodes.length; i++){
					if(!code[i])return aminoChars[i];
				}
			}
			else{
				//This is a bit of a hack, need to think of a more elegant way of doing this
				if(codecounter==2){
					if(!code[1] && !code[2])return 'R';
					else if(!code[12] && !code[13])return 'L';
					else return '!';
				}
				return 'X';
			}
		}
		else{
			for(int j = 8 ; j >-1; j-=4){
				for(int i = 0; i < revAminoCodes.length; i++){
					if(!code[i]){
						long temp = revAminoCodes[i]>>>j&rmask;
						if((skit>>>j&temp) != 0);
						else if (temp == 0);//In case this is a blank nucleotide (-)
						else{
							code[i]=true;
							codecounter--;
						}
						if(codecounter==1)break;
					}
					if(codecounter==1)break;
				}
			}
			if(codecounter==1){
				for(int i = 0; i < revAminoCodes.length; i++){
					if(!code[i])return aminoChars[i];
				}
			}
			else{
				//This is a bit of a hack, need to think of a more elegant way of doing this
				if(codecounter==2){
					if(!code[1] && !code[2])return 'R';
					else if(!code[12] && !code[13])return 'L';
					else return '!';
				}
				return 'X';
			}
		}
		return '!';
	}

	/**
	 * 
	 * Still TODO Test this method
	 * 
	 * Creates a consensus sequence, starting at start integer
	 * ie start = 3 str = ATTTG
	 * dna2=     ATTTG
	 * this= AAAATTTGAAAAA
	 *       0123456789....
	 * @param start index to start in this dna data (0-based)
	 * @param dna2 FourBitSequence to merge
	 */
	@SuppressWarnings("unused")
	private void merge(int start, FourBitSequence dna2){
		if(start < this.getActualLength()){
			int offset = start%4;
			start /= 4;
			long[] dna1d = this.getDataArray();
			long[] dna2d = dna2.getDataArray();
			
			for(int i = start ; i < dna1d.length; i++){
				if(i != start && offset != 0)dna1d[i-1]|=dna2d[i-start]<<(64-(offset*4));
				dna1d[i]|=dna2d[i-start]>>>(offset*4);
			}
			if(dna2.getActualLength()+start > this.getActualLength()){
				//TODO complete
				Logger.getRootLogger().error("Unimplemented Fuuu!");
			}
		}
		else{
			throw new IndexOutOfBoundsException("Start of merge, after the end of data");
		}
	}

	
	public Alphabet getAlphabet() {
		if(this.isRNA) return RNATools.getRNA();
		else return DNATools.getDNA();
	}

	//TODO check is RNA the same except for the u....????
	public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
		long charvalue = this.getLong(index);
		if(forward){
				 if(charvalue==0x0000000000000001L) return DNATools.a();
			else if(charvalue==0x0000000000000002L) return DNATools.c();
			else if(charvalue==0x0000000000000004L) return DNATools.g();
			else if(charvalue==0x0000000000000008L) return isRNA ? RNATools.u() : DNATools.t();
			
			else if(charvalue==0x0000000000000005L) return DNATools.r();
			else if(charvalue==0x000000000000000aL) return DNATools.y();
			else if(charvalue==0x0000000000000006L) return DNATools.s();
			else if(charvalue==0x0000000000000009L) return DNATools.w();
			else if(charvalue==0x000000000000000cL) return DNATools.k();
			else if(charvalue==0x0000000000000003L) return DNATools.m();
			
			else if(charvalue==0x000000000000000eL) return DNATools.b();
			else if(charvalue==0x000000000000000dL) return DNATools.d();
			else if(charvalue==0x000000000000000bL) return DNATools.h();
			else if(charvalue==0x0000000000000007L) return DNATools.v();
			
			else if(charvalue==0x000000000000000fL) return DNATools.n();
			else return DNATools.n(); //Compatibility issues, look into gappedsequence
		}
		else{
				 if(charvalue==0x0000000000000001L) return isRNA ? RNATools.u() : DNATools.t();
			else if(charvalue==0x0000000000000002L) return DNATools.g();
			else if(charvalue==0x0000000000000004L) return DNATools.c();
			else if(charvalue==0x0000000000000008L) return DNATools.a();
			
			else if(charvalue==0x0000000000000005L) return DNATools.y();
			else if(charvalue==0x000000000000000aL) return DNATools.r();
			else if(charvalue==0x0000000000000006L) return DNATools.s();
			else if(charvalue==0x0000000000000009L) return DNATools.w();
			else if(charvalue==0x000000000000000cL) return DNATools.m();
			else if(charvalue==0x0000000000000003L) return DNATools.k();
			
			else if(charvalue==0x000000000000000eL) return DNATools.v();
			else if(charvalue==0x000000000000000dL) return DNATools.h();
			else if(charvalue==0x000000000000000bL) return DNATools.d();
			else if(charvalue==0x0000000000000007L) return DNATools.b();
			
			else if(charvalue==0x000000000000000fL) return DNATools.n();
			else return DNATools.n();
		}
	}

	public List<Symbol> toList() {
		List<Symbol> list = new ArrayList<Symbol>(this.getActualLength());
		for(int i =0; i< this.getActualLength(); i++){
			list.add(this.symbolAt(i));
		}
		return list;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String s){
		this.name = s;
	}
	
	public boolean hasNext() {
		return !(this.getActualLength()>=this.iter_pos);
	}

	public Symbol next() {
		return this.symbolAt(this.iter_pos);
	}

	public void remove() {
		//More difficult
	}

	public void updateLength(){
		//TODO
		Logger.getRootLogger().error("Unimplemented method");
	}
	
	public String getSequence() {
		return this.getAsString();
	}

	public int getSequenceType() {
		if(this.isRNA)return SequenceObject.RNA;
		else return SequenceObject.DNA;
	}

	public int leftTrim(int i) {
		
		Logger.getRootLogger().error("Unimplemented method");
		updateLength();
		return -1;
	}

	public int rightTrim(int i) {
		Logger.getRootLogger().error("Unimplemented method");
		updateLength();
		return -1;
	}

	public SequenceObject[] removeSection(int start, int end) {
		Logger.getRootLogger().error("Unimplemented method");
		updateLength();
		return null;
	}

	public String getQuality() {
		return this.quality;
	}
	
	public void setQuality(String q){
		this.quality = q;
	}

	public void insert(int pos, SequenceObject s) {
		// TODO Auto-generated method stub
		Logger.getRootLogger().error("Unimplemented method");
	}

	public void append(SequenceObject s) {
		// TODO Auto-generated method stub
		Logger.getRootLogger().error("Unimplemented method");
	}

	public void extendLeft(int i) {
		// TODO Auto-generated method stub
		Logger.getRootLogger().error("Unimplemented method");
	}

	public void extendRight(int i) {
		// TODO Auto-generated method stub
		Logger.getRootLogger().error("Unimplemented method");
	}

	public boolean hasQuality() {
		return quality!=null;
	}

	public void setSequence(String sequence) {
		init(sequence.length());
		parseString(sequence);
	}
}
