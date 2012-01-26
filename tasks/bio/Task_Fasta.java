package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import bio.fasta.Fasta;
import bio.fasta.FastaParser;

import tasks.TaskXT;
import tools.Tools_System;

public class Task_Fasta extends TaskXT{
	
	protected String qual;
	Fasta fasta;
	
	public Task_Fasta(){
		
	}
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running task @ "+Tools_System.getDateNow());
		
		/*
		 * Convert Fasta&qual to Fastq
		 */
		output = output.replaceAll(".fasta", "");
		output = output.replaceAll(".fastq", "");
		output = output.replaceAll(".fna", "");
		output = output.replaceAll(".qual", "");
		File infile = null;
		File qualfile = null;
		File outfile = null;
		if((infile=getFile(input, IS_FILE)) != null){//This is getting a bit messy...
			Logger.getRootLogger().debug("Input File is good");
			/*
			 * Convert Fasta And Qual 2 Fastq
			 */
			if(getQual() != null){
				Logger.getRootLogger().debug("Set to Converting fasta 2 fastq");
				if((qualfile = getFile(getQual(), IS_FILE)) !=null){
					if(isOverwrite() || (outfile = getFile(output+".fastq", NOT_FILE_OR_DIRECTORY)) != null){
						fasta = new Fasta();
						fasta.setFastq(false);
						FastaParser parser = new FastaParser(fasta);
						try {
							parser.parseFasta(infile);
							parser.parseQual(qualfile);	
							Logger.getRootLogger().debug("Fasta Parsed, saving...");
							fasta.save2Fastq(outfile);
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
			/*
			 * Convert Fastq to Fasta And Qual
			 */
			else{
				Logger.getRootLogger().debug("Set to Converting fastq 2 fasta and qual");
				File outfile2 = null;
				if(isOverwrite() || ( (outfile = getFile(output+".fasta", NOT_FILE_OR_DIRECTORY)) != null || (outfile2 = getFile(output+".qual", NOT_FILE_OR_DIRECTORY)) != null)){
					fasta = new Fasta();
					fasta.setFastq(true);
					FastaParser parser = new FastaParser(fasta);
					try {
						parser.parseFastq(infile);
						Logger.getRootLogger().debug("Fastq Parsed, saving...");
						fasta.save2FastaAndQual(outfile, outfile2);
					}
					catch (Exception e) {
						Logger.getRootLogger().error("Error parsing Fastq file", e);
					}
				}
			}
		}
		else{
			Logger.getRootLogger().error("Input file is set, but filepath: "+input+" is not a valid file");	
		}
		Logger.getRootLogger().debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("qual")){
			qual = cmd.getOptionValue("qual");
		}
	}
	
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta file");
		options.addOption(new Option("q", "qual", true, "Optional quality file for convert fasta & qual -> fastq"));
		options.getOption("o").setDescription("Output file or files");		
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
