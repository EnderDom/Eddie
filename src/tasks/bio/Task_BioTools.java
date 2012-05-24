package tasks.bio;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import tasks.TaskXT;
import tools.Tools_System;

public class Task_BioTools extends TaskXT{
	
	private boolean r;
	private String input;
	private String contig;
	private String sequence;
	
	public Task_BioTools(){
		setHelpHeader("--This is the Help Message for the General Bio Tools Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		if(cmd.hasOption("rc"))r = true;
		if(cmd.hasOption("i"))input = cmd.getOptionValue("i");
		if(cmd.hasOption("c"))contig = cmd.getOptionValue("c");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("rc","reversecomp", false, "Reverse & Compliment String"));
		options.addOption(new Option("s","sequence", true, "Input sequence via commandline ie -s ATCGTGCTACG"));
		options.addOption(new Option("i","input", true, "Use input file as source"));
		options.addOption(new Option("c","contig", true, "Use this string from database source"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
}
