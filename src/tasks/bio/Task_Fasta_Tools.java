package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import bio.fasta.Fasta;
import bio.fasta.FastaParser;

import tasks.TaskXTwIO;
import tools.Tools_String;
import tools.Tools_System;

public class Task_Fasta_Tools extends TaskXTwIO{
	
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
			output = output.replaceAll(".fasta", "");
			output = output.replaceAll(".fastq", "");
			output = output.replaceAll(".fna", "");
			output = output.replaceAll(".qual", "");
			File infile = new File(this.input);
			File qualfile = new File(this.qual);
			File outfile =  new File(output+".fastq");
			if(infile.exists()){
				logger.debug("Input File is good");
				/*
				 * Convert Fasta And Qual 2 Fastq
				 */
				if(qualfile.exists()){
					fasta = new Fasta();
					fasta.setFastq(false);
					FastaParser parser = new FastaParser(fasta);
					try {
						parser.parseFasta(infile);
						parser.parseQual(qualfile);	
						Logger.getRootLogger().debug("Fasta Parsed, saving...");
						subRun();
						if(!NoOut){
							if(trim){ 
								outfile =  new File(output+".fasta");
								File outfile2 =  new File(output+".qual");
								fasta.save2FastaAndQual(outfile, outfile2);
							}
							else fasta.save2Fastq(outfile);
						}
					}
					catch (Exception e) {
						Logger.getRootLogger().error("Error parsing Fastq file", e);
					}
				}
				/*
				 * Convert Fastq to Fasta And Qual
				 */
				else{
					File outfile2 =  new File(output+".qual");
					outfile =new File(output+".fasta");
					if((!outfile.exists() && !outfile2.exists()) || overwrite){
						fasta = new Fasta();
						fasta.setFastq(true);
						FastaParser parser = new FastaParser(fasta);
						try {
							parser.parseFastq(infile);
							Logger.getRootLogger().debug("Fastq Parsed, saving...");
							subRun();
							if(!NoOut){
								if(!trim) fasta.save2FastaAndQual(outfile, outfile2);
								else{ 
									outfile = new File(output+".fastq");
									fasta.save2Fastq(outfile);
								}
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
			logger.warn("No action actually set");
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
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
		else if(cmd.hasOption("convert")){
			
			this.NoOut = false;
		}
		else{
			logger.warn("No Actual options set");
			this.NoOut = true;
			this.NoContinual = true;
		}
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
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta file");
		options.addOption(new Option("q", "qual", true, "Optional quality file for convert fasta & qual -> fastq"));
		options.getOption("o").setDescription("Output file or files");
		options.addOption(new Option("trim", true, "Trim Sequences Using below this value ie -trim 100"));
		options.addOption(new Option("stats", false, "Print Statistics for Fasta/q files"));
		options.addOption(new Option("convert", false, "Convert files to another file type"));
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
