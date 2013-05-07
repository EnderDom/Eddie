package enderdom.eddie.tools.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import enderdom.eddie.graphics.bio.BioImg_Exception;
import enderdom.eddie.graphics.bio.BioImg_RegionLine;
import enderdom.eddie.graphics.bio.BioImg_RegionSpace;
import enderdom.eddie.graphics.bio.BioImg_RegionStyle;
import enderdom.eddie.tools.Tools_String;

public class Tools_BioImg {

	public static int parseNumb(String start2, int lineno, int cds_start) throws BioImg_Exception{
		start2=start2.trim();
		int fina = -1;
		int ind=start2.indexOf("CDS:");
		ind = ind==-1 ? 0: 4;
		
		int ind2 = 0;
		if((ind2 =start2.indexOf("aa")) != -1){ 
			Integer fin = Tools_String.parseString2Int(start2.substring(ind, ind2));
			if(fin == null) throw new BioImg_Exception("\""+start2+"\" is supposed to be a xaa on line " + lineno + " format exception)");
			fina = (ind == 4) ? (fin*3)+cds_start : fin*3;
		}
		else if((ind2 = start2.indexOf("bp")) != -1){
			Integer fin = Tools_String.parseString2Int(start2.substring(ind, ind2));
			if(fin == null) throw new BioImg_Exception("\""+start2+"\" is supposed to be a xbp on line " + lineno + " format exception)");
			fina = (ind == 4) ? fin+cds_start : fin;
		}
		else{
			throw new BioImg_Exception("Start/Stop values on line " + lineno + ", \""+start2+"\" must include either 'aa' or 'bp'");
		}	
		return fina;
	}
	
	
	public static BufferedImage getPattern(int width, int height, int linewidth, BioImg_RegionStyle style, BioImg_RegionLine line, BioImg_RegionSpace space, Color color){
		 
		 BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	     Graphics2D gc = img.createGraphics();
	     gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	     gc.setColor(color);
	     int pw = linewidth;
	     int sw = linewidth*2;
	     if(line == BioImg_RegionLine.thin){
	    	 pw/=2;
	     }
	     if(line == BioImg_RegionLine.thick){
	    	 pw*=2;
	     }
	     if(space == BioImg_RegionSpace.narrow){
	    	 sw/=2;
	     }
	     if(space == BioImg_RegionSpace.wide){
	    	 sw*=2;
	     }
	     if(style == BioImg_RegionStyle.hori){
	    	 for(int i =0; i < height; i+=sw+pw){
	    		 gc.fillRect(0,i, width, pw);
	    	 }
	     }
	     if(style == BioImg_RegionStyle.verti){
	    	 for(int i =0; i < width; i+=sw+pw){
	    		 gc.fillRect(i,0, i+pw, height);
	    	 }
	     }
	     if(style == BioImg_RegionStyle.diag || style == BioImg_RegionStyle.diagrev || style == BioImg_RegionStyle.cross){
	    	 for(int i =0-width; i < width*2; i+=sw+pw){
	    		 for(int j =0 ; j < pw; j++){
	    			 if(style == BioImg_RegionStyle.diagrev || style == BioImg_RegionStyle.cross){
	    				 gc.drawLine(i+j, 0, i+(sw*8)+j, height);
	    			 }
	    			 if(style == BioImg_RegionStyle.diag || style == BioImg_RegionStyle.cross){
	    				 gc.drawLine(i+j, height, i+(sw*8)+j, 0);
	    			 }
	    		 }
	    	 }
	     }
	     if(style == BioImg_RegionStyle.fill){
	    	 gc.drawRect(0, 0, width, height);
	    	 gc.fillRect(0, 0, width, height);
	     }
	     return img;
	}
	
}
