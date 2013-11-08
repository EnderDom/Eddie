package enderdom.eddie.databases.bioSQL.psuedoORM;

import enderdom.eddie.bio.sequence.SequenceObject;

public interface BioSequence extends SequenceObject{

	public abstract int getBioentry();

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract int getLength();

	public abstract String getAlphabet();

	public abstract void setAlphabet(String alphabet);


}