package enderdom.eddie.output.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDCcitt;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.jfree.chart.encoders.EncoderUtil;

import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.PropertyLoaderXT;

/**
 *
 * 
 * @author Dominic Wood
 *
 *
 * This just doesn't work
 * 
 * If I there was a @Borked tag I'd add it 
 * I may switch to iTextPDF instead as much
 * as I don't want to give up, I just can't
 * understand what the problem is here. (It
 * undoubtably is something i've miss understood)
 * 
 *
 */

public class PDFBuilder {

	PDDocument document;
	PDPage currentpage;
	PDPageContentStream contentStream;
	int pagecount;
	int linecount;
	
	public float lineheight;
	public float currentheight;
	private boolean includecredits =true;
	private float pageWidth;
	private float pageHeight;
	private float fontSize;
	private float headersize = 16;
	public float margin = 20;
	public float lineheightmargin = 4;
	private static PDFont font = PDType1Font.HELVETICA_BOLD;
	
	public PDFBuilder() throws IOException{
		document = new PDDocument();
		setFontSize(12);
		nextPage();	
	}
	
	public void setFontSize(float f){
		fontSize =f;
		lineheight = fontSize+lineheightmargin;
	}
	
	public void getDimensions(){
		PDRectangle pageSize = currentpage.findMediaBox();
		int rotation = currentpage.findRotation(); 
        boolean landscape = rotation == 90 || rotation == 270; 
		pageWidth = landscape ? pageSize.getHeight() : pageSize.getWidth(); //If landscape get height as width, else get width
        pageHeight = landscape ? pageSize.getWidth() : pageSize.getHeight();
	}
	
	public float getWidthofString(String message) throws IOException{
		return font.getStringWidth( message )*fontSize/1000f;
	}
	
	public void drawImage(String filepath) throws IOException{
		if(filepath.toLowerCase().endsWith(".jpg") || filepath.toLowerCase().endsWith(".jpeg")){
			PDJpeg ob = new PDJpeg(document, new FileInputStream(new File(filepath)));
			drawImage(ob);
		}
		else if(filepath.toLowerCase().endsWith(".tif") || filepath.toLowerCase().endsWith(".tiff")){
			PDCcitt tif = new PDCcitt(document, new RandomAccessFile(new File(filepath),"r"));
			drawImage(tif);
		}
	}
	
	/*
	 * Inputting BufferedImage into PDJpeg doesn't seem to work
	 * So force into jpeg the reload
	 */
	
	//TODO don't need to save just outputstream
	public void drawBufferedImage(BufferedImage img) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//File f = File.createTempFile("JPEGIO", ".jpg");
		EncoderUtil.writeBufferedImage(img, "jpeg", os);
        //create a new inputstream
		PDXObjectImage ob = new PDJpeg(document, new ByteArrayInputStream(os.toByteArray()));
		drawImage(ob);
	}
	
	public void drawImage(PDXObjectImage ximage) throws IOException{
		
		contentStream = new PDPageContentStream(document, currentpage, true, true);
		float hei = ximage.getHeight();
		
		float internalhei = pageHeight-(margin*2);
		if(hei > internalhei){
			hei = internalhei;
		}
		float wid = ximage.getWidth();
		float internalwid =pageWidth-(margin*2); 
		if(wid > internalwid){
			wid = internalwid;
		}
		
		float minratio = Math.min(wid/ximage.getWidth(), hei/ximage.getHeight());
		wid = ximage.getWidth()*minratio;
		hei = ximage.getHeight()*minratio;
		if(currentheight - hei < margin){
			nextPage();
		}
		
		contentStream.drawXObject(ximage, margin, currentheight - hei, wid, hei);
		contentStream.close();
		currentheight-=hei;
	}
	
	public void writeSimpleHeader(String str) throws IOException{
		contentStream = new PDPageContentStream(document, currentpage, true, true);
		contentStream.beginText();
		contentStream.setFont(font, headersize );
		contentStream.moveTextPositionByAmount(margin, currentheight );
		contentStream.drawString(str);
		contentStream.moveTextPositionByAmount(0, -lineheight*2);
		currentheight -=lineheight*2;
		contentStream.endText();
		contentStream.close();
	}
	
	public void paragraph(){
		currentheight -=lineheight*2;
	}
	
	public void writeLines(String str) throws IOException{
		writeLines(str, this.fontSize);
	}
	
	public void writeLines(String str, float fontsize) throws IOException{
		contentStream = new PDPageContentStream(document, currentpage, true, true);
		contentStream.beginText();
		contentStream.setFont(font, fontsize );
		contentStream.moveTextPositionByAmount(margin, currentheight );
		float space = getWidthofString(" ");
		String[] s = str.split(" ");
		float len = 0;
		int c = s.length -1;
		StringBuffer builder = new StringBuffer();
		for(c =0; c < s.length ; c++){
			float strlen = getWidthofString(s[c]) ;
			if(currentheight+margin > pageHeight || currentheight-margin < 0){ //If new page needed
				nextPage();
				contentStream = new PDPageContentStream(document, currentpage, true, true);
				contentStream.beginText();
				contentStream.setFont(font, fontsize );
				contentStream.moveTextPositionByAmount(margin, currentheight );
			}
			if(len + strlen > this.pageWidth-(margin*2)){ //If adding word makes line too long
				contentStream.drawString(builder.toString());
				contentStream.moveTextPositionByAmount(0, -lineheight);
				currentheight -=lineheight;
				builder = new StringBuffer();
				//Add word to next line
				len =strlen+space;
				if(len >  this.pageWidth-(margin*2)){//Whole word is too large
					Logger.getRootLogger().warn("Does not yet support words longer than two lines");
					s[c] = "**REMOVED**";
					len = getWidthofString(s[c]);
					if(len > this.pageWidth-(margin*2) ){
						Logger.getRootLogger().error("Width / FontSize ratio too low");
						s[c] ="";
					}
				}
				builder.append(new StringBuffer(s[c]) + " ");
			}
			else{
				len += strlen+space;
				builder.append(new StringBuffer(s[c]) + " ");
			}
		}
		if(builder.length() > 0){
			if(currentheight+margin > pageHeight){
				nextPage();
				contentStream = new PDPageContentStream(document, currentpage, true, true);
				contentStream.beginText();
				contentStream.setFont(font, fontsize );
				contentStream.moveTextPositionByAmount(margin, currentheight );
			}
			contentStream.drawString(builder.toString());
			contentStream.moveTextPositionByAmount(0, -lineheight);
			currentheight -=lineheight;
		}
		contentStream.endText();
		contentStream.close();
	}
	
	public void writeLines(String[] text) throws IOException{
		for(int i =0; i < text.length; i++){
			writeLines(text[i]);
		}
	}
	
	public void writeLines(String[] text, int fontsize) throws IOException{
		for(int i =0; i < text.length; i++){
			writeLines(text[i], fontsize);
		}
	}
	
	public void writeParagraphs(String[] text) throws IOException{
		for(int i =0; i < text.length; i++){
			writeLines(text[i]);
			paragraph();
		}
	}
	
	public void nextPage() throws IOException{
		currentpage = new PDPage(PDPage.PAGE_SIZE_A4);
		document.addPage(currentpage);
		if(currentheight == 0)getDimensions();
		currentheight = pageHeight-margin;
		pagecount++;
	}
	
	public void save(String filepath) throws COSVisitorException, IOException{
		if(contentStream != null)contentStream.close();
		if(isIncludecredits())addCredits();
		document.save(filepath);
		document.close();
		document = null;
	}

	public boolean isIncludecredits() {
		return includecredits;
	}

	public void setIncludecredits(boolean includecredits) {
		this.includecredits = includecredits;
	}
	
	private void addCredits() throws IOException{
		nextPage();
		currentheight =lineheight*3;
		String[] strs = new String[]{"PDF Autogenerated by Eddie v"+PropertyLoaderXT.getFullVersion()+
				" using the PDFBox Apache Library", "Author: Dominic Matthew Wood 2012","Date: "+Tools_System.getDateNow()};
		
		writeLines(strs, 8);
	}
	
	public static void testAddImage2Pdf(String inputFile, String image, String outputFile) throws IOException, COSVisitorException{
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
