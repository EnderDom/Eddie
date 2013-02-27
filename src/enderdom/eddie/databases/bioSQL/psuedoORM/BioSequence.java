package enderdom.eddie.databases.bioSQL.psuedoORM;

public interface BioSequence {

	public abstract int getBioentry();

	public abstract String getIdentifier();

	public abstract void setIdentifier(String identifier);

	public abstract int getVersion();

	public abstract void setVersion(int version);

	public abstract int getLength();

	public abstract String getAlphabet();

	public abstract void setAlphabet(String alphabet);

	public abstract String getSequence();

	public abstract void setSequence(String sequence);

}