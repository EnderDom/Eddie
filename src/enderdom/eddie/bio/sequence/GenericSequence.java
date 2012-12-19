package enderdom.eddie.bio.sequence;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.SequenceObject;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.bio.Tools_Fasta;

/*
 * I guess just a wrapper for String
 * in reality, but you should switch
 * to nuclear or amino as quick as possible
 */
public class GenericSequence implements SequenceObject{
	
	private String sequence;
	private String name;
	private String quality; //QUALITY STORED AS FASTQ 
	
	public GenericSequence(String name){
		this.name = name;
	}
	
	public GenericSequence(String name, String sequence, String quality){
		this.name = name;
		this.sequence = sequence;
		this.quality = quality;
		if(this.quality !=null)if(this.sequence.length() != this.quality.length()){
			Logger.getRootLogger().warn("Sequence length and quality length are not the same, this will fail");
		}
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
		// TODO Auto-generated method stub
		return -1;
	}

	public int getActualLength() {//Rough implementation for now
		return this.sequence.replaceAll("-", "").length();
	}
	
	public int getLength(){
		return this.sequence.length();
	}

	public int leftTrim(int i) {
		if(i > this.sequence.length()){
			Logger.getRootLogger().warn("Trimmed shorter than actual length");
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
			Logger.getRootLogger().warn("Trimmed shorter than actual length");
			this.sequence = "";
			this.quality = "";
		}
		trim(0, this.sequence.length()-i);
		return this.sequence.length();
	}
	
	private void trim(int start, int end){
		this.sequence = this.sequence.substring(start, end);
		if(this.quality != null) this.quality = this.quality.substring(start, end);
	}

	public SequenceObject[] removeSection(int start, int end) {
		GenericSequence[] seq = new GenericSequence[2];
		seq[0] = this.replicate();
		seq[1] = this.replicate();
		seq[0].rightTrim(start);
		seq[1].leftTrim(end);
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
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public void setName(String title) {
		this.name = title;
	}
	

}
