package enderdom.eddie.tasks.bio;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;

public class Task_UniVec extends TaskXT{

	private String uni_db;
	private String blast_bin;
	private String workspace;
	private boolean create;
	
	public Task_UniVec(){
		
	}
	
	public void run(){
		setComplete(started);
		logger.debug("Started running task @ "+Tools_System.getDateNow());
		if(uni_db != null && create){
			//TODO
		}
		else if(create){
			//TODO
		}
		else if(ui.getPropertyLoader().getValue("UNI_VEC_DB") != null){
			//TODO
		}
		else{
			logger.warn("No uni-vec set, nor create is set.");
			ui.requiresUserYNI("Do you want to automatically create UniVec data?", "Create Univec Database?");
		}
		logger.debug("Finished running task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("u", "uni_db", true, "Set UniVec database location"));
		options.addOption(new Option("c", "create_db", true, "Downloads and creates the UniVec database with the makeblastdb"));
		options.addOption(new Option("bbb", "blast_bin", true, "Specify blast bin directory"));
	}
	
	public void parseOpts(Properties props){
		if(blast_bin == null){
			blast_bin = props.getProperty("BLAST_BIN_DIR");
		}
		workspace = props.getProperty("WORKSPACE");
		logger.trace("Parse Options From props");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("u"))uni_db=cmd.getOptionValue("bdb");
		if(cmd.hasOption("bbb"))blast_bin=cmd.getOptionValue("bbb");
		if(cmd.hasOption("create"))create=true;
	}
	
	
}
