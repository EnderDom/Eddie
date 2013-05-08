package enderdom.eddie.graphics.bio;

import java.awt.Color;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;

public class ClustalImageFeature {

	//Color of background of Image
	Color background;
	//Color of text
	Color text;
	//Color of box if box is set
	Color box;
	//Width of box if set
	int boxwidth;
	//Remove lines with adjacent to box with same color
	boolean boxmerge;
	//Remove lines with adjacent to box with any color
	boolean colorboxmerge;
	int seq_x;
	int seq_y;
	
	int x;
	int y;
	
	Logger logger = Logger.getRootLogger();
	
	public ClustalImageFeature(){
		
	}
	
	public void parseFeature(String line){
		String[] s = line.split(",");
		for(int i =0;i < s.length; i++){
			if(s.equals("|")){
				
			}
			else if(s[i].equals("boxmerge")){
				boxmerge=true;
			}
			else if(s[i].startsWith("boxed=")){
				
				Integer l = Tools_String.parseString2Int(s[i].substring(6, s[i].length()));
				if(l != null){
					boxwidth = l;
				}
				else{
					logger.error("For adding box to region use: boxed=1, " + s[i] + " <- this is wrong");
				}
			}
			
		}
	}
	
	public void paintFeature(){
		
	}
}
