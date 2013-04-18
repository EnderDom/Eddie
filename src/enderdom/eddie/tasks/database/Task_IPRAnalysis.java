package enderdom.eddie.tasks.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;

public class Task_IPRAnalysis extends TaskXT{

	private String output;
	private DatabaseManager manager;
	private int runid;
	//private boolean ignore;
	
	public Task_IPRAnalysis(){
		setHelpHeader("--Task outputs interpro analysis using database records--");
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		output = getOption(cmd, "o", null);
		runid = getOption(cmd, "ri",-1);
		//ignore = cmd.hasOption("ignore");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("o", "output", true,  "Output file location"));
		//options.addOption(new Option("ignore", false, "Ignore noIPR term"));
		options.addOption(new Option("ri", true, "Run ID for interpro"));
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		if(output !=null && runid != -1){
			manager = ui.getDatabaseManager(this.password);
			try{
				if(manager.open())getStats();
				else logger.error("Failed to open database");
			}
			catch(Exception e){
				logger.error("Failed to open database",e);
			}
			
		}
		else{
			logger.error("Output and run id should be set");
		}
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
	
	public void getStats() throws IOException{
		String n = FilenameUtils.getFullPath(output)+FilenameUtils.getBaseName(output)+".csv";
		File file = new File(n);
		FileWriter fstream = new FileWriter(file, false);
		@SuppressWarnings("unused")
		BufferedWriter out = new BufferedWriter(fstream);
	}
}
