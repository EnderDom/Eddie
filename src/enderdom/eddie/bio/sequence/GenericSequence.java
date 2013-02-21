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
	private String name;
	private String quality; //QUALITY STORED AS FASTQ 
	private int type = -1;
	private int positioninlist = -1;
	
	public GenericSequence(String name){
		this.name = name;
	}
	
	public GenericSequence(String name, int position){
		this.name = name;
		this.positioninlist = position;
	}
	
	public GenericSequence(String name, String sequence, String quality, int postion){
		this.name = name;
		this.sequence = sequence;
		this.quality = quality;
		this.positioninlist = -1;
	}
	
	public GenericSequence(String name, String sequence, String quality){
		this.name = name;
		this.sequence = sequence;
		this.quality = quality;
	}
	
	public GenericSequence(String name, String sequence){
		this.name = name;
		this.sequence = sequence;
	}

	private GenericSequence replicate(){
		if(this.quality !=null)
			return new GenericSequence(new String(this.name), new String(this.sequence), new String(this.quality));
		else 
			return new GenericSequence(new String(this.name), new String(this.sequence));
	}
	
	public String getName() {
		return this.name;
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
		return (this.sequence == null) ? 0 :this.sequence.length();
	}

	public int leftTrim(int i, int base) {
		i-=base;
		if(i > this.sequence.length()){
			Logger.getRootLogger().warn("Tried to Trim "+i+"bp from "+name+" shorter than actual length of "+getLength());
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
			Logger.getRootLogger().warn("Tried to Trim "+i+"bp from "+name+" shorter than actual length of "+getLength());
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
				Logger.getRootLogger().error("Quality Check Failed, Sequence and Quality not same length for " + this.name);
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
		seq[0].setName(this.name+"_0");
		seq[1].setName(this.name+"_1");
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

	public void setName(String title) {
		this.name = title;
	}


	public int getPositionInList() {
		return this.positioninlist;
	}
	
	public void setPositionInList(int i) {
		positioninlist = i;
	}

	public boolean qualCheck(){
		return quality.length() != sequence.length();
	}
	
}
