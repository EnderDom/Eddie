package enderdom.eddie.bio.sequence;

/**
 * A very simple region object
 * whilst i'd like to use biojava feature
 * I really can't bothered to implement all those
 * methods I'm never going to use.
 *  
 * @author Dominic Matthew Wood
 *
 */

public class BasicRegion implements Region{

	private int start;
	private int stop;
	private String name;
	
	public BasicRegion(){
		
	}
	
	public BasicRegion(int start, int stop, int base, String regionname){
		this.setName(regionname);
		this.setStart(start, base);
		this.setStop(stop, base);
	}
	
	
	public int getStart(int base) {
		return this.start+base;
	}

	public int getStop(int base) {
		return this.stop+base;
	}

	public void setStart(int start, int base) {
		this.start = start-base;
	}

	public void setStop(int stop, int base) {
		this.stop = stop-base;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
