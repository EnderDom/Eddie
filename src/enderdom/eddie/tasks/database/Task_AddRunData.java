package enderdom.eddie.tasks.database;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

public class Task_AddRunData extends TaskXT{

	private Run run;
	private boolean list;
	private DatabaseManager manager;
	private int remove;
	
	public Task_AddRunData(){
		logger.debug("Task_AddRunData has been initalised");
		setHelpHeader("--This is the Help Message for the AddRunData Task--");
		this.run = new Run();
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		run.setProgram(getOption(cmd, "program", "Unknown Program"));
		run.setVersion(getOption(cmd, "version", "0"));
		run.setRuntype(getOption(cmd, "run_type", "UNKNOWN"));
		run.setDateValue(getOption(cmd, "run_date",
				Tools_System.getDateNow(Tools_System.SQL_DATE_FORMAT)), Tools_System.SQL_DATE_FORMAT);
		run.setDbname(getOption(cmd, "dbname", null));
		run.setParams(getOption(cmd, "params", null));
		run.setComment(getOption(cmd, "comment", null));
		run.setSource(getOption(cmd, "source", null));
		String s = getOption(cmd, "parent", null);
		run.setParent_id(s==null?null:Tools_String.parseString2Int(s));
		this.list = cmd.hasOption("list");
		remove = getOption(cmd, "removeRun", -1);
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("prt","parent", true, "Parent run id"));
		options.addOption(new Option("prg","program", true, "Name of program used for run"));
		options.addOption(new Option("ver","version", true, "Version of program run"));
		options.addOption(new Option("rtp","run_type", true, "Run type (eg. BLAST, ASSEMBLY...), see list of current types"));
		options.addOption(new Option("rdt","run_date", true, "Run date, date of run "+Tools_System.SQL_DATE_FORMAT));
		options.addOption(new Option("dbn","dbname", true, "Database name, if any database used"));
		options.addOption(new Option("par","params", true, "Program parameters"));
		options.addOption(new Option("com","comment", true, "Any additional comments about the run"));
		options.addOption(new Option("src","source", true, "Source of the Data eg \"Arabiopsis root cDNA\""));
		options.addOption(new Option("list", false, "List all current run programs/versions/dbs"));
		options.addOption(new Option("rem", "removeRun", true, "Remove the run (only the run id) with run id specified"));
	}

	
	public void run(){
		setCompleteState(TaskState.STARTED);
		if(password == null){
			manager = this.ui.getDatabaseManager();
		}
		else{
			manager = this.ui.getDatabaseManager(password);
		}
		try{
			manager.open();
			if(this.list){
				System.out.println(run.list(manager));
			}
			else if(remove > -1){
				manager.getBioSQLXT().removeRun(manager, remove);
				logger.debug("Removed run with run id " + remove);
			}
			else{
				if(run.validate()){
					int run_id = run.uploadRun(manager);
					System.out.println("RUN_ID:"+run_id);
					logger.debug("Run Uploaded");
				}
				else{
					for(String s : run.getValidationErrors())logger.error(s);
				}
			}
		}
		catch(Exception e){
			logger.error("Failed to open database " , e);
			setCompleteState(TaskState.ERROR);
		}
		setCompleteState(TaskState.FINISHED);
	}
	
	
}
