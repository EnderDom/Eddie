package tasks.database.niche;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;

import bio.objects.ContigXT;
import bio.xml.XML_Blastx;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.bioSQL.objects.ContigFactory;
import databases.manager.DatabaseManager;


import output.pdf.PDFBuilder;

import tasks.MapManager;
import tasks.Task;
import tools.Tools_Array;
import tools.Tools_File;
import tools.Tools_Math;
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
	private BioSQLExtended bsxt; 
	private BioSQL bs;
	private File debugfile;
	private static double contigcutoff = 0.2;
	private boolean reads;
	
	public Task_ContigComparison(){
		setHelpHeader("--This is the Help Message for the ContigComparison Task--");
		outformat = "PDF";
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		//if(cmd.hasOption("f"))outformat=cmd.getOptionValue("f");
		if(cmd.hasOption("o"))output = cmd.getOptionValue("o");
		if(cmd.hasOption("d1"))division1 = cmd.getOptionValue("d1");
		if(cmd.hasOption("d2"))division2 = cmd.getOptionValue("d2");
		if(cmd.hasOption("b1"))blastfolder1 = cmd.getOptionValue("b1");
		if(cmd.hasOption("b2"))blastfolder2 = cmd.getOptionValue("b2");
		if(cmd.hasOption("reads"))reads=true;
		if(cmd.hasOption("i")){
			contignames = Tools_File.quickRead(new File(cmd.getOptionValue("i"))).split(Tools_System.getNewline());
		}
		if(cmd.hasOption("c")){
			if(contignames == null){
				contignames = new String[]{cmd.getOptionValue("c")};
			}
			else{
				contignames = Tools_Array.mergeStrings(contignames, new String[]{cmd.getOptionValue("c")});
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
		options.addOption(new Option("c","contig", true, "Name of contig as is in ace file, alternative to input"));
		options.addOption(new Option("d2","division2", true, "Second 6-letter division ie NEWBLE"));
		options.addOption(new Option("b1","blast1", true, "Blast folder for the first input"));
		options.addOption(new Option("b2","blast2", true, "Blast folder for the second input"));
		options.addOption(new Option("r","reads", false, "Draw read names on image"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	/*
	 * Forgive me, had paper revisions to do
	 * John wanted to show the that there was
	 * limited difference between the two
	 * assemblies for the main contigs we were
	 * talking about. This was my cobbled together
	 * solution. Quite a lot of improvements could
	 * and should be done. For a starts I hate the 
	 * ContigFactory. I think a read2contig object 
	 * could be created. And a simple contig object
	 * which contains data. This would then be extended
	 * to a 'graphical' contig which could be drawn in 
	 * a variety of ways. Perhaps look into JalView and 
	 * consider a way of implementing it so it would 
	 * be easy for the contig to be drawn in a jalview sub
	 * window. But for now I give you this shite.
	 * 
	 */
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		if(testmode){
			runTest();
			return;
		}
		if(division1 == null || division2 == null){
			logger.error("Failed Due to no division stated");
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
		mapman = new MapManager(ui.getPropertyLoader());
		logger.info("Checks complete, Continuing...");
		database_id = manager.getEddieDBID();
		bsxt = manager.getBioSQLXT();
		bs = manager.getBioSQL();
		
		logger.debug("Database ID: " + this.database_id);
		logger.info("Starting Mapping Sequences to blasts");
		logger.debug("Checking for previous map");

		//Holds the actual name as is in file (ACE) record
		HashMap<String, String> name2id1 = bsxt.getContigNameNIdentifier(manager.getCon(), division1);
		HashMap<String, String> name2id2 = bsxt.getContigNameNIdentifier(manager.getCon(), division2);
		
		//Holds the Contig id 2 blast file for division 1
		contig2file1 = mapFiles(name2id1, b1, blastfolder1, division1);
		if(contig2file1 == null){
			logger.error("Failed to map files for " + division1);
			return;
		}
		//Holds the Contig id 2 blast file for division 2
		contig2file2 = mapFiles(name2id2, b2, blastfolder2, division2);
		if(contig2file2 == null){
			logger.error("Failed to map files for " + division2);
			return;
		}
	
		String assembler1 = bsxt.getNamesFromTerm(manager.getCon(), division1)[0];
		String assembler2 = bsxt.getNamesFromTerm(manager.getCon(), division2)[0];
		File t = null;
		PDFBuilder builder = null;
		int mcount =0;
		
		try {
			builder = new PDFBuilder();
			for(String s : name2id1.keySet()){
				for(int i =0 ;i < contignames.length; i++){
					if(s.equals(contignames[i])){
						mcount++;
						logger.debug("Developing Contig " + contignames[i]);
						int contig_id = bs.getBioEntry(manager.getCon(),name2id1.get(s), null, manager.getEddieDBID());
						ContigFactory factory = new ContigFactory();
						ContigXT contigxt = factory.getContigXT(manager,contig_id, division1);
						contigxt.setName(contignames[i] + " - " + assembler1);
						contigxt = getTopContigAndColors(contigxt, division2);
						int topcontigid = contigxt.getTopContig();
						
						String[] topcontigname = bs.getBioEntryNames(manager.getCon(), topcontigid);
						logger.debug("Top Contig is: " + topcontigname[0]);
						ContigXT othercontig = factory.getContigXT(manager, topcontigid, division2);
						othercontig.setName(topcontigname[0] + " - " + assembler2);
						othercontig = getTopContigAndColors(othercontig, division1);
						if(topcontigid > -1){
							contigxt.overlayContig(othercontig, topcontigid);
						}
			
						logger.debug("Retrieving Blast Data for "+name2id1.get(s));
						t = new File(contig2file1.get(name2id1.get(s)));
						ArrayList<String> blasts = new ArrayList<String>();
						try {
							XML_Blastx blastx = new XML_Blastx(t);
								if(blastx.getNoOfHits() != 0){
								blasts.add("");
								blasts.add("TOP 3 Blasts for " + contignames[i] + " - "+assembler1); 
								blasts.add("");
								for(int k =1; k < blastx.getNoOfHits() && k < 4;k ++){
									blasts.add("HIT"+k+": "+blastx.getHitTagContents("Hit_def", k));
								}
							}
							else{
								blasts.add("");
								blasts.add("No blast hits found for " + topcontigname[0] + " - "+assembler2);
								blasts.add("");
							}
						} 
						catch (Exception e) {
							logger.error("Failed to parse blast file " + t.getPath() + ", you sure this is a blast XML?",e);
						}
						blasts.add("");
						logger.debug("About to retrieve contig " + topcontigname[0] +" blast data");
						if(topcontigid > -1){
							t = new File(contig2file2.get(topcontigname[2]));
							try {
								XML_Blastx blastx = new XML_Blastx(t);
								if(blastx.getNoOfHits() != 0){
									blasts.add("");
									blasts.add("TOP 3 Blasts for " + topcontigname[0] + " - "+assembler2);
									blasts.add("");
									for(int k =1; k < blastx.getNoOfHits() && k < 4;k ++){
										blasts.add("HIT"+k+": "+blastx.getHitTagContents("Hit_def", k));
									}
								}
								else{
									blasts.add("");
									blasts.add("No blast hits found for " + topcontigname[0] + " - "+assembler2);
									blasts.add("");
								}
							} 
							catch (Exception e) {
								logger.error("Failed to parse blast file " + t.getPath() + ", you sure this is a blast XML?", e);
							}
						}
						logger.debug("About to retrieve top contig " + topcontigname[0]);
						if(mcount != 1)builder.nextPage();
						builder.writeSimpleHeader("Assembly to Assembly Comparison of " + contignames[i] + " from Assembly " + assembler1);
						builder.paragraph();
						builder.writeLines(writeStats(contigxt, othercontig, assembler1, assembler2));
						builder.writeSimpleHeader("Top Blasts:");
						builder.writeLines(blasts.toArray(new String[0]));
						builder.writeLines("Figure " + mcount + " shows a diagram of the two assemblies");
						builder.nextPage();
						builder.writeSimpleHeader("Figure " + mcount);

						String[] reads1 = null;
						String[] reads2 = null;
						if(reads){
							logger.debug("Getting reads, again... Did this somewhere else methinks?");
							int[] reads1_ids = contigxt.getReadIDs();
							reads1 =new String[reads1_ids.length];
							for(int o =0;  o < reads1_ids.length ;o ++){
								reads1[o] = bs.getBioEntryNames(manager.getCon(), reads1_ids[o])[0];
							}
							reads1_ids = othercontig.getReadIDs();
							if(reads1_ids != null && reads1_ids.length > 0){
								reads2 = new String[reads1_ids.length];
								for(int o =0;  o < reads1_ids.length ; o ++){
									reads2[o] = bs.getBioEntryNames(manager.getCon(), reads1_ids[o])[0];
								}
							}
						}
						BufferedImage c1 = Tools_RoughImages.drawContigRough(contigxt.getName(), reads1, false, contigxt.getReadPositions(), contigxt.getBlasts(), contigxt.getColors(), 10, 0.2);
						Tools_Image.image2File("c1", c1, "png");
						BufferedImage c2 = Tools_RoughImages.drawContigRough(othercontig.getName(), reads2, false, othercontig.getReadPositions(), othercontig.getBlasts(),  othercontig.getColors(), 10, 0.2);
						Tools_Image.image2File("c2", c2, "png");
						BufferedImage c3 = Tools_Image.simpleMerge(c1, contigxt.getOffset(), c2, othercontig.getOffset(),10, Tools_RoughImages.background, Tools_RoughImages.defaultBGR);
						Tools_Image.image2File("c3", c3, "png");
						builder.drawBufferedImage(c3);
						//builder.writeLines("Figure " + mcount + " shows the "+contignames[0]+" from the "+assembler1+" aligned horizontally with the most similar contig, in this case "
//								+topcontigname[0]+" from "+assembler2+
//								", based on comparing reads used in the assembly." +
//								". They have been labbelled with Read Names. Green" +
//								" bars represent reads shared by both contigs. " +
//								"Yellow bars represent reads which are present in the other assembly, but not in the top paired contig (the one in the figure). " +
//								"Red bars represent reads which were not included in the " +assembler2 +" assembly", 8);
					}
				 }
			 }
		} 
		catch (IOException e1) {
			logger.error("Failed to start pdfbuilder", e1);
			return;
		}
		if(mcount == contignames.length)logger.debug("Dealt with " + mcount + " of " + contignames.length);
		else{
			logger.error("Failed to deal with all contigs " +(contignames.length-mcount) + " of " + contignames.length + " failed");
			logger.info("Make sure contig names are the same as appear in the assembly ace file");
		}
		
		try{
			builder.save(output+".pdf");
		}
		catch(IOException io){
			logger.error("Failed to save pdf", io);
		} 
		catch (COSVisitorException e) {
			logger.error("Failed to save pdf", e);
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public String[] writeStats(ContigXT main, ContigXT other, String assembler1, String assembler2){
		ArrayList<String> towrite = new ArrayList<String>();
		int[] uniqs = main.getUniqs();
		int[] sizes = main.getSizes();
		
		towrite.add(new String("Reads within "+main.getName() + " are included within (Show contigs >"+contigcutoff*100+"% match):"));
		towrite.add("");
		String top = getStats(uniqs, sizes, towrite, assembler2);
		towrite.add("");
		if(other.getReadCount() == 0){
			towrite.add("No Contig found in " + assembler2);
		}
		else{
			towrite.add(new String("Reads within the top contig : "+top + " match reads in these contigs from " + assembler1));
			towrite.add("");
			uniqs = other.getUniqs();
			sizes = other.getSizes();
			getStats(uniqs, sizes, towrite, assembler1);
			towrite.add("");
		}
		return towrite.toArray(new String[0]);
	}
	
	public String getStats(int[] uniqs, int[] sizes, ArrayList<String> towrite, String assembler2){
		double d = 0;
		double total = (double)Tools_Math.sum(sizes);
		String top = "";
		for(int i =uniqs.length-1 ; i >-1; i--){
			d = (double)sizes[i]/total;
			if(i ==uniqs.length-1){
				String[] names =  bs.getBioEntryNames(manager.getCon(), uniqs[i]);
				if(names[0] != null){
					towrite.add("--"+(uniqs.length-i)+") " +names[0] + " - " + assembler2 + " : " + sizes[i]+ " reads ("+ Tools_Math.round(d*100,2)+"%)");
				}
				else{
					towrite.add("--"+(uniqs.length-i)+") Unmatched : " + sizes[i]+ " reads ("+ Tools_Math.round(d*100,2)+"%)");
				}
				top = names[0];
			}
			else if(d > contigcutoff){
				String[] names =  bs.getBioEntryNames(manager.getCon(), uniqs[i]);
				if(names[0] != null){
					towrite.add("--"+(uniqs.length-i)+") " +names[0] + " - " + assembler2 + " : " + sizes[i]+ " reads ("+ Tools_Math.round(d*100,2)+"%)");
				}
				else{
					towrite.add("--"+(uniqs.length-i)+") Unmatched : " + sizes[i]+ " reads ("+ Tools_Math.round(d*100,2)+"%)");
				}
			}
		}
		return top;
	}
		
	public ContigXT getTopContigAndColors(ContigXT xt, String division){
		if(xt == null){
			logger.error("Why the fuck are you null!?");
		}
		int[] rids = xt.getReadIDs();
		for(int t =0; t < rids.length; t++){
			int j = bsxt.getContigFromRead(manager.getCon(), rids[t], division);
			if(j > -2){
				xt.setContig(t, j);
			}
			else logger.error("Error!!!");//TODO better error message
		}
		
		xt.initColors();
		
		return xt;
	}
	
	public HashMap<String, String> mapFiles(HashMap<String, String> contig2file, File b, String bf, String division){
		boolean gotmap = false;
		if(mapman.hasMap(division, b.getPath())){
			logger.debug("Has previous map, Loading...");
			try{
				contig2file =mapman.getMap(division, b.getPath());
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
				mapman.addMap(division, b.getPath(), finalmap);
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
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
	public void pushDebug(String s){
		if(debugfile == null){
			debugfile= new File("contig_compare_debug.txt");
			if(debugfile.exists())debugfile.delete();
			Tools_File.quickWrite("--New Debug File--", debugfile, true);
		}
		Tools_File.quickWrite(s+Tools_System.getNewline(), debugfile, true);
	}
	
}
