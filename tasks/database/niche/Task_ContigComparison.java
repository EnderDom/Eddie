package tasks.database.niche;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDCcitt;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import output.pdf.PDFBuilder;

import tasks.Task;
import tools.Tools_System;
import tools.bio.graphics.Tools_RoughImages;
import tools.graphics.Tools_Image;

public class Task_ContigComparison extends Task{

	public String outformat;
	public String testfolder;
	Logger logger= Logger.getRootLogger();
	
	public Task_ContigComparison(){
		setHelpHeader("--This is the Help Message for the ContigComparison Task--");
		outformat = "PDF";
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("f","outformat", true, "Options currently are HTML"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		try{
			if(testmode){
				runTest();
				return;
			}
		}
		catch(Exception e){
			logger.error("Run",e);
			
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	
	public void runTest(){

		//TEST DATA
		logger.debug("Running Test of Contig Comparison");
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
		BufferedImage c1 = Tools_RoughImages.drawContigRough("Contig1",false, pos1, b3, colors1, 10, 5);
		BufferedImage c2 = Tools_RoughImages.drawContigRough("Contig2",false, pos2, b3, colors2, 10, 5);
		BufferedImage c3 = Tools_Image.simpleMerge(c1, 3, c2, 0,10, Tools_RoughImages.background, Tools_RoughImages.defaultBGR);
		logger.debug("Saving Images");
		Tools_Image.image2PngFile("test1", c1);
		Tools_Image.image2PngFile("test2", c2);
		Tools_Image.image2PngFile("test3", c3);
		
		try{
			logger.debug("Saving PDF...");
			PDFBuilder build = new PDFBuilder();
			build.nextPage();
			build.drawBufferedImage(c3);
			build.writeSingleLine("Test");
			build.save("test.pdf");
			logger.debug("Done");
			test2("test.pdf", "/tmp/JPEGIO6589438095402104780.jpg", "test2.pdf");
		}
		catch(IOException io){
			logger.error("Failed to write to pdf", io);
		} 
		catch (COSVisitorException e) {
			logger.error("Failed to write to pdf", e);
		}
	}
	
	public void test2(String inputFile, String image, String outputFile) throws IOException, COSVisitorException{
		 PDDocument doc = null;
	        try
	        {
	            doc = PDDocument.load( inputFile );

	            //we will add the image to the first page.
	            PDPage page = (PDPage)doc.getDocumentCatalog().getAllPages().get( 0 );

	            PDXObjectImage ximage = null;
	            if( image.toLowerCase().endsWith( ".jpg" ) )
	            {
	                ximage = new PDJpeg(doc, new FileInputStream( image ) );
	            }
	            else if (image.toLowerCase().endsWith(".tif") || image.toLowerCase().endsWith(".tiff"))
	            {
	                ximage = new PDCcitt(doc, new RandomAccessFile(new File(image),"r"));
	            }
	            else
	            {
	                //BufferedImage awtImage = ImageIO.read( new File( image ) );
	                //ximage = new PDPixelMap(doc, awtImage);
	                throw new IOException( "Image type not supported:" + image );
	            }
	            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true);

	            contentStream.drawImage( ximage, 20, 20 );

	            contentStream.close();
	            doc.save( outputFile );
	        }
	        finally
	        {
	            if( doc != null )
	            {
	                doc.close();
	            }
	        }
	}
}
