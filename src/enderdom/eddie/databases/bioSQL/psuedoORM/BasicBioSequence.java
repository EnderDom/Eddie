package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.bio.sequence.SequenceObject;

public class BasicBioSequence implements BioSequence {

	private int bioentry;
	private int version;
	private int length;
	private String alphabet;
	private String sequence;
	private String identifier;
	
	public BasicBioSequence(int b, int v, int l, String a, String s){
		this.bioentry = b;
		this.version = v;
		this.length= l ;
		this.alphabet = a;
		this.sequence = s;
	}
	
	public int getBioentry() {
		return bioentry;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
	
	public boolean basicCompare(SequenceObject s) throws Exception{
		return this.sequence.equals(s.getSequence());
	}
	
}
