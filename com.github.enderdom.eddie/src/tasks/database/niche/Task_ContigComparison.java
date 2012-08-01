package tasks.database.niche;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import bio.objects.maps.Blast2BlastMap;
import bio.objects.maps.Contig2ContigMap;

import databases.bioSQL.interfaces.BioSQL;
import databases.bioSQL.interfaces.BioSQLExtended;
import databases.manager.DatabaseManager;


import output.generic.Report;

import tasks.MapManager;
import tasks.Task;
import tools.Tools_Array;
import tools.Tools_File;
import tools.Tools_String;
import tools.Tools_System;
import tools.bio.Tools_Assembly;

import ui.UI;

public class Task_ContigComparison extends Task{

	public String outformat;
	Logger logger= Logger.getRootLogger();
	private HashMap<String, String> contig2file1;
	private HashMap<String, String> contig2file2;
	private String blastfolder1;
	private String blastfolder2;
	private String output;
	public MapManager mapman;
	private UI ui;
	private int database_id;
	private String[] contignames;
	private BioSQLExtended bsxt; 
	private BioSQL bs;
	private int run_id1;
	private int run_id2;
	private String prg1;
	private String prg2;
	
	private static double contigcutoff = 0.0;
	
	
	public Task_ContigComparison(){
		setHelpHeader("--This is the Help Message for the ContigComparison Task--");
		outformat = "PDF";
		run_id1 = -1;
		run_id2 = -1;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		//if(cmd.hasOption("f"))outformat=cmd.getOptionValue("f");
		if(cmd.hasOption("o"))output = cmd.getOptionValue("o");
		if(cmd.hasOption("b1"))blastfolder1 = cmd.getOptionValue("b1");
		if(cmd.hasOption("b2"))blastfolder2 = cmd.getOptionValue("b2");
		if(cmd.hasOption("p1"))prg1 = cmd.getOptionValue("p1");
		if(cmd.hasOption("p2"))prg2 = cmd.getOptionValue("p2");
		if(cmd.hasOption("r1")){
			Integer rr1 = Tools_String.parseString2Int(cmd.getOptionValue("r1"));
			run_id1 = (rr1 != null) ? rr1 : -1;
		}
		if(cmd.hasOption("r2")){
			Integer rr2 = Tools_String.parseString2Int(cmd.getOptionValue("r1"));
			run_id2 = (rr2 != null) ? rr2 : -1;
		}
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
		
		options.addOption(new Option("i","input", true, "List of contigs to report (Assumed division 1)," +
				" as is in ACE file, separated by newline"));
		options.addOption(new Option("c","contig", true, "Name of contig as is in ace file, alternative to input"));
		options.addOption(new Option("b1","blast1", true, "Blast folder for the first input"));
		options.addOption(new Option("b2","blast2", true, "Blast folder for the second input"));
		options.addOption(new Option("r1","run_id1", true, "Force Run Id if know, else will be asked if there are multiple runs with program"));
		options.addOption(new Option("r2","run_id2", true, "Force Run Id if know, else will be asked if there are multiple runs with program"));
		options.addOption(new Option("p1","prog1", true, "Program used for assembly 1"));
		options.addOption(new Option("p2","prog2", true, "Program used for assembly 2"));
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
		DatabaseManager manager = this.ui.getDatabaseManager(password);
		if(!manager.open()){
			logger.error("Could not open a connection to the database");
			return;
		}
		mapman = new MapManager(ui.getPropertyLoader());
		logger.info("Checks complete, Continuing...");
		database_id = manager.getEddieDBID();
		bsxt = manager.getBioSQLXT();
		bs = manager.getBioSQL();
		
		if(run_id1 == -1 || run_id2 == -1){
			run_id1 = Tools_Assembly.getSingleRunId(manager, prg1, BioSQLExtended.assembly);
			run_id2 = Tools_Assembly.getSingleRunId(manager, prg2,BioSQLExtended.assembly);
		}
		
		String assembler1 = bsxt.getRun(manager, run_id1).getProgram();
		String assembler2 = bsxt.getRun(manager, run_id2).getProgram();
		
		logger.debug("Database ID: " + this.database_id);
		logger.info("Starting Mapping Sequences to blasts");
		logger.debug("Checking for previous map");

		//Holds the actual name as is in file (ACE) record
		HashMap<String, String> name2id1 = bsxt.getContigNameNIdentifier(manager, run_id1);
		HashMap<String, String> name2id2 = bsxt.getContigNameNIdentifier(manager, run_id2);
		
		//Holds the Contig id 2 blast file for division 1
		contig2file1 = mapFiles(name2id1, b1, blastfolder1, run_id1, assembler1);
		if(contig2file1 == null){
			logger.error("Failed to map files for run id " + assembler1);
			return;
		}
		//Holds the Contig id 2 blast file for division 2
		contig2file2 = mapFiles(name2id2, b2, blastfolder2, run_id2, assembler2);
		if(contig2file2 == null){
			logger.error("Failed to map files for run id " + assembler2);
			return;
		}
	
		
		Report report = new Report();
		int mcount =0;
		Blast2BlastMap blastmap = null;
		Contig2ContigMap contigmap = null;
		Contig2ContigMap othercontig = null;
		try {
			if(!report.setFileAndType(this.output, Report.OUT_HTML)) {
				logger.error("Failed to build Report Object");
				return;
			}
			for(String s : name2id1.keySet()){
				for(int i =0 ;i < contignames.length; i++){
					if(s.equals(contignames[i])){
						mcount++;
						logger.debug("Developing Contig " + contignames[i]);
						
						contigmap = new Contig2ContigMap(run_id2);
						//Drawing Header
						report.addHeader(contignames[i] +" - "+ assembler1);
						//Building Map
						logger.debug("Building Contig Read Map and Mapping to " + assembler2 +" contigs");
						if(!contigmap.buildMap(manager, name2id1.get(s))){
							logger.error("Failed to build contig map");
							return;
						}
						else{
							int[][] contigs = contigmap.getContigsAboveThreshold(contigcutoff);
							report.addAsOneParagraph(getStats(manager, contigs, assembler2, contigmap, contigmap.getTopRealContig()));
						}
						
						logger.debug("Identifying Top Contig And generating Map");
						int otherid = contigmap.getTopRealContig();
						String[] othercontignames = new String[3];
						if(otherid != -1){
							logger.debug("Building Other Contig");
							othercontig = new Contig2ContigMap(run_id1);
							othercontignames = bs.getBioEntryNames(manager.getCon(), otherid);
							othercontig.setContigName(othercontignames[0]);
							logger.debug("Top Match is "+ othercontignames[0]);
							othercontig.buildMap(manager, otherid);
							if(!contigmap.validateMap(othercontig)){
								logger.error("Mapping has failed to correctly work for"+contigmap.getContigName() 
										+" and  "+othercontig.getContigName()+". Please Report.");
							}
							int[][] contigs = othercontig.getContigsAboveThreshold(contigcutoff);
							logger.debug("Reporting " + contigs[0].length+ " Contigs matched via reads");
							report.addAsOneParagraph("Comparison with " + othercontig.getContigName() + " - " +assembler2);
							report.addAsOneParagraph(getStats(manager, contigs, assembler1, othercontig, contigmap.getContigId()));
							logger.debug("Contig2Contig Mapping Stage is complete.");
							logger.debug("Starting Blast Mapping");
							blastmap = new Blast2BlastMap(new File(contig2file1.get(name2id1.get(s))), new File(contig2file2.get(othercontignames[2])));
							blastmap.limit(50, 1e-6);
							if(!blastmap.build()){
								logger.error("Error Building map");
							}
							logger.debug("Blast Map Built");
							int over = blastmap.getAccessionOverlap();
							logger.debug("Overlapping Accessions: " + over);
							int[] hits = blastmap.getNoOfHits();
							logger.debug("From " + hits[0] + " hits in " + contigmap.getContigName());
							logger.debug("From " + hits[1] + " hits in " + othercontig.getContigName());
							if(hits[0] > 0 && hits[1] > 0){
								hits[0] = (int)(((double)over/(double)hits[0])*100);
								hits[1] = (int)(((double)over/(double)hits[1])*100);
								logger.debug("Calculating Overlap...");
								report.addAsOneParagraph("Number of Matching Accessions : "+over); 
								report.addAsOneParagraph(contigmap.getContigName() + "("+hits[0]+"% of hits)  --  " + othercontig.getContigName() + "("+hits[1]+"% of hits)");
								logger.debug("Getting Top hits for contig " + contigmap.getContigName());
								String[] hit_defs = blastmap.getTopXHitDefs4one(3);
								for(int h =0 ; h < hit_defs.length; h++){
									hit_defs[h] = "Hit No."+(h+1)+") " + hit_defs[h];
									if(hit_defs[h].length() > 40)hit_defs[h]=hit_defs[h].substring(0, 40);
									
								}
								logger.debug("Reporting...");
								report.addAsOneParagraph(Tools_Array.mergeStrings(new String[]{"TOP "+(hit_defs.length+1)+
										" Blast Hits from "+contigmap.getContigName() + " - "+ assembler1}, hit_defs));
								
								logger.debug("Getting Top hits for contig " + othercontig.getContigName());
								hit_defs = blastmap.getTopXHitDefs4two(3);
								for(int h =0 ; h < hit_defs.length; h++){
									hit_defs[h] = "Hit No."+(h+1)+") " + hit_defs[h];
									if(hit_defs[h].length() > 40)hit_defs[h]=hit_defs[h].substring(0, 40);
								}
								logger.debug("Reporting...");
								report.addAsOneParagraph(Tools_Array.mergeStrings(new String[]{"TOP "+(hit_defs.length+1)+
										" Blast Hits from "+othercontig.getContigName() + " - "+ assembler2}, hit_defs));
								logger.debug("All work done for this contig");
							}
							else{
								report.addAsOneParagraph("One or both Contigs do not have Blast Hits to Compare");
							}
						}
						else{
							logger.info("No Contig matches " + contignames[i]);
							report.addAsOneParagraph("Contig Does not match any contigs from " + assembler2);
						}
					}
				 }
			 }
			report.saveAndClose();
		} 
		catch (Exception e1) {
			logger.error("Failed to map contigs", e1);
			return;
		}
		if(mcount == contignames.length)logger.debug("Dealt with " + mcount + " of " + contignames.length);
		else{
			logger.error("Failed to deal with all contigs " +(contignames.length-mcount) + " of " + contignames.length + " failed");
			logger.info("Make sure contig names are the same as appear in the assembly ace file");
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	
	public String[] getStats(DatabaseManager manager, int[][] contigs, String assembler, Contig2ContigMap map, int topmatch){
		String[] contigdescript = new String[contigs[0].length];
		for(int ind = 0; ind < contigs[0].length ; ind++){
			if(contigs[0][ind] == -1){
				contigdescript[ind] = " Unmatched "+" (" + contigs[1][ind] + "% of "+map.getNoOfReads()+" reads)";
			}
			else{
				String name = bs.getBioEntryNames(manager.getCon(), contigs[0][ind])[0];
				if(contigs[0][ind] == topmatch)name="<b>"+name+"</b>";
				if(name != null){
					contigdescript[ind] = name + " - " + assembler + " ("+contigs[1][ind]+"% of "+map.getNoOfReads()+" reads)";
				}
				else{
					contigdescript[ind] = "ERROR: SOMETHING TERRIBLE HAPPENED HERE";
					logger.error("Failure to retrieve name from database");
				}
			}
		}
		return contigdescript;
	}
	
	
	public HashMap<String, String> mapFiles(HashMap<String, String> contig2file, File b, String bf, int run_id, String assembler){
		boolean gotmap = false;
		if(mapman.hasMap(run_id+"", b.getPath())){
			logger.debug("Has previous map, Loading...");
			try{
				contig2file =mapman.getMap(run_id+"", b.getPath());
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
				logger.error("No sequences with division "+ run_id);
				return null;
			}
			else{
				logger.debug("Found some "+ contig2file.size() + " for records for "+run_id);
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
				mapman.addMap(run_id+"", b.getPath(), finalmap);
				map = null;
				return finalmap;
			}
		}
		else{
			return contig2file;
		}
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
}
