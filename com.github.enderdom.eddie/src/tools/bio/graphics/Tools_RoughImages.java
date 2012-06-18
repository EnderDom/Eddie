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
	public static Color[] defaultcolors = new Color[]{new Color(255,0,0), new Color(0,255,0),  new Color(255,255,0),};
	public static int defaultBGR = BufferedImage.TYPE_USHORT_565_RGB;
	/*
	 * INDEV
	 */
	public static BufferedImage drawContigRough(String name, String[] reads, boolean boxed, int[][] pos, int[][] blasts, short[] colors, int heightbar, double widthperbp){
		
		//Get width of image
		if(reads == null || reads.length  < 1){
			BufferedImage image = new BufferedImage(100, 100, defaultBGR);
			Graphics g = image.getGraphics();
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(background);
			g2.drawRect(0, 0, 100, 100);
			g2.fillRect(0, 0, 100, 100);
			g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
			g2.setColor(Color.black);
			g2.drawString("No Complement Contig Found",1, 50);
			image.flush();
			return image;
		}
		if(reads.length > 50){
			reads = null;
		}
		int maxwidth = (int)Math.round((double)Tools_Math.getMaxXValue(pos)*widthperbp);
		if(maxwidth > 60000){
			widthperbp = 60000.0/(double)Tools_Math.getMaxXValue(pos);
			maxwidth = (int)Math.round((double)Tools_Math.getMaxXValue(pos)*widthperbp);
		}
		
		int height= pos[0].length*heightbar;
		//Add default margins
		maxwidth+=margin*2;
		height +=margin*2;
		if(name != null) height+=nameheader;
		if(blasts != null)height +=heightbar*3; //Additionalbar, plus 2 margins
		if(height > 30000){
			height = 30000;
			heightbar =3;
		}
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
			Color blast = new Color(0, 0, 255);		
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
			g2.fillRect((int)Math.round(pos[0][i]*widthperbp) +margin, h, (int)Math.round(pos[1][i]*widthperbp), heightbar);
			g2.setColor(Color.black);
			g2.drawRect((int)Math.round(pos[0][i]*widthperbp)+margin, h, (int)Math.round(pos[1][i]*widthperbp), heightbar);
			if(reads != null){
				g2.setFont(new Font("Times New Roman", Font.PLAIN, 7));
				g2.drawString(reads[i], (int)Math.round(pos[0][i]*widthperbp)+margin+1, h+heightbar-1);
			}
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
