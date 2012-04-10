package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.Tools_String;
import tools.Tools_System;
import bio.fasta.Fasta;
import bio.fasta.FastaParser;

public class Task_Fasta_Tools extends TaskXT{
	
	protected String qual;
	Fasta fasta;
	private boolean trim;
	private boolean stats;
	private int trimlen;
	private boolean NoOut;
	private boolean NoContinual;
	
	public Task_Fasta_Tools(){
		
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		/*
		 * Convert Fasta&qual to Fastq
		 */
		if(!NoContinual){
			if(output != null){
				output = output.replaceAll(".fasta", "");
				output = output.replaceAll(".fastq", "");
				output = output.replaceAll(".fna", "");
				output = output.replaceAll(".qual", "");
			}
			File infile = null;
			File qualfile = null;
			File outfile = null;
			if((infile=getFile(input, IS_FILE)) != null){//This is getting a bit messy...
				logger.debug("Input File is good");
				/*
				 * Convert Fasta And Qual 2 Fastq
				 */
				if(getQual() != null){
					logger.debug("Fasta/Qual");
					if((qualfile = getFile(getQual(), IS_FILE)) !=null){
						File outfile2 = getFile(output+".qual", NOT_FILE_OR_DIRECTORY);
						outfile = getFile(output+".fasta", NOT_FILE_OR_DIRECTORY);
						if(isOverwrite() || (outfile != null && outfile2 != null) || NoOut){					
							if(outfile == null)outfile = new File(output+".fasta");//Quick fix //TODO sort this out
							if(outfile2 == null)outfile2 = new File(output+".qual");//Quick fix //TODO sort this out
							fasta = new Fasta();
							fasta.setFastq(false);
							FastaParser parser = new FastaParser(fasta);
							try {
								parser.parseFasta(infile);
								parser.parseQual(qualfile);
								subRun();
								if(!NoOut)fasta.save2FastaAndQual(outfile, outfile2);
							}
							catch (Exception e) {
								Logger.getRootLogger().error("Error parsing Fastq file", e);
							}
						}
						else{
							Logger.getRootLogger().error("File " + output + " already exists, change filepath or set overwrite to true");
						}
					}
					else{
						Logger.getRootLogger().error("Quality file is set, but filepath: "+qual+" is not a valid file");
					}
				}
				else{
					String st = this.detectFileType(input);
					boolean fastq = st.equals("FASTQ");
					if(fastq)outfile = getFile(output+".fastq", NOT_FILE_OR_DIRECTORY);
					else outfile = getFile(output+".fasta", NOT_FILE_OR_DIRECTORY);
					if(isOverwrite() || outfile != null || NoOut){
						if(outfile == null)outfile = new File(output+".fasta");//Quick fix //TODO sort this out
						fasta = new Fasta();
						if(fastq)fasta.setFastq(true);
						FastaParser parser = new FastaParser(fasta);
						try {
							if(fastq)parser.parseFastq(infile);
							else parser.parseFasta(infile);
							subRun();
							if(!NoOut){
								if(fastq) fasta.save2Fastq(outfile);
								else fasta.save2Fasta(outfile);
							}
						}
						catch (Exception e) {
							logger.error("Error parsing Fastq file", e);
						}
					}
				}
			}
			else{
				logger.error("Input file is set, but filepath: "+input+" is not a valid file");	
			}
		}
		else{
			logger.warn("No actual active option set");
			this.printHelpMessage();
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void subRun(){
		if(trim){
			logger.info(" Trimming Reads...");
			int u = trimSequences();
			logger.info(u+" Sequences trimmed");
		}
		if(stats){
			logger.info("Retrieving Statistics...");
			System.out.println("Total No. of Sequences: 	" + this.fasta.getNoOfSequences());
			long[] arr = fasta.getAllStats();
			System.out.println("Total No. of Bps: 		" + arr[0]);
			System.out.println("Minumum Sequence Length: 	" + arr[1]);
			System.out.println("Maximum Sequence Length: 	" + arr[2]);
			System.out.println("N50:				" + arr[3]);
			System.out.println("N90: 				" + arr[4]);
		}
		//OTHER fasta tools
	}
		
	public int trimSequences(){
		return this.fasta.trimSequences(this.trimlen);
	}

	
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		this.NoContinual = false;
		if(cmd.hasOption("qual")){
			qual = cmd.getOptionValue("qual");
		}
		//Options 
		if(cmd.hasOption("trim")){
			trim=true;
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("trim"));
			if(i != null){
				this.trimlen = i;
			}
			else{
				logger.warn("Trim set, but is not a number");
			}
		}
		else if(cmd.hasOption("stats")){
			stats=true;
			this.NoOut = true;
		}
		else{
			logger.warn("No Actual options set");
			this.NoOut = true;
			this.NoContinual = true;
		}
	}
	
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta file");
		options.addOption(new Option("q", "qual", true, "Optional quality file for convert fasta & qual -> fastq"));
		options.getOption("o").setDescription("Output file or files");
		options.addOption(new Option("trim", true, "Trim Sequences Using below this value ie -trim 100"));
		options.addOption(new Option("stats", false, "Print Statistics for Fasta/q files"));
	}
	
	public Options getOptions(){
		return this.options;
	}

	public String getQual() {
		return qual;
	}

	public void setQual(String qual) {
		this.qual = qual;
	}
}
