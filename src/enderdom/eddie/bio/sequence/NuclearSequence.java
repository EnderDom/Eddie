package enderdom.eddie.bio.sequence;


public interface NuclearSequence extends SequenceObject {

	public double getGC();
	
	public SequenceObject getProtein(int frame, boolean sense);
	
	public boolean isRNA();
	
}
