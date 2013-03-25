package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.SequenceObject;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.UI;

@SuppressWarnings("unused")
public class Task_ESTUpload extends TaskXT{
	
	private String input;

	private String name;
	private String prot;
	private int runid;
	
	/* 
	 * WIP
	 */
	
	public Task_ESTUpload(){
		setHelpHeader("--This is the Help Message for the Assemby2DB Task--");
		options.addOption(new Option("p","proteins", true, "Include protein sequence file, which can be used to validate upload"));
		options.addOption(new Option("i","input",true, "Input file from ESTscan containing ESTs"));
		options.addOption("r", "runid", true, "Map ESTs using the runid for the assembly");
		options.removeOption("w");
		options.removeOption("o");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("i")){
			this.input = cmd.getOptionValue("i");
		}
		if(cmd.hasOption("p")){
			this.prot = cmd.getOptionValue("p");
		}
		if(cmd.hasOption("n")){
			this.name = cmd.getOptionValue("n");
		}
		if(cmd.hasOption("r")){
			Integer r = Tools_String.parseString2Int(cmd.getOptionValue("r"));
			if(r != null){
				runid = r;
			}
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
	}
	
	public Options getOptions(){
		return this.options;
	}
	public void run(){
		setCompleteState(TaskState.STARTED);
		Logger.getRootLogger().debug("Started running ESTUploadTask @ "+Tools_System.getDateNow());
		if(input != null){
			File f = new File(input);
			if(f.isFile()){
				try {
					SequenceList l = SequenceListFactory.getSequenceList(input);
					DatabaseManager man = ui.getDatabaseManager();
					while(l.hasNext()){
						SequenceObject o = l.next();						
					}
					
				}catch (Exception e) {
					logger.error("Failed to parse sequences ", e);
					setCompleteState(TaskState.ERROR);
				}
				
			}
		}
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		logger.debug("UI "+ui.getClass().getName()+" was given to me " + this.getClass().getName());
		this.ui = ui;
	}
	
	protected UI getUI(){
		return ui;
	}
}
