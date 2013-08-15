package enderdom.eddie.bio.sequence;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.bio.Tools_Fasta;
import enderdom.eddie.tools.bio.Tools_Sequences;

/*
 * I guess just a wrapper for String
 * in reality
 * 
 * Probably good idea to migrate to biojava, somehow
 * 
 */
public class GenericSequence implements SequenceObject{
	
	private String sequence;
	private String Identifier;
	private String quality; //QUALITY STORED AS FASTQ 
	private int type = -1;
	private int positioninlist = -1;
	Logger logger = Logger.getRootLogger();
	
	public GenericSequence(String Identifier){
		this.Identifier = Identifier;
	}
	
	public GenericSequence(String Identifier, int position){
		this.Identifier = Identifier;
		this.positioninlist = position;
	}
	
	public GenericSequence(String Identifier, String sequence, int position){
		this.Identifier = Identifier;
		this.positioninlist = position;
		this.sequence = sequence;
	}
	
	public GenericSequence(String Identifier, String sequence, String quality, int position){
		this.Identifier = Identifier;
		this.sequence = sequence;
		this.quality = quality;
		this.positioninlist = position;
	}
	
	public GenericSequence(String Identifier, String sequence, String quality){
		this.Identifier = Identifier;
		this.sequence = sequence;
		this.quality = quality;
	}
	
	public GenericSequence(String Identifier, String sequence){
		this.Identifier = Identifier;
		this.sequence = sequence;
	}
	
	private GenericSequence replicate(){
		if(this.quality !=null)
			return new GenericSequence(new String(this.Identifier), new String(this.sequence), new String(this.quality));
		else 
			return new GenericSequence(new String(this.Identifier), new String(this.sequence));
	}
	
	public String getIdentifier() {
		return this.Identifier;
	}
	

	/*
	 * Returns a the Identifier with any space characters right of the
	 * main Identifier removed, including other non space chars
	 * ie Contig_232 232 323 ; ---> Contig_232
	 * 
	 */
	public String getShortIdentifier(){
		if(this.Identifier.trim().contains(" ")){
			String n = this.Identifier.trim();
			n=n.substring(0, this.Identifier.indexOf(" "));
		}
		return this.Identifier;
	}

	public String getSequence() {
		return this.sequence;
	}

	public String getQuality() {
		return this.quality;
	}

	public int getSequenceType() {
		if(type == -1){
			this.type = Tools_Sequences.detectSequence(this);
		}
		return type;
	}

	public int getActualLength() {
		int l = this.getLength();
		if(l == 0)return l;
		int i =0;
		while(i < sequence.length()){
			if(sequence.charAt(i) == '-' || sequence.charAt(i) == '*') {
				l--;
			}
			i++;
		}
		return l;
	}
	
	public int getLength(){
		if(this.sequence == null)Logger.getRootLogger().warn("Sequence is reporting 0 length as its not been set");
		return (this.sequence == null) ? 0 :this.sequence.length();
	}

	public int leftTrim(int i, int base) {
		i-=base;
		if(i > this.sequence.length()){
			Logger.getRootLogger().warn("Tried to Trim "+i+"bp from "+Identifier+" shorter than actual length of "+getLength());
			this.sequence = "";
			this.quality = "";
		}
		else{
			trim(i, this.sequence.length());
		}
		return this.sequence.length();
	}

	public int rightTrim(int i, int base) {
		i-=base;
		if(i > this.sequence.length()){
			Logger.getRootLogger().warn("Tried to Trim "+i+"bp from "+Identifier+" shorter than actual length of "+getLength());
			this.sequence = "";
			this.quality = "";
		}
		trim(0, this.sequence.length()-i);
		return this.sequence.length();
	}
	
	private void trim(int start, int end){
		if(end >= this.sequence.length()-1){
			this.sequence = this.sequence.substring(start);
			if(this.quality != null) this.quality = this.quality.substring(start);
		}
		else{
			this.sequence = this.sequence.substring(start, end);
			if(this.quality != null) this.quality = this.quality.substring(start, end);
		}
		if(this.quality != null){
			if(!qualCheck()){
				Logger.getRootLogger().error("Quality Check Failed, Sequence ("+sequence.length()+
						"bp) and Quality ("+quality.length()+"bp) not same length for " + this.Identifier);
			}
		}
	}

	public SequenceObject[] removeSection(int start, int end, int base) {
		start-=base;
		end-=base;
		GenericSequence[] seq = new GenericSequence[2];
		seq[0] = this.replicate();
		seq[1] = this.replicate();
		seq[0].rightTrim(start, base);
		seq[1].leftTrim(end, base);
		seq[0].setIdentifier(this.Identifier+"_0");
		seq[1].setIdentifier(this.Identifier+"_1");
		return seq;
	}
	
	private String insertString(int pos, String s1, String s2){
		StringBuilder build = new StringBuilder();
		if(pos!=0)build.append(s1.substring(0,pos));		
		build.append(s2);
		build.append(s1.substring(pos, s1.length()));
		return build.toString();
	}
	
	public void insert(int pos, SequenceObject s, int base) {
		pos-=base;
		this.sequence = insertString(pos, this.sequence, s.getSequence());
		if(quality != null && quality.length() != 0){
			String q = s.getQuality();
			if(s.getQuality() == null){
				int[] arr = new int[s.getLength()];
				q= Tools_Fasta.converArray2Phrap(arr);
			}
			this.quality = insertString(pos, this.quality, q);
			
		}
	}

	public void append(SequenceObject s) {
		insert(this.sequence.length(), s, 0);
	}

	public void extendLeft(int i) {
		this.sequence = Tools_String.stringPadding(sequence, sequence.length()+i, false, '-', false);
		if(quality != null){
			this.quality = Tools_String.stringPadding(sequence, sequence.length()+i, false, (char)33 , false);
		}
	}

	public void extendRight(int i) {
		this.sequence = Tools_String.stringPadding(sequence, sequence.length()+i, false, '-', true);
		if(quality != null){
			this.quality = Tools_String.stringPadding(sequence, sequence.length()+i, false, (char)33 , true);
		}
	}

	public boolean hasQuality() {
		return quality != null;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public void setIdentifier(String title) {
		this.Identifier = title;
	}


	public int getPositionInList() {
		return this.positioninlist;
	}
	
	public void setPositionInList(int i) {
		positioninlist = i;
	}

	public boolean qualCheck(){
		return quality.length() == sequence.length();
	}

	public int getLengthAtIndex(int index) {
		if(index > this.getLength()){
			Logger.getRootLogger().warn("Asking for index beyond length of sequence!");
			return this.getActualLength();
		}
		int ret = index;
		for(int i =0;i < index; i++){
			if(this.sequence.charAt(i) == '*' || this.sequence.charAt(i) == '-' ){
				ret--;
			}
		}
		return ret;
	}
	
}
