package enderdom.eddie.tasks.database;

import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.exceptions.EddieDBException;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_System;

public class Task_AssemblyReport extends TaskXTwIO{

	private int run_id;
	private int ncross;
	
	public Task_AssemblyReport(){
		setHelpHeader("--This is the Help Message for the AssemblyReport Task--");
	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		run_id = getOption(cmd, "r", -1);
		ncross = getOption(cmd, "n", 3);
		
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("n", "ncross", true, "Number to cross section"));
		options.addOption(new Option("r","run_id", true, "run id for the meta assembly"));
		options.removeOption("w");
		options.getOption("o").setDescription("Output folder, will save html report and related files within folder");
	}
	
	public void run(){
		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		setCompleteState(TaskState.STARTED);

		DatabaseManager manager = this.ui.getDatabaseManager(password);
		LinkedList<Integer> programindexstart = new LinkedList<Integer>();
		String currentprogram="";
		
		try {
			if(manager.open()){
				//Lots of debug logs because this array maths is making my brain hurt
				logger.debug("Generating list of contigs attached to meta assembly run_id " + run_id);
				String[][] contigs = manager.getBioSQLXT().getListOfContigsfromMetaAssembly(manager, run_id);
				for(int i =0; i < contigs[0].length;i++){
					if(!contigs[3][i].equals(currentprogram)){
						programindexstart.add(i);
						currentprogram = contigs[3][i];
						logger.debug(currentprogram + " assembly program identified beginning at index " + programindexstart.getLast());
					}
				}
				programindexstart.add(contigs[0].length-1);
				for(int i = 1 ; i< programindexstart.size();i++){
					logger.debug("Assessing program " + contigs[3][programindexstart.get(i-1)]);
					int[] indices = new int[ncross];
					int delta = programindexstart.get(i)-programindexstart.get(i-1);
					logger.debug("Sub array of contigs between "+(programindexstart.get(i-1)) 
							+ " and " + (programindexstart.get(i)) +" (delta: "+delta+")"  );
					for(int j = 0; j < indices.length;j++){
						indices[j]= programindexstart.get(i)+(delta/(ncross-1))*j;
						logger.debug("Contig at index " + indices[j] + " called "+contigs[1][indices[j]]
						+"selected and stored  " + j);
					}
					indices[ncross-1]=programindexstart.get(i)+delta-1;
					logger.debug(ncross-1+" store reset to last contig (compensating for remainder drift) at "
							+indices[ncross-1] + " called" +contigs[1][indices[ncross-1]]);
				}
			}
			else logger.error("Failed to open database"); 
			
		} catch (InstantiationException e) {
			logger.error("Failed to open database", e);
		} catch (IllegalAccessException e) {
			logger.error("Failed to open database", e);
		} catch (ClassNotFoundException e) {
			logger.error("Failed to open database", e);
		} catch (SQLException e) {
			logger.error("Failed to open database", e);
		} catch (EddieDBException e) {
			logger.error("Failed to open database", e);
		} catch (InterruptedException e) {
			logger.error("Failed to open database", e);
		}
		
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
}
