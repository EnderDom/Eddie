package tasks.bio.fasta;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.Tools_System;

public class Task_Fasta extends TaskXT{
	
	protected String qual;
	
	public Task_Fasta(){
		
	}
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running task @ "+Tools_System.getDateNow());
		
		/*
		 * Convert Fasta&qual to Fastq
		 */
		
		if(isOverwrite() || getFile(getOutput(), 0) != null){
			if(getQual() != null){
				System.out.println("//TODO");
			}
			/*
			 * Convert Fastq to Fasta And Qual
			 */
			else{
				System.out.println("//TODO");
			}
		}
		else{
			Logger.getRootLogger().error("File " + output + " already exists, change filepath or set overwrite to true");
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
