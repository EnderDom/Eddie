package tasks.bio;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;


import bio.assembly.ACEObject;
import bio.assembly.ACEParser;
import bio.fasta.Fasta;
import bio.fasta.FastaParser;
import bio.sequence.Sequences;

import tasks.TaskXTwIO;
import tools.Tools_System;


@SuppressWarnings("deprecation")
public class Task_Sequence_Analysis extends TaskXTwIO{
	
	protected String qual;
	Sequences seqs;
	
	public Task_Sequence_Analysis(){
		setHelpHeader("--This is the Help Message for the Sequence Analysis Task--");
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(input != null){
			File in = new File(input);
			if(in.isFile()){
				if(filetype == null)filetype = this.detectFileType(in.getName());
				boolean cont = false;
				if(filetype.equals("FASTQ") || filetype.equals("FASTA")){
					Fasta fasta = new Fasta();
					if(filetype.equals("FASTQ"))fasta.setFastq(true);
					FastaParser parser = new FastaParser(fasta);
					try {
						if(filetype.contains("FASTQ"))parser.parseFastq(in);
						if(filetype.contains("FASTA"))parser.parseFasta(in);
						Logger.getRootLogger().debug("File Parsed");
						seqs = fasta;
						cont = true;
					}
					catch (Exception e) {
						logger.error("Error parsing Fasta/q file", e);
					}
				}
				else if(filetype.equals("ACE")){
					ACEObject obj = new ACEObject();			
					ACEParser parser = new ACEParser(obj);
					try{
						parser.parseAce(in);
						Logger.getRootLogger().debug("File Parsed");
						seqs = obj;
						
						cont = true;
					}
					catch (Exception e) {
						logger.error("Error parsing ACE file", e);
					}
				}
				if(cont){
					System.out.println("--Sequence Analysis--");
					System.out.println("No. of Seqs: " + seqs.getNoOfSequences());
					long[] stats = seqs.getAllStats();
					System.out.println("No. of bp: " + stats[0] + "bp");
					System.out.println("Min Sequence Length: " + stats[1] + "bp");
					System.out.println("Max Sequence Length: " + stats[2] + "bp");
					System.out.println("n50: " + stats[3]);
					System.out.println("n90: " + stats[4]);
					System.out.println("Sequences >500bp " + stats[5]);
					System.out.println("Sequences >1Kb " + stats[6]);
				}
			}
			else{
				logger.error("Input not a file");
			}
		}
		else{
			logger.error("Input not set");
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
	}
	
	
	public void parseOpts(Properties props){
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.getOption("i").setDescription("Input fasta/ace file");
	}
	
	public Options getOptions(){
		return this.options;
	}

}
