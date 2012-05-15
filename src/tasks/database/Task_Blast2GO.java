package tasks.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import tasks.TaskXT;
import tools.Tools_System;


public class Task_Blast2GO extends TaskXT{

	private boolean upload;
	
	public Task_Blast2GO(){
		setHelpHeader("--This is the Help Message for the the blast2go Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("upload"))upload=true;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("upload", false, "Perform default setup"));
	}
	
	public Options getOptions(){
		return this.options;
	}

	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(upload){
			logger.info("Task is not complete");
		}
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
}
