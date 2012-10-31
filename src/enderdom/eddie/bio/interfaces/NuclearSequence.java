package enderdom.eddie.bio.interfaces;

public interface NuclearSequence extends Sequence {

	public double getGC();
	
	public Sequence getProtein(int frame, boolean sense);
	
	public boolean isRNA();
	
}
