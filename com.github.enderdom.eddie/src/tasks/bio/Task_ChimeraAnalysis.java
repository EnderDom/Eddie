package tasks.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import bio.assembly.ACEFileParser;
import bio.assembly.ACERecord;
import bio.xml.XML_Blastx;

import tasks.MapManager;
import tasks.TaskXTwIO;
import tools.Tools_File;
import tools.Tools_System;
import ui.UI;

public class Task_ChimeraAnalysis extends TaskXTwIO{
	
	private String blastfolders; //Path containing the blast files
	HashMap<String, String>contig2file;
	MapManager mapman;
	UI ui;
	
	public Task_ChimeraAnalysis(){
		
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))blastfolders=cmd.getOptionValue("b");
	}
	
	public void parseOpts(Properties props){
		logger.trace("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "blastfolder", true, "folder containing individual blast XMLs (MultiBlast not supported yet)"));
		options.getOption("i").setDescription("Input assembly file (only ACE currently)");
	}
	
	/*
	 * Call this in run() else there'll be trouble
	 * 
	 * Can't init till after constructor (as UI is added via auxilliary method)
	 * due to the incredibly akward setup I have for added extra tasks to the module.
	 * 
	 */
	public void init(){
		this.contig2file = new HashMap<String, String>();
		mapman = new MapManager(ui.getPropertyLoader());
	}
	
	public void run(){
		setComplete(started);
		init();
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		File in = new File(input);
		File blastfolder = new File(blastfolders);
		if(in.isFile() && blastfolder.isDirectory()){
		if(filetype == null)filetype = this.detectFileType(in.getName());
			if(!filetype.equals("ACE")){
				logger.warn("File does not have expected suffix '.ace'");
			}
			try{
				ACEFileParser parse = new ACEFileParser(new FileInputStream(in));
				int count=0;
				while(parse.hasNext()){
					ACERecord record = parse.next();
					this.contig2file.put(record.getContigName(), record.getContigName()+".xml");
					System.out.print("\r(No."+count+") : " + record.getContigName() + "        ");
					count++;
				}
				System.out.println();
				if(mapman.hasMap(input, blastfolders)){
					logger.info("Contigs have already been mapped to blast, excellent.");
					this.contig2file = mapman.getMap(input, blastfolders);
					if(contig2file == null){
						logger.error("Spoke too soon, somethings gone wrong... rebuilding map from scratch :(");
						this.contig2file= Tools_File.mapFiles(contig2file, blastfolder);
						logger.info("Saving Assembly2Blast Map...");
						mapman.addMap(input, blastfolders, this.contig2file);
					}
				}
				else{
					logger.info("Mapping Contigs to Blast files... Please wait...");
					this.contig2file= Tools_File.mapFiles(contig2file, blastfolder);
					logger.info("Saving Assembly2Blast Map...");
					mapman.addMap(input, blastfolders, this.contig2file);
				}
				if(this.contig2file == null){
					logger.error("Map could not be built for whatever reason. Please rename the blast files sensibly (ie the same as contig names ?)");
				}
				else{					
					logger.info("Next Stage of Chimera Analysis...");
					logger.info("Code Pending...");
					logger.info("As in, I haven't written it yet");
					parse = new ACEFileParser(new FileInputStream(in));
					
					while(parse.hasNext()){
						ACERecord record = parse.next();
						System.out.println("Getting depth Map");
						int[] depths = record.getDepthMap();
						System.out.println("Done");
						File file = new File(contig2file.get(record.getContigName()));
						String rec = record.getConsensusAsString();
						for(int i =0; i < depths.length; i++){
							System.out.print(rec.charAt(i)+",");
						}
						System.out.println();
						for(int i =0; i < depths.length; i++){
							System.out.print(depths[i]+",");
						}
						System.out.println();
						try{
							XML_Blastx xml = new XML_Blastx(file);
							for(int i =0; i < xml.getNoOfHits(); i++){
								
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}
			}
			catch(IOException e){
				logger.error("Failed to parse ACE file properly",e);
			}
		}
		else{
			if(!in.isFile()){
				logger.error("Input specified is not a file");
			}
			if(!blastfolder.isDirectory()){
				logger.error("Blastfolder is not a directory");
			}
			printHelpMessage();
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
}
