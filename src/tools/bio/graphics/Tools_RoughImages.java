package tools.bio.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import tools.Tools_Math;

/*
 * Some tools to roughly generate tools
 */

public class Tools_RoughImages {

	public static int margin = 15;
	public static int nameheader =20;
	public static Color background = Color.white;
	public static Color[] defaultcolors = new Color[]{new Color(255,0,0), new Color(0,255,0)};
	public static int defaultBGR = BufferedImage.TYPE_USHORT_565_RGB;
	/*
	 * INDEV
	 */
	public static BufferedImage drawContigRough(String name, boolean boxed, int[][] pos, int[][] blasts, short[] colors, int heightbar, int widthperbp){
		
		//Get width of image
		int maxwidth = Tools_Math.getMaxXValue(pos)*widthperbp;
		int height= pos[0].length*heightbar;
		//Add default margins
		maxwidth+=margin*2;
		height +=margin*2;
		if(name != null) height+=nameheader;
		if(blasts != null)height +=heightbar*3; //Additionalbar, plus 2 margins
		
		BufferedImage image = new BufferedImage(maxwidth, height, defaultBGR);
		Graphics g = image.getGraphics();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(background);
		g2.drawRect(0, 0, maxwidth, height);
		g2.fillRect(0, 0, maxwidth, height);
		if(boxed){
			if(name == null){
				g2.setColor(Color.black);
				g2.drawRect(2,2,maxwidth-4, height-4);
				g2.drawRect(3,3,maxwidth-6, height-6);
			}
			else{
				g2.setColor(Color.black);
				g2.drawRect(2,nameheader+2,maxwidth-4, height-4-nameheader);
				g2.drawRect(3,nameheader+3,maxwidth-6, height-6-nameheader);
			}
		}
		int h = margin; //Start the height cursor
		if(name != null){
			h= margin+nameheader;
			g2.setColor(Color.black);
			g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			g2.drawString(name, margin, nameheader);
		}
		if(blasts != null){
			h+=heightbar;
			int blastalpha= 2;
			if(blasts[0].length < 125)blastalpha = (int)Math.round((double)255/(double)blasts[0].length);
			Color blast = new Color(0, 0, 255,blastalpha);
			g2.setColor(blast);
			for(int i =0; i < blasts[0].length; i++){
				blasts[0][i]*=widthperbp;
				blasts[1][i]*=widthperbp;
				g2.fillRect(blasts[0][i]+margin, h,blasts[1][i], heightbar);
			}
			h+=heightbar*2;
		}
		for(int i =0;i < pos[0].length; i++){
			if(colors[i] >= defaultcolors.length){
				generateMoreColors(colors[i]);
			}
			g2.setColor(defaultcolors[colors[i]]);
			g2.fillRect((pos[0][i]*widthperbp)+margin, h, (pos[1][i]*widthperbp), heightbar);
			g2.setColor(Color.black);
			g2.drawRect((pos[0][i]*widthperbp)+margin, h, (pos[1][i]*widthperbp), heightbar);
			h+=heightbar;
		}
		image.flush();
		return image;
	}

	
	public static void generateMoreColors(short i){
		Color[] def = new Color[i+1];
		int j =0;
		for(;j<defaultcolors.length; j++){
			def[j] = defaultcolors[j]; 
		}
		for(;j < def.length;j++){
			Color t = new Color((int)Math.round(Math.random()*255), (int)Math.round(Math.random()*255),(int)Math.round(Math.random()*255));
			def[j] = t;
		}
		defaultcolors = def;
	}
}
