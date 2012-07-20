package tasks.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import databases.bioSQL.psuedoORM.Run;
import databases.manager.DatabaseManager;

import tasks.TaskXT;
import tools.Tools_System;
import ui.UI;

public class Task_AddRunData extends TaskXT{

	private Run run;
	private boolean list;
	private DatabaseManager manager;
	private UI ui;
	
	public Task_AddRunData(){
		setHelpHeader("--This is the Help Message for the AddRunData Task--"
				+Tools_System.getNewline()+"It might be advisable to use the GUI for this");
		this.run = new Run();
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("program"))run.setProgram(cmd.getOptionValue("program"));
		if(cmd.hasOption("version"))run.setVersion(cmd.getOptionValue("version"));
		if(cmd.hasOption("run_type"))run.setRuntype(cmd.getOptionValue("run_type"));
		if(cmd.hasOption("run_date"))run.setDateValue(cmd.getOptionValue("run_date"),"dd-MM-yyyy");
		if(cmd.hasOption("dbname"))run.setDbname(cmd.getOptionValue("dbname"));
		if(cmd.hasOption("params"))run.setParams(cmd.getOptionValue("params"));
		if(cmd.hasOption("comment"))run.setComment(cmd.getOptionValue("comment"));
		if(cmd.hasOption("list"))this.list = true;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("prg","program", true, "Name of program used for run"));
		options.addOption(new Option("ver","version", true, "Version of program run"));
		options.addOption(new Option("rtp","run_type", true, "Run type, see list of current types"));
		options.addOption(new Option("pdb","run_date", true, "Run date, date of run dd-MM-yyyy"));
		options.addOption(new Option("dbn","dbname", true, "Database name, if any database used"));
		options.addOption(new Option("par","params", true, "Program parameters"));
		options.addOption(new Option("com","comment", true, "Any additional comments about the run"));
		options.addOption(new Option("list", false, "List all current run programs/versions/dbs"));
	}
	/*
	 * 
	 * 
	 * 
	 */
	
	public void run(){
		manager = this.ui.getDatabaseManager(password);
		
		if(manager.open()){
			if(this.list){
				System.out.println(run.list(manager));
			}
			else{
				if(run.validate()){
					run.uploadRun(manager);
				}
				else{
					for(String s : run.getValidationErrors())logger.error(s);
				}
			}
		}
		else{
			logger.error("Failed to open database connection");
		}
	}
	
	
}
