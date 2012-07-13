package databases.bioSQL.psuedoORM;

import databases.bioSQL.interfaces.BioSQL;
import bio.sequence.FourBitSequence;

public class BioSequence {

	private int bioentry;
	private int version;
	private int length;
	private String alphabet;
	private String sequence;
	
	public BioSequence(int b, int v, int l, String a, String s){
		this.bioentry = b;
		this.version = v;
		this.length= l ;
		this.alphabet = a;
		this.sequence = s;
	}
	
	public int getBioentry() {
		return bioentry;
	}
	public void setBioentry(int bioentry) {
		this.bioentry = bioentry;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getAlphabet() {
		return alphabet;
	}
	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public boolean sequenceBasicCompare(String s){//TODO improve to a better comparison method
		return this.sequence.toUpperCase().equals(s.toUpperCase());
	}
	
	public boolean rawDNACompare(FourBitSequence s) throws Exception{
		if(this.alphabet == BioSQL.alphabet_DNA){
			FourBitSequence resolve = new FourBitSequence(this.sequence);
			return resolve.compareBasic(s);	
		}
		else{
			throw new Exception("Cannot compare, not DNA");
		}
	}
	
}
