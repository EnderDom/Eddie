package enderdom.eddie.bio.sequence;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.bio.Tools_Fasta;
import enderdom.eddie.tools.bio.Tools_Sequences;

/*
 * I guess just a wrapper for String
 * in reality, but you should switch
 * to nuclear or amino as quick as possible
 */
public class GenericSequence implements SequenceObject{
	
	private String sequence;
	private String name;
	private String quality; //QUALITY STORED AS FASTQ 
	private boolean gaps;
	private int type = -1;
	
	public GenericSequence(String name){
		this.name = name;
	}
	
	public GenericSequence(String name, String sequence, String quality){
		this.name = name;
		this.sequence = sequence;
		this.quality = quality;
		if(this.quality !=null)if(this.sequence.length() != this.quality.length()){
			Logger.getRootLogger().warn("Sequence length and quality length are not the same "+
					this.sequence.length() + " and " + this.quality.length()+" for "+name+", this will fail");
		}
		if(hasGaps(sequence))gaps=true;
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

	public int getActualLength() {//Rough implementation for now
		if(this.gaps)return this.sequence.replaceAll("-", "").length();
		else return this.getLength();
	}
	
	public int getLength(){
		return (this.sequence == null) ? 0 :this.sequence.length();
	}

	public int leftTrim(int i) {
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

	public int rightTrim(int i) {
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
		
		if(this.quality.length() != this.sequence.length()){
			Logger.getRootLogger().error("Quality Check Failed, Sequence and Quality not same length for " + this.name);
		}
	}

	public SequenceObject[] removeSection(int start, int end) {
		GenericSequence[] seq = new GenericSequence[2];
		seq[0] = this.replicate();
		seq[1] = this.replicate();
		seq[0].rightTrim(start);
		seq[1].leftTrim(seq[1].getLength()-end);
		seq[0].setName(this.name+"_0");
		seq[1].setName(this.name+"_1");
		return seq;
	}

	public FourBitNuclear getAsNuclear(){
		return new FourBitNuclear(this.name, this.sequence, this.quality);
	}
	
	private String insertString(int pos, String s1, String s2){
		StringBuilder build = new StringBuilder();
		build.append(s1.substring(0,pos));
		build.append(s2);
		build.append(s1.substring(pos, s1.length()));
		return build.toString();
	}
	
	public void insert(int pos, SequenceObject s) {
		this.sequence = insertString(pos, this.sequence, s.getSequence());
		if(quality != null && quality.length() != 0){
			String q = s.getQuality();
			if(s.getQuality() == null){
				int[] arr = new int[s.getLength()];
				q= Tools_Fasta.converArray2Phrap(arr);
			}
			this.quality = insertString(pos, this.quality, q);
			if(quality.length() != sequence.length()){
				Logger.getRootLogger().error("Sequence and quality not same length for "+ this.name);
			}
		}
	}

	public void append(SequenceObject s) {
		insert(this.sequence.length(), s);
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
		if(hasGaps(sequence));
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public void setName(String title) {
		this.name = title;
	}
	
	private boolean hasGaps(String seq){
		return seq.contains("-");
	}

}
