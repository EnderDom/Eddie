package enderdom.eddie.bio.sequence;

/**
 * A basic region interface
 * importantly it forces bases
 * to try and avoid the 1-base 0-base confusion
 * 
 * 		
 * @author dominic
 *
 */
		
public interface Region {

	public int getStart(int base);
	
	public int getStop(int base);
	
	public void setStart(int start, int base);
	
	public void setStop(int stop, int base);
	
	public void setName(String name);
	
	public String getName();
	
}
