package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.fasta.Fasta;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.UI;

public class Task_dbTools extends TaskXT{

	private String contig;
	private boolean readsasfasta;
	private String output;

	
	public Task_dbTools(){
		setHelpHeader("--Database Tools--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("c"))contig = cmd.getOptionValue("c");
		if(cmd.hasOption("readsasfasta"))readsasfasta = true;
		if(cmd.hasOption("o"))output = cmd.getOptionValue("output");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("c","contig", true, "Contig name"));
		options.addOption(new Option("readsasfasta", false, "Pulls reads which make this contig as fasta"));
		options.addOption(new Option("o","output", true, "Output file"));
	}
	

	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(readsasfasta){
			DatabaseManager manager = ui.getDatabaseManager(password);
			if(manager.open()){
				if(contig != null && output != null){
					int m = manager.getBioSQLXT().getBioEntryId(manager, contig, true, manager.getEddieDBID());
					if(m == -1){
						logger.error("Failed to retrieve the ID for contig " + contig);
					}
					else{
						int[] reads = manager.getBioSQLXT().getReads(manager, m);
						if(reads.length == 0){
							logger.error("Failed to get any reads for contig " + contig + "(ID:"+m+")");
							return;
						}
						else{
							Fasta fasta = new Fasta();
							for(int i : reads){
								String[] names = manager.getBioSQL().getBioEntryNames(manager.getCon(), i);
								String sequence = manager.getBioSQL().getSequence(manager.getCon(), i);
								fasta.addSequence(names[0], sequence);
							}
							boolean success = false;
							try {
								 success = fasta.save2Fasta(new File(this.output));
							} 
							catch (IOException e) {
								success = false;
								logger.error("Failed to save fasta" , e);
							}
							if(success){
								logger.info("Successfully saved fasta at " + output);
							}
						}
					}
				}
				else{
					if(output == null){
						logger.error("No output set");
						return;
					}
					else if(contig != null){
						int i = this.ui.requiresUserYNI("No Contig was specified, do you want to get all reads available?", "Contig not specified!");
						if(i == UI.YES){
							//	TODO dump all reads
							logger.error("This option has not been implemented yet");
						}
					}
				}
			}
			else{
				logger.error("Failed to connect to Database");
			}
		}

		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
}

