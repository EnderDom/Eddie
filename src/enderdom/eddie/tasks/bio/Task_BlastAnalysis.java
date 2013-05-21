package enderdom.eddie.tasks.bio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.homology.blast.BlastObject;
import enderdom.eddie.bio.homology.blast.BlastxDocumentParser;
import enderdom.eddie.bio.homology.blast.MultiblastParser;
import enderdom.eddie.bio.lists.Fasta;
import enderdom.eddie.bio.lists.FastaParser;

import enderdom.eddie.tasks.MapManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_File;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.UI;

@SuppressWarnings("deprecation")
public class Task_BlastAnalysis extends TaskXTwIO{
	
	private String blastfolders; //Path containing the blast files
	private HashMap<String, String>contig2file;
	private MapManager mapman;
	private UI ui;
	double e;
	private boolean contigblast;
	private boolean blastonly;
	
	public Task_BlastAnalysis(){
		e = 1e-3;
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("b"))blastfolders=cmd.getOptionValue("b");
		if(cmd.hasOption("e")){
			Double d = Tools_String.parseString2Double(cmd.getOptionValue("e"));
			if(d !=null)e=d;
		}
		if(cmd.hasOption("c"))contigblast = true;
		if(cmd.hasOption("x"))blastonly = true;
	}
	
	public void parseOpts(Properties props){
		logger.trace("Parse Options From props");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("b", "blastfolder", true, "folder containing individual blast XMLs (MultiBlast not supported yet)"));
		options.getOption("i").setDescription("Input assembly file (only Fasta currently)");
		options.addOption(new Option("e", "evalue", true, "Ignore any blasts with min evalue above this threshold"));
		options.addOption(new Option("c", "contigs", false, "Analysis with contigs"));
		options.addOption(new Option("x", "blastonly", false, "Only Analyse blast files"));
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
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(contigblast){
			init();
			File in = new File(input);
			File blastfolder = new File(blastfolders);
			logger.trace("Checking files...");
			if(in.isFile() && blastfolder.isDirectory()){
				if(filetype == null){
					filetype = this.detectFileType(in.getName());
				}
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
						int hits =0;
						int hsps = 0;
						count=0;
						BlastObject xml = null;
						for(String contig : contig2file.keySet()){
							try{
								xml = new BlastxDocumentParser(contig2file.get(contig)).getBlastObject();
								if(xml.getNoOfHits() > 0){
									if(xml.getLowestEValue() < this.e){
										contigswithblasts++;
										avcov+=xml.getLargestRange();
										if(xml.getNoOfHits() > 9){
											blast10++;
										}
										int[] hitcount = xml.getNumberofHitsBelow(this.e);
										hits+=hitcount[0];
										hsps+=hitcount[1];
									}
								}
							}
							catch (Exception e) {
								logger.error("Failed to parse blast xml for " + contig , e);
							}
							System.out.print("\r(No."+count+") : " + contig + "        ");
							count++;
						}
						double ratio = ((double)((int)(((double)hsps/(double)hits)*100)))/100;
						System.out.println();
						System.out.println("--STATS--");
						System.out.println("Sequences with blast results: " + contigswithblasts);
						System.out.println("Sequences >9 blast results: " + blast10);
						System.out.println("No. of bp matched: "+avcov+"bp");
						System.out.println("Hit to Hsp Ratio: "+ratio);
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
		}
		else if(blastonly){
			File blastfolder = new File(blastfolders);
			int c =0;
			int c2=0;
			int l=0;
			int hsps = 0;
			int hits =0;
			if(blastfolder.isDirectory()){
				File[] files = blastfolder.listFiles();
				l = files.length;
				for(File file : files){
					try{
						MultiblastParser parser = new MultiblastParser(MultiblastParser.BASICBLAST, file);
						while (parser.hasNext()){
							BlastObject xml = parser.next();
							double ee = xml.getLowestEValue();
							if(ee < this.e && ee != -1){
								c2++;
							}
							else{
								c++;
							}
							hits+=xml.getNoOfHits();
							for(int i=1;i < xml.getNoOfHits()+1;i++)hsps+=xml.getNoOfHsps(i);
							System.out.print("\r"+ (c2+c) + " of " +l);
						}
					}
					catch(Exception e){
						logger.error("Failed to parse blast file '"+ file.getName()+"'", e);
					}
				}
				System.out.println("");
				System.out.println("--STATS--");
				System.out.println("Blast below " +this.e+": " + c2);
				System.out.println("Blast above " +this.e+": " + c);
				System.out.println("Hits Total " +hits);
				System.out.println("Hsps Total " +hsps);
			}
			else{
				logger.error("Blast folder should be a directory");
			}
		}
		else{
			logger.info("Select an option from the menu");
			printHelpMessage();
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
}
