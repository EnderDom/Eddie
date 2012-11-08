package enderdom.eddie.bio.sequence;

import org.apache.log4j.Logger;

import enderdom.eddie.bio.interfaces.SequenceObject;

/*
 * I guess just a wrapper for String
 * in reality, but you should switch
 * to nuclear or amino as quick as possible
 */
public class GenericSequence implements SequenceObject{
	
	private String sequence;
	private String name;
	private String quality;
	
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
		return new GenericSequence(new String(this.name), new String(this.sequence), new String(this.quality));
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
	
//	public EightBitAmino getAsAmino(){
//		return new EightBitAmino(this.name, this.sequence);
//	}
}
