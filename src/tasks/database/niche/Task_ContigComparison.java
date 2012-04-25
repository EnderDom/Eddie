package tasks.database.niche;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;

import bio.objects.Contig;
import bio.xml.XML_Blastx;

import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;


import output.pdf.PDFBuilder;

import tasks.MapManager;
import tasks.Task;
import tools.Tools_Array;
import tools.Tools_File;
import tools.Tools_System;
import tools.bio.graphics.Tools_RoughImages;
import tools.graphics.Tools_Image;
import ui.UI;

public class Task_ContigComparison extends Task{

	public String outformat;
	Logger logger= Logger.getRootLogger();
	private HashMap<String, String> contig2file1;
	private HashMap<String, String> contig2file2;
	private String division1;
	private String division2;
	private String blastfolder1;
	private String blastfolder2;
	private String output;
	public MapManager mapman;
	public DatabaseManager manager;
	private UI ui;
	private int database_id;
	private String[] contignames;
	
	public Task_ContigComparison(){
		setHelpHeader("--This is the Help Message for the ContigComparison Task--");
		outformat = "PDF";
		mapman = new MapManager(ui.getPropertyLoader());
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		//if(cmd.hasOption("f"))outformat=cmd.getOptionValue("f");
		if(cmd.hasOption("o"))output = cmd.getOptionValue("o");
		if(cmd.hasOption("d1"))division1 = cmd.getOptionValue("d1");
		if(cmd.hasOption("d2"))division2 = cmd.getOptionValue("d2");
		if(cmd.hasOption("b1"))blastfolder1 = cmd.getOptionValue("b1");
		if(cmd.hasOption("b2"))blastfolder2 = cmd.getOptionValue("b2");
		if(cmd.hasOption("i")){
			contignames = Tools_File.quickRead(new File(cmd.getOptionValue("i"))).split(Tools_System.getNewline());
		}
		if(cmd.hasOption("c")){
			if(contignames == null){
				contignames = new String[]{cmd.getOptionValue("c")};
			}
			else{
				Tools_Array.mergeStrings(contignames, new String[]{cmd.getOptionValue("c")});
			}
		}
	}
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		//options.addOption(new Option("f","outformat", true, "Options currently are HTML"));
		options.addOption(new Option("o","output", true, "Output file"));
		options.addOption(new Option("d1","division1", true, "First 6-letter division ie CLCBIO"));
		options.addOption(new Option("i","input", true, "List of contigs to report (Assumed division 1)," +
				" as is in ACE file, separated by newline"));
		options.addOption(new Option("c","contig", true, "First 6-letter division ie CLCBIO"));
		options.addOption(new Option("d2","division2", true, "Second 6-letter division ie NEWBLE"));
		options.addOption(new Option("b1","blast1", true, "Blast folder for the first input"));
		options.addOption(new Option("b2","blast2", true, "Blast folder for the second input"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(testmode){
			runTest();
			return;
		}
		if(contignames == null || contignames.length == 0){
			logger.error("No contig(s) specified");
			return;
		}
		File b1 = new File(blastfolder1);
		if(!b1.isDirectory()){
			logger.error("Input1 is not a folder");
			return;
		}
		File b2 = new File(blastfolder2);
		if(!b2.isDirectory()){
			logger.error("blastfolder2 is not a folder");
			return;
		}
		logger.info("Starting database manager...");
		manager = this.ui.getDatabaseManager();
		if(!manager.open()){
			logger.error("Could not open a connection to the database");
			return;
		}
		
		logger.info("Checks complete, Continuing...");
		database_id = manager.getEddieDBID();
		BioSQLExtended bsxt = manager.getBioSQLXT();
	
		logger.debug("Database ID: " + this.database_id);
		logger.info("Starting Mapping Sequences to blasts");
		logger.debug("Checking for previous map");
		
		HashMap<String, String> name2id1 = bsxt.getContigNameNIdentifier(manager.getCon(), division1);
		HashMap<String, String> name2id2 = bsxt.getContigNameNIdentifier(manager.getCon(), division2);
		contig2file1 = mapFiles(name2id1, b1, blastfolder1, division1);
		if(contig2file1 == null){
			logger.error("Failed to map files for " + division1);
			return;
		}
		contig2file2 = mapFiles(name2id2, b2, blastfolder2, division2);
		if(contig2file2 == null){
			logger.error("Failed to map files for " + division2);
			return;
		}
		File out = new File(output);
		File t = null; 
		for(String s : name2id1.keySet()){
			for(int i =0 ;i < contignames.length; i++){
				if(contig2file1.get(s).equals(contignames[i])){
					Contig contig = generateContig(s);
					
					t = new File(contig2file1.get(s));
					if(t.exists()){
						try {
							XML_Blastx blastx = new XML_Blastx(t);
						} 
						catch (Exception e) {
							logger.error("Failed to parse blast file " + t.getPath() + ", you sure this is a blast XML?");
						}
					}
					else{
						logger.error("File " + contig2file1.get(s) + " does not appear to exist!");
					}
				}
			}
		}
		
		System.out.println("Method still incomplete!");
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public Contig generateContig(String contig_identifier){
		return null;//TODO
	}
		
	public void getTopContig(String contig_identifier){
		//TODO mysql shizzle
	}
	
	public HashMap<String, String> mapFiles(HashMap<String, String> contig2file, File b, String bf, String division){
		boolean gotmap = false;
		if(mapman.hasMap(division, b.getPath())){
			logger.debug("Has previous map, Loading...");
			try{
				contig2file =mapman.getMap(division1, b.getPath());
				gotmap = true;
			}
			catch(IOException io){
				logger.warn("Failed to load Map, despite having records of it??");
				gotmap = false;
				return null;
			}
		}
		if(!gotmap){
			if(contig2file.size() == 0){ 
				logger.error("No sequences with division "+ division);
				return null;
			}
			else{
				logger.debug("Found some "+ contig2file.size() + " for records for "+division);
				@SuppressWarnings("unchecked")
				HashMap<String, String> map =Tools_File.mapFiles((HashMap<String, String>)contig2file.clone(), b);
				HashMap<String, String> finalmap = new HashMap<String, String> ();
				for(String name : map.keySet()){
					for(String identifier : contig2file.keySet()){
						if(name.equals(identifier)){
							finalmap.put(contig2file.get(name), map.get(name));
						}
					}
				}
				mapman.addMap(division, bf, finalmap);
				map = null;
				return finalmap;
			}
		}
		else{
			return contig2file;
		}
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
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
}
