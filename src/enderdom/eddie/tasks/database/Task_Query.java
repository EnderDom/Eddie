package enderdom.eddie.tasks.database;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FilenameUtils;

import enderdom.eddie.databases.bioSQL.psuedoORM.Run;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

public class Task_Query extends TaskXT{

	private int blastRunID;
	private int assRunID;
	private String output;
	private boolean cumulcount;
	private DatabaseManager manager;

	public Task_Query(){
		setHelpHeader("--Task just for running the queries I can't remember--");
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
		if(cmd.hasOption("o"))output=cmd.getOptionValue("o");
		if(cmd.hasOption("OP_c"))cumulcount=true;
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("o", "output", true,  "Output file location"));
		options.addOption(new Option("OP_c", "OP_cumulCount", false, "Outputs a cumulative score for -ra & -rb"));
		options.addOption(new Option("OP_b", "OP_blastStats", false, "Outputs blast statistics"));
		options.addOption(new Option("ra", "runAssembly", true, "Set assembly run id if needed"));
		options.addOption(new Option("rb", "runBlast", true, "Set assembly run id if needed"));
		
	}

	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(cumulcount && output!=null){
			manager = ui.getDatabaseManager(password);
			try{
				if(manager.open()){
					if(assRunID > 0){
						manager.getBioSQLXT().cumulativeCountQuery(manager, new File(output),this.blastRunID, this.assRunID);
					}
					else{
						logger.info("You did not set assembly run, running for each assembly avaialble");
						int[] asses = manager.getBioSQLXT().getRunId(manager, null, Run.RUNTYPE_ASSEMBLY);
						File f;
						for(int i=0;i < asses.length; i++){
							Run run = manager.getBioSQLXT().getRun(manager, asses[i]);
							String filename = run.getProgram().replaceAll(" ", "_") +"_"; 
							filename += (run.getSource().indexOf(" ") != 1) ? run.getSource().split(" ")[0] : run.getSource();
							f = new File(FilenameUtils.getFullPath(output)+filename+".dat");
							if(manager.getBioSQLXT().cumulativeCountQuery(manager, f,this.blastRunID, asses[i])){
								logger.info("Appears to have succesfully run for assembly ID "+asses[i]+", output at " +f.getPath());
							}
							else logger.error("Error returned, please check logs");
						}
					}			
				}
				else throw new Exception("Failed to open database");
			}
			catch(Exception e){
				logger.error("Failed database or something",e);
				setCompleteState(TaskState.ERROR);
				return;
			}
		}
		else logger.error("cumulcount and/or output not set");
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
}
