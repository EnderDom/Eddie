package enderdom.eddie.tools.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import enderdom.eddie.tools.Tools_System;

public class Tools_Image {

	//From : http://stackoverflow.com/questions/3772098/how-does-java-awt-color-getcolorstring-colorname-work
	//Doesn't seem to work???
	public static Color getColor(String colorname){
		try {
			Field field = Color.class.getField(colorname);
			return (Color)field.get(null);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static double convertDPI2DPMM(double dpi){
		return dpi/25.4;
	}
	
	
	/* Sticks two images together, one on top of the other
	 * As yet negative padding not supported
	 */	
	public static BufferedImage simpleMerge(BufferedImage one, int xpad, BufferedImage two, int xpad2, int ymargin, Color background, int BGRTYPE){
		
		int width = Math.max(one.getWidth()+xpad,two.getWidth()+xpad2);
		int height = one.getHeight()+two.getHeight()+ymargin*3;
		BufferedImage image = new BufferedImage(width,height,BGRTYPE);
		
		Graphics g = image.getGraphics();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(background);
		g2.drawRect(0, 0, width, height);
		g2.fillRect(0, 0, width, height);
		g2.drawImage(one, xpad, ymargin, null);		
		g2.drawImage(two, xpad2, ymargin+ymargin+one.getHeight(), null);
		image.flush();
		return image;
	}
	
	/*
	 * xpads holds the left align, setting to 0 is obviously right aligned,
	 * set to -1 for rightaligned
	 */
	public static BufferedImage simpleMerge(BufferedImage[] imgs, int[] xpads, int ymargin, Color background, int BGRTYPE){
		int width = 0;
		int height = ymargin;
		
		for(int i =0; i < imgs.length ; i++){
			if(xpads[i] != -1){
				if(width < imgs[i].getWidth()+xpads[i]){ 
					width = imgs[i].getWidth()+xpads[i];
				}
			}
			else{
				if(width < imgs[i].getWidth()){ 
					width = imgs[i].getWidth();
				}
			}
			height+= imgs[i].getHeight()+ymargin;
		}
		for(int i =0; i < xpads.length; i++){
			if(xpads[i] == -1){
				xpads[i] = width-imgs[i].getWidth();
			}
		}
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics g = image.getGraphics();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(background);
		g2.drawRect(0, 0, width, height);
		g2.fillRect(0, 0, width, height);
		int h = ymargin;
		for(int i =0; i < imgs.length; i++){
			g2.drawImage(imgs[i], xpads[i], h, null);
			h=imgs[i].getHeight()+ymargin;
		}
		image.flush();
		return image;
	}
	
	public static boolean image2File(File outputfile, BufferedImage img, String filetype){
		try {
			ImageIO.write(img, filetype, outputfile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean image2File(String filepath, BufferedImage img, String filetype){
		File outputfile = new File(filepath+"."+filetype);
		return image2File(outputfile, img, filetype);
	}
	
	public static boolean image2PngFile(String filepath,BufferedImage img){
		return image2File(filepath, img, "png");
	}
	
	public static boolean image2TifFile(String filepath,BufferedImage img){
		return image2File(filepath, img, "tiff");
	}
	
	public static InputStream image2Stream(BufferedImage img) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(img,"png",os);
		return new ByteArrayInputStream(os.toByteArray());
	}
	
	public static BufferedImage drawTestImage(){
		BufferedImage image = new BufferedImage(200,200,BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics g = image.getGraphics();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.drawRect(0, 0, 200, 200);
		g2.fillRect(0, 0, 200, 200);
		g2.setColor(Color.black);
		g2.drawRect(50, 50, 100, 100);
		g2.setColor(Color.orange);
		g2.fillRect(50, 50, 100, 100);
		g2.setColor(Color.black);
		g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		g2.drawString("TEST IMAGE BY Eddie", 75, 75);
		g2.drawString(Tools_System.getDateNow(), 75, 85);
		g2.drawString(System.getProperty("os.name")+" " + System.getProperty("os.arch"), 75, 95);
		return image;
	}
	
	
	public static void saveImageDPI(File output, BufferedImage image, double dpi)throws IOException {
		double dpmm = dpi /25.4;
		saveImageDPMM(output, image, dpmm);
	}
	
	public static void saveImageDPMM(File output, BufferedImage image, double dpmm) throws IOException {
	    output.delete();

	    final String formatName = "png";

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	       ImageWriter writer = iw.next();
	       ImageWriteParam writeParam = writer.getDefaultWriteParam();
	       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(image.getType());
	   
	       IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	          continue;
	       }

	       setDPI(metadata, dpmm);

	       final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	       try {
	          writer.setOutput(stream);
	          writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
	       } finally {
	          stream.close();
	       }
	       break;
	    }
	 }

	 public static void setDPI(IIOMetadata metadata, double dpmm) throws IIOInvalidTreeException {
	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dpmm));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dpmm));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	 }
}
