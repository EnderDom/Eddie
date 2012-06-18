package tasks.testing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;

import output.pdf.PDFBuilder;
import tools.Tools_File;
import tools.bio.graphics.Tools_RoughImages;
import tools.graphics.Tools_Image;

public class Task_Test_PDFBuilder {

	Logger logger = Logger.getRootLogger();
	
	public void run(){

		//TEST DATA
		logger.debug("Running Test of PDF And Image Generation");
		int[]   contig1 = new int[]  {0 ,1 ,1 ,1 ,3 ,5 ,67,64};
		int[]   len1    = new int[]  {10,31,31,12,80,10,25,50};
		short[] colors1 = new short[]{0, 1, 1, 1, 0 ,1 ,1 ,1 };
		int[]   contig2 = new int[]  {3 ,4 ,4 ,9 ,6 ,8 ,70,67,85};
		short[] colors2 = new short[]{1 ,1 ,1 ,0 ,1 ,1 ,1 ,1 ,0 };
		int[]   len2    = new int[]  {10,31,31,23,12,80,10,25,50};
		int[][] pos1 = new int[][]{contig1, len1};
		int[][] pos2 = new int[][]{contig2, len2};
		
		int[] b1 = new int[]{0 ,1 ,1 ,4 ,4 ,5 ,5 ,5 ,5 ,5 ,5 ,5 ,10};
		int[] b2 = new int[]{10,10,10,10,10,10,10,10,10,10,10,15,5 };
		int[][]b3 = new int[][]{b1,b2};
		
		logger.debug("Generating Images");
		BufferedImage c1 = Tools_RoughImages.drawContigRough("Contig1",null,false, pos1, b3, colors1, 10, 5);
		BufferedImage c2 = Tools_RoughImages.drawContigRough("Contig2",null,false, pos2, b3, colors2, 10, 5);
		BufferedImage c3 = Tools_Image.simpleMerge(c1, 3, c2, 0,10, Tools_RoughImages.background, Tools_RoughImages.defaultBGR);
		logger.debug("Saving Images");
		Tools_Image.image2PngFile("test1", c1);
		Tools_Image.image2PngFile("test2", c2);
		Tools_Image.image2PngFile("test3", c3);
		
		try{
			logger.debug("Saving PDF...");
			PDFBuilder build = new PDFBuilder();
			//build.drawBufferedImage(c3);
			//build.writeSingleLine("Test");
			build.drawBufferedImage(c3);
			build.writeLines(Tools_File.quickRead(new File("test.txt")));
			
			build.save("test.pdf");
			logger.debug("Done");
		}
		catch(IOException io){
			logger.error("Failed to write to pdf", io);
		} 
		catch (COSVisitorException e) {
			logger.error("Failed to write to pdf", e);
		}
	}
	
}
