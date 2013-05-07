package enderdom.eddie.graphics.bio;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.graphics.Tools_BioImg;

/**
 * 
 * @author dominic
 *
 * I was considering using Feature from biojava
 * with the idea that later on this could also
 * be plugged into other stuff more readily. But
 * Feature is just a bit to chocked full o' stuff
 * for me to deal with. I'm never going to be able
 * to integrate into biojava at this rate
 *
 */

public class BioImg_Transcript implements BioImg_Object{

	Logger logger = Logger.getRootLogger();
	private int stop = -1;
	private int cds_start = -1;
	@SuppressWarnings("unused")
	private int cds_stop = - 1;
	@SuppressWarnings("unused")
	private boolean cds_show;
	boolean UTR = false;
	LinkedList<BioImg_Region> motifs = new LinkedList<BioImg_Region>();
	LinkedList<BioImg_Primer> primers = new LinkedList<BioImg_Primer>();
	private static String region = "@REGION;";
	private static String primer = "@PRIMER;";
	private static String ppi = "@PPI;";
	private static String width = "@WIDTH;";		
	private static String height = "@HEIGHT;";
	private static String title = "@TITLE;";
	private static String utr = "@UTR;";
	private static String gene = "@GENE;";
	private static String cds = "@CDS;";
	private static double dpi2mm = 1/25.4;
	private double dpmm = 20;
	private double wit = 180;
	private double heit = 80;
	private String titl;
	private double witplier = 0.0;
	private double margin = 0.05;
	private double barheight = 0.2;
	private double linewid = 0.7;
	
	public BioImg_Transcript(){
		
	}
	
	//A bunch of this could be avoided with setStroke :(
	public BufferedImage getBufferedImage() {
		int linewidth = (int)(linewid*dpmm);
		linewidth += linewidth%2;
		int lw = linewidth/2;
		logger.debug("Penicl width is " + linewidth);
		wit = (double)wit*dpmm;
		heit = (double)heit*dpmm;
		
		logger.debug("Stop is " + stop);
		
		double marg = margin*wit;
		witplier = (wit-marg)/stop;
		double barheit = barheight*heit;
		int mid = (int)(heit*0.5);
		int bary = mid - ((int)(barheit*0.5));
		int barx = (int)marg/2;
		
		Collections.sort(motifs);
		Collections.sort(primers);
		
		int w= (int)wit;
		int h = (int)heit;
		logger.debug("Width x Height of image in pixels: " + w + "x" +h);
		logger.debug("Margin: "+ marg);
		
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(new Font("Arial", Font.BOLD, 16));
		FontMetrics metr  = g.getFontMetrics();
		
		
		//Draw Box
		g.setColor(Color.white);
		g.drawRect(0,0,w, h);
		g.fillRect(0,0,w, h);
		g.setColor(Color.BLACK);
		for(int i =0;i < lw; i++){
			g.drawRect(0+i,0+i, w-2-(i*2), h-2-(i*2));
		}
		g.setFont(new Font("Times New Roman", Font.PLAIN,20));
		metr  = g.getFontMetrics();
		int yf = (int)(margin*heit)+metr.getHeight();
		int xf = (w/2)-(metr.stringWidth(titl)/2)-2;
		g.drawString(titl, xf, yf);
		for(int i =0; i < 3 ;i++)g.drawLine(xf, yf+5+i, xf+metr.stringWidth(titl), yf+5+i);
		
		
		//Draw Region
		
		int l =barx;
		for(int j =0; j < lw; j++){
			g.drawLine(l-(lw/2)+j, mid-(int)((double)barheit*0.25), l-(lw/2)+j, mid+(int)((double)barheit*0.25));
		}
		
		for(int i=0;i < motifs.size(); i++){
			int start = (int)((double)motifs.get(i).start*witplier)+barx;
			int stop = (int)((double)motifs.get(i).stop*witplier)+barx;
			logger.debug("Start is " + start + " and stop is " + stop);
			if(l+lw < start){
				for(int j = 0; j < lw; j++){
					g.drawLine(l, mid-(lw/2)+j, start, mid-(lw/2)+j);
				}
			}
			Rectangle filler = new Rectangle(start, bary, stop-start, (int)barheit);
			g.setPaint(new TexturePaint(motifs.get(i).getTexture(stop-start, (int)barheit, lw), filler));
			g.fill(filler);
			g.setColor(Color.BLACK);
			for(int j =0 ; j < lw; j++){
				g.drawRect(start+j, bary+j, stop-start-(j*2), (int)barheit-(j*2));
			}		
			
			g.setFont(new Font("Arial", Font.BOLD, 16));
			metr  = g.getFontMetrics();
			int space = 2;
			int ftw = metr.stringWidth(motifs.get(i).shortname);
			int fth = metr.getHeight();
			int fx = start+((stop-start)/2) - (ftw/2);
			int fy = mid - (fth/2);
			g.setColor(Color.WHITE);
			g.fillRect(fx-space, fy-space, ftw+(space*2), fth+(space*2));
			g.setColor(Color.BLACK);
			g.drawString(motifs.get(i).shortname, fx-2, mid+((int)((double)fth*0.35)));
			
			l=stop;
		}
		if(l+lw < w-barx){
			for(int j =0; j < lw; j++){
				g.drawLine(l, mid-(lw/2)+j, w-barx, mid-(lw/2)+j);
			}
		}
		for(int j =0; j < lw; j++){
			g.drawLine(w-barx-(lw/2)+j, mid-(int)((double)barheit*0.25), w-barx-(lw/2)+j, mid+(int)((double)barheit*0.25));
		}
		

		return img;
	}

	public void parseLine(String line, int linenumb) throws BioImg_Exception {
		if(line.startsWith(region)){
			String[] s = line.substring(region.length(), line.length()).split(",");
			if(s.length == 6){
				BioImg_Region r = new BioImg_Region(s[0], s[1], s[2], s[3], s[4],s[5], cds_start, linenumb);
				motifs.add(r);
			}
			else{
				throw new BioImg_Exception("Line " + linenumb + " REGION tag does not have 6 parameters");
			}
		}
		else if(line.startsWith(primer)){
			String[] s = line.substring(primer.length(), line.length()).split(",");
			if(s.length == 3){
				BioImg_Primer r = new BioImg_Primer(s[0], s[1], s[2], cds_start, linenumb);
				primers.add(r);
			}
			else{
				throw new BioImg_Exception("Line " + linenumb + " REGION tag does not have 5 parameters");
			}
		}
		else if(line.startsWith(ppi)){
			String s = line.substring(ppi.length(), line.length()).trim();
			Integer i = Tools_String.parseString2Int(s);
			if(i!=null){
				dpmm = dpi2mm*(double)i;
			}
			else{
				throw new BioImg_Exception("PPI is not a parsable integer line " + linenumb);
			}
		}
		else if(line.startsWith(width)){
			String s = line.substring(width.length(), line.length()).trim();
			Integer i = Tools_String.parseString2Int(s);
			if(i!=null){
				wit = i;
			}
			else{
				throw new BioImg_Exception("Width is not a parsable integer line " + linenumb);
			}
		}
		else if(line.startsWith(height)){
			String s = line.substring(height.length(), line.length()).trim();
			Integer i = Tools_String.parseString2Int(s);
			if(i!=null){
				heit = i;
			}
			else{
				throw new BioImg_Exception("Height is not a parsable integer line " + linenumb);
			}
		}
		else if(line.startsWith(title)){
			titl = line.substring(title.length(), line.length()).trim();
			logger.debug("Title is set to " + titl);
		}
		else if(line.startsWith(utr)){
			UTR = line.substring(line.indexOf(utr)).trim().equalsIgnoreCase("true");
		}
		else if(line.startsWith(gene)){
			this.stop = Tools_BioImg.parseNumb(line.substring(gene.length()), linenumb, -1);
			logger.debug("Parse stop as "+ this.stop);
		}
		else if(line.startsWith(cds)){
			String[] s = line.substring(cds.length()).split(",");
			if(s.length == 3){
				this.cds_start = Tools_BioImg.parseNumb(s[0], linenumb, -1);
				this.cds_stop = Tools_BioImg.parseNumb(s[1], linenumb, -1);
				this.cds_show = s[2].trim().equalsIgnoreCase("true");
			}
			else{
				throw new BioImg_Exception("CDS should have 2 position parameters, line " + linenumb); 
			}
		}
		else{
			logger.warn("Unrecognised tag " + line + " at line " + linenumb);
		}
	}
	
}
