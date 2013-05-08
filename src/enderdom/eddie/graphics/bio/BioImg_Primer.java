package enderdom.eddie.graphics.bio;

import enderdom.eddie.tools.graphics.Tools_BioImg;

public class BioImg_Primer implements Comparable<BioImg_Primer>{

	public int start;
	public int numb;
	public boolean forward;
	public String name;
	
	public BioImg_Primer(String name, String star, String dir, int cds_start, int lineno) throws BioImg_Exception{
		this.start = Tools_BioImg.parseNumb(star, lineno, cds_start);
		this.name = name;
		if(dir.equalsIgnoreCase("for") || dir.equalsIgnoreCase("forward") || dir.equalsIgnoreCase("5'"))forward=true;
	}

	public int compareTo(BioImg_Primer arg0) {
		if (this.start < arg0.start){
			return 1;
		}
		else if(this.start > arg0.start) {
			return -1;
		}
		else{ 
			return 0;
		}
	}
	
}
