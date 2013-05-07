package enderdom.eddie.graphics.bio;

import java.awt.Color;
import java.awt.image.BufferedImage;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.graphics.Tools_BioImg;
import enderdom.eddie.tools.graphics.Tools_Image;

public class BioImg_Region implements Comparable<BioImg_Region>{

	public int start;
	public int stop;
	
	public Color color;
	public String shortname;
	public String longname;
	
	public BioImg_RegionStyle style = BioImg_RegionStyle.empty;
	public BioImg_RegionSpace space = BioImg_RegionSpace.mid;
	public BioImg_RegionLine line = BioImg_RegionLine.norm;
	
	public BioImg_Region(String shortname, String longname, String start, String stop, String style,String Color, int len, int lineno) throws BioImg_Exception{
		this.shortname = shortname;
		this.longname = longname;
		this.start = Tools_BioImg.parseNumb(start, lineno, len);
		this.stop  = Tools_BioImg.parseNumb(stop, lineno, len);
		parseStyle(style, lineno);
		parseColor(Color);
	}

	
	public BufferedImage getTexture(int width, int height, int linewidth){
		return Tools_BioImg.getPattern(width, height, linewidth, style, line, space, color);
	}
	
	private void parseColor(String color2) throws BioImg_Exception {
		if(color2.contains("rgb")){
			boolean fail = true;
			if(color2.contains("(") && color2.contains(")")){
				String rgb = color2.substring(color2.indexOf("(")+1, color2.indexOf(")"));
				String[] cols = rgb.split(";");
				if(cols.length == 3){
					Integer r = Tools_String.parseString2Int(cols[0]);
					Integer g = Tools_String.parseString2Int(cols[1]);
					Integer b = Tools_String.parseString2Int(cols[2]);
					if(r != null || g != null || b != null){
						if(r <256 && g < 256 && b < 256){
							color = new Color(r,g,b);
							fail = false;
						}
					}
				}
			}
			if(fail){
				throw new BioImg_Exception("Failed to parse color: " + color2 + " rgb format should be rgb(x,x,x)");
			}
		}
		else{
			color2 = color2.trim();
			color = Tools_Image.getColor(color2);//Doesn't work???
			if(color == null){
				throw new BioImg_Exception("Failed to parse color: " + color2 + " should be field name in Java.awt.Color");
			}
		}
	}

	private void parseStyle(String style2, int lineno) throws BioImg_Exception {
		style2=style2.trim();

		if(style2.contains("-")){
			String[] ss = style2.split("-");
			this.style = BioImg_RegionStyle.valueOf(ss[0]);
			this.space = BioImg_RegionSpace.valueOf(ss[1]);
			this.line = BioImg_RegionLine.valueOf(ss[2]);
		}
		else{
			throw new BioImg_Exception("Failed to parse style ("+style
					+") of region, needs 3 values separated by - ie cross-mid-norm");
		}
		
	}

	public int compareTo(BioImg_Region arg0) {
		if (this.start < arg0.start){
			return -1;
		}
		else if(this.start > arg0.start) {
			return 1;
		}
		else{ 
			if(arg0.stop > this.stop ){
				return -1;
			}
			if(arg0.stop < this.stop ){
				return 1;
			}
			else{
				return 0;
			}
		}
	}

	
	
}
