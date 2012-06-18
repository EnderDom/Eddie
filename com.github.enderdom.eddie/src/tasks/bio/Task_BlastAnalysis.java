package tasks.bio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import bio.fasta.Fasta;
import bio.fasta.FastaParser;
import bio.xml.XML_Blastx;

import tasks.MapManager;
import tasks.TaskXTwIO;
import tools.Tools_File;
import tools.Tools_String;
import tools.Tools_System;
import ui.UI;

public class Task_BlastAnalysis extends TaskXTwIO{
	
	private String blastfolders; //Path containing the blast files
	HashMap<String, String>contig2file;
	MapManager mapman;
	UI ui;
	double e;
	
	public Task_BlastAnalysis(){
		e = 1e-6;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))blastfolders=cmd.getOptionValue("b");
		if(cmd.hasOption("e")){
			Double d = Tools_String.parseString2Double(cmd.getOptionValue("e"));
			if(d !=null)e=d;
		}
	}
	
	public void parseOpts(Properties props){
		logger.trace("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "blastfolder", true, "folder containing individual blast XMLs (MultiBlast not supported yet)"));
		options.getOption("i").setDescription("Input assembly file (only ACE currently)");
		options.addOption(new Option("e", "evalue", true, "Ignore any blasts with min evalue above this threshold"));
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
		logger.trace("Checking files...");
		if(in.isFile() && blastfolder.isDirectory()){
		if(filetype == null)filetype = this.detectFileType(in.getName());
			if(!filetype.equals("FASTA")){
				logger.error("File does not have expected suffix '.fasta' or '.fna'");
			}
			logger.trace("Parsing Setting Fastas...");
			Fasta fasta = new Fasta();
			FastaParser parse = new FastaParser(fasta);
			int count=0;
			try{
				logger.debug("Parsing Fasta");
				//parse.setShorttitles(true);
				parse.parseFasta(in);
				logger.debug("Fasta Parsed");
				this.contig2file = fasta.getSequences();
				for(String name : contig2file.keySet()){
					contig2file.put(name, name+".xml");
					count++;
					System.out.print("\r(No."+count+") : " + name + "        ");
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
					logger.info("Next Stage of Blast Analysis...");
					int contigswithblasts = 0;
					int blast10 = 0;
					int avcov=0;
					count=0;
					XML_Blastx xml = null;
					for(String contig : contig2file.keySet()){
						try{
							xml = new XML_Blastx(contig2file.get(contig));
							if(xml.getNoOfHits() > 0){
								if(xml.getLowestEValue() < this.e){
									contigswithblasts++;
									avcov+=xml.getLargestRange();
									if(xml.getNoOfHits() > 9){
										blast10++;
									}
								}
							}
						}
						catch (Exception e) {
							logger.error("Failed to parse blast xml for " + contig , e);
						}
						System.out.print("\r(No."+count+") : " + contig + "        ");
						count++;
					}
					System.out.println();
					System.out.println("--STATS--");
					System.out.println("Sequences with blast results: " + contigswithblasts);
					System.out.println("Sequences >9 blast results: " + blast10);
					System.out.println("No. of bp matched: "+avcov+"bp");
					System.out.println("---------");
				}
			}
			catch(IOException io){
				logger.error("Cannot parse fasta file", io);
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
