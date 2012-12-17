package enderdom.eddie.bio.blast;

public class UniVecRegion {

	int start = -1;
	int end = -1;
	int querylen =-1;
	public static int STRONG = 1;
	public static int MODERATE = 2;
	public static int WEAK = 3;
	
	
	public boolean isRegionStrong(int i){
		//TODO
		return false;
	}
	
	public boolean isRegionModerate(int i){
		//TODO
		return false;
	}
	
	public boolean isRegionWeak(int i){
		//TODO
		return false;
	}
	
	public boolean isRegionSuspect(){
		//TODO
		return false;
	}
	
	public int regionType(int i){
		if(isRegionStrong(i))return STRONG;
		else if(isRegionModerate(i))return MODERATE;
		else if(isRegionWeak(i))return WEAK;
		else return -1;
	}
	
	private boolean isTerminal() throws Exception{
		if(start < 25 || end+25 > querylen){
			return true;
		}
		else return false;
	}
	
}
