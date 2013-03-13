package enderdom.eddie.graphics.bio;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import enderdom.eddie.bio.homology.ClustalAlign;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.tools.graphics.Tools_Image;

public class ClustalImage extends ClustalAlign {
	
	//Defaults for size of image and resolution
	public static double wdefault = 210.0; //mm
	public static double hdefault = 300.0; //mm
	public static double ppmmdefault = Tools_Image.convertDPI2DPMM(300);
	
	//Defaults for arrangement of sequence
	
	public static int defaultbp = 60;
	public static int defaultgap = 4;
	public static int defaultword = 8;
	
	//Internal sizes and resolution
	public int fontsize = 32;
	private double width;
	private double height;
	private double ppmm;
	
	//Etc
	private Font font;

	
	public ClustalImage(File file, BioFileType type)
			throws UnsupportedTypeException, Exception {
		super(file, type);
		this.width = wdefault;
		this.height = hdefault;
		this.ppmm = ppmmdefault;
	}
	
	public ClustalImage(File file, BioFileType type, double w, double h, double dpmm)
			throws UnsupportedTypeException, Exception {
		super(file, type);
		this.width = w;
		this.height=h;
		this.ppmm =dpmm;
	}
	
	public ClustalImage(InputStream file, BioFileType type)
			throws UnsupportedTypeException, Exception {
		super(file, type);
		this.width = wdefault;
		this.height = hdefault;
		this.ppmm = ppmmdefault;
	}
	
	public ClustalImage(InputStream file, BioFileType type, double w, double h, double dpmm)
			throws UnsupportedTypeException, Exception {
		super(file, type);
		this.width = w;
		this.height=h;
		this.ppmm =dpmm;
	}
	
	public void drawImage(File f){
		int w = (int)Math.round(this.width*ppmm);
		int h = (int)Math.round(this.height*ppmm);
		
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.drawRect(0, 0, w, h);
		g2.fillRect(0, 0, w, h);
		
		int boxwidth = w/(defaultbp+defaultgap+defaultword+2);
		int boxheight = (int)( (double)boxwidth*1.5);
		System.out.println(boxwidth);
		System.out.println(boxheight);
		//test, to remove
		g2.setColor(Color.black);
		g2.setFont(getFont(fontsize));
		int len = this.getSequence(0).getLength();
		int heightcount=1;
		for(int l =0; l < len-defaultbp; l+=defaultbp){
			for(String s : this.keySet()){
				g2.drawString(this.getSequence(s).getIdentifier(), 2,(heightcount*boxheight)-10);
				for(int j =0; j < defaultbp+defaultgap+defaultword;j++){
					g2.drawRect(j*boxwidth, (heightcount-1)*boxheight, boxwidth, boxheight);
				}
				for(int j = 0;j < defaultbp;j++){
					g2.drawString(this.getSequence(s).getSequence().substring(l+j, l+j+1), 
							((j+defaultgap+defaultword)*boxwidth)+5, (heightcount*boxheight)-10);
				}
				g2.drawString(this.getSequence(s).getLengthAtIndex(l+defaultbp)+"", ((defaultbp+defaultgap+defaultword)*boxwidth)+(boxwidth/2),(heightcount*boxheight)-10);
				heightcount++;
			}
			heightcount++;
		}
		try {
			Tools_Image.saveImageDPMM(f, image, ppmm);
		} catch (IOException e) {
			logger.error("Failed to save, attempt to save again without metadata");
			Tools_Image.image2File(f, image, "PNG");
		}
	}
	
	/**
	 * Loads font from filepath given as argument
	 * 
	 * @param filename filepath for font file
	 * @return true if created font without error
	 */
	public boolean loadFont(String filename){
		File f = new File(filename);
		if(!f.exists()) return false;
		else{
			try {
				this.font = Font.createFont(Font.PLAIN, f);
				return true;
			} catch (FontFormatException e) {
				logger.error("Issue with the font format",e);
				return false;
			} catch (IOException e) {
				logger.error("Could not load font as font",e);
				return false;
			}
		}
	}
	
	public Font getFont(int size){
		if(this.font != null){
			return this.font.deriveFont(size); 			 
		}
		else{
			return new Font("Courier", Font.PLAIN, size);
		}
	}
	

	public void loadSettings(File f) throws IOException{
	
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		String line = "";
		StringBuffer b = new StringBuffer();
		int lino =1;
		String n = Tools_System.getNewline();
		while((line = reader.readLine()) != null){
			if(line.startsWith("WIDTH:")){
				Integer i = Tools_String.parseString2Int(line.substring(6));
				if(i==null)b.append("Line:"+lino+" Width is not an integer"+n);
				else this.width=i;
			}
			else if(line.startsWith("HEIGHT:")){
				Integer i = Tools_String.parseString2Int(line.substring(7));
				if(i==null)b.append("Line:"+lino+" Height is not an integer"+n);
				else this.height=i;
			}
			else if(line.startsWith("PPMM:")){
				Double i = Tools_String.parseString2Double(line.substring(6));
				if(i==null)b.append("Line:"+lino+" PPMM is not a double"+n);
				else this.ppmm=i;
			}
			else if(line.startsWith("FONTSIZE:")){
				Integer i = Tools_String.parseString2Int(line.substring(9));
				if(i==null)b.append("Line:"+lino+" Fontsize is not an integer"+n);
				else this.fontsize=i;
			}
			else if(line.startsWith("FONTFILE:")){
				String s = line.substring(9);
				if(!loadFont(s)){
					b.append("Line: "+lino+" Failed to load font file");
				}
			}
			else if(line.startsWith("HGLITE:")){
				
			}
			else if(line.startsWith("#")){
				
			}
			else{
				b.append("Line: "+ lino+" not recognised! LINE("+line+")");
			}
			lino++;
		}
		fis.close();		
	}
	

}
