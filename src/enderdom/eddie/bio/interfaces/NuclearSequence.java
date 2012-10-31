package enderdom.eddie.bio.interfaces;

public interface NuclearSequence extends SequenceObject {

	public double getGC();
	
	public SequenceObject getProtein(int frame, boolean sense);
	
	public boolean isRNA();
	
}
