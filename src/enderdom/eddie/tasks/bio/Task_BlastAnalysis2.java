package enderdom.eddie.tasks.bio;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;


import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

public class Task_BlastAnalysis2 extends TaskXT{

	private int blastRunID;
	private int assRunID;
	private String output;
	private double evalue;
	private DatabaseManager manager;
	
	public Task_BlastAnalysis2(){
		setHelpHeader("--Task outputs blast analysis using database and assembly files--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("rb")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("rb"));
			if(i !=null)blastRunID=i;
			else logger.error("Failed to parse rb" + cmd.getOptionValue("rb"));
		}
		if(cmd.hasOption("ra")){
			Integer i = Tools_String.parseString2Int(cmd.getOptionValue("ra"));
			if(i !=null)assRunID=i;
			else logger.error("Failed to parse ra" + cmd.getOptionValue("ra"));
		}
		if(cmd.hasOption("e")){
			Double d = Tools_String.parseString2Double(cmd.getOptionValue("d"));
			if(d!=null)evalue=d;
		}
		if(cmd.hasOption("o"))output=cmd.getOptionValue("o");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("o", "output", true,  "Output file location"));
		options.addOption(new Option("rb", "runBlast", true, "Set assembly run id if needed"));
		options.addOption(new Option("e", "evalue", true, "Evalue filter"));
		options.addOption(new Option("numbhits", true, "Number of hits to limit to."));
	}

	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		try{
			manager = ui.getDatabaseManager(this.password);
			if(manager.open()){
				
			}
			else logger.error("Failed to open manager");
		}
		catch(Exception e){
			logger.error("Failed to run database",e);
			this.setCompleteState(TaskState.ERROR);
		}
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public int getDbxref_Bioentry_Count(DatabaseManager manager, int run_id, int hit_no, double evalue){
		String[][] stuff = manager.getBioSQL().getGenericResults(manager.getCon(), new String[]{"bioentry_id"}, "bioentry_dbxref",
				new String[]{"run_id", "evalue", "hit_no"},new String[]{run_id+"", evalue+"", hit_no+""});
		return stuff[0].length;
	}
	
}
