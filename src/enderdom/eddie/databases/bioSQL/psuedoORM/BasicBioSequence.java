package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.bio.sequence.GenericSequence;

public class BasicBioSequence extends GenericSequence implements BioSequence {

	private int bioentry;
	private int version;
	private int length;
	private String alphabet;
	
	public BasicBioSequence(String id, int b, int v, int l, String a, String s){
		super(id, s);
		this.bioentry = b;
		this.version = v;
		this.length= l ;
		this.alphabet = a;
	}
	
	public int getBioentry() {
		return bioentry;
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
	
}
