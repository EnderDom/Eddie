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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import enderdom.eddie.bio.homology.ClustalAlign;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.graphics.Tools_Image;

//WIP

public class ClustalImage extends ClustalAlign implements BioImg_Object{
	
	//Defaults for size of image and resolution
	public static double wdefault = 210.0; //mm
	public static double hdefault = 400.0; //mm
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
	
	//Padding for letters in box
	private int internalpadside =7;
	private int internalpadtop =13;
	
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
	
	public BufferedImage getBufferedImage(){
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
		for(int l =0; l < len; l+=defaultbp){
			heightcount++;
			for(String s : this.keySet()){
				//Draw Sequence Name
				g2.drawString(this.getSequence(s).getIdentifier(), 2,(heightcount*boxheight)-10);
				//Draw a box for letter
				for(int j =0; j < defaultbp+defaultgap+defaultword;j++){
					g2.drawRect(j*boxwidth, (heightcount-1)*boxheight, boxwidth, boxheight);
				}
				//Draw Letter, if index outside length, don't draw
				for(int j = 0;j < defaultbp;j++){
					if(this.getSequence(s).getSequence().length() > l+j+1){
						
						
						g2.drawString(this.getSequence(s).getSequence().substring(l+j, l+j+1), 
							((j+defaultgap+defaultword)*boxwidth)+internalpadside, (heightcount*boxheight)-internalpadtop);
					}
				}
				//Draw Lengths at the end
				if(this.getSequence(s).getSequence().length() > l+defaultbp){
					g2.drawString(this.getSequence(s).getLengthAtIndex(l+defaultbp)+"", 
							((defaultbp+defaultgap+defaultword)*boxwidth)+(boxwidth/2),(heightcount*boxheight)-10);
				}
				else{
					g2.drawString(this.getSequence(s).getActualLength()+"", 
							((defaultbp+defaultgap+defaultword)*boxwidth)+(boxwidth/2),(heightcount*boxheight)-10);
				}
				//Increase The row count
				heightcount++;
			}
		}
		return image;
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
	

	public void loadSettings(File f) throws IOException, BioImg_Exception{
	
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		String line = "";
		int lino =1;
		while((line = reader.readLine()) != null){
			parseLine(line, lino);
		}
		fis.close();		
	}

	public int parseLine(String line, int lino) throws BioImg_Exception {
		if(line.startsWith("@WIDTH;")){
			Integer i = Tools_String.parseString2Int(line.substring(6));
			if(i==null)logger.warn("Line:"+lino+" Width is not an integer, "+line);
			else this.width=i;
		}
		else if(line.startsWith("@HEIGHT;")){
			Integer i = Tools_String.parseString2Int(line.substring(7));
			if(i==null)logger.warn("Line:"+lino+" Height is not an integer"+line);
			else this.height=i;
		}
		else if(line.startsWith("@PPMM;")){
			Double i = Tools_String.parseString2Double(line.substring(6));
			if(i==null)logger.warn("Line:"+lino+" PPMM is not a double"+line);
			else this.ppmm=i;
		}
		else if(line.startsWith("@FONTSIZE;")){
			Integer i = Tools_String.parseString2Int(line.substring(9));
			if(i==null)logger.warn("Line:"+lino+" Fontsize is not an integer"+line);
			else this.fontsize=i;
		}
		else if(line.startsWith("@FONTFILE;")){
			String s = line.substring(9);
			if(!loadFont(s)){
				logger.warn("Line: "+lino+" Failed to load font file");
			}
		}
		else if(line.startsWith("@HGLITE;")){
			
		}
		else if(line.startsWith("#")){
			
		}
		else{
			logger.warn("Line: "+ lino+" not recognised! LINE("+line+")");
		}
		return lino++;
	}

	public double getDPMM(){
		return this.ppmm;
	}

}
