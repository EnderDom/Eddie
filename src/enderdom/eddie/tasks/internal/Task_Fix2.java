package enderdom.eddie.tasks.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import enderdom.eddie.bio.factories.SequenceListFactory;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;
import enderdom.eddie.databases.manager.DatabaseManager;
import enderdom.eddie.exceptions.EddieDBException;
import enderdom.eddie.exceptions.EddieGenericException;
import enderdom.eddie.tasks.TaskState;
import enderdom.eddie.tasks.TaskXTwIO;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;

public class Task_Fix2 extends TaskXTwIO{

	private String start;
	private String stop;
	private boolean notdryrun;
	
	public Task_Fix2(){

	}

	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		start = getOption(cmd, "tf", null);
		stop = getOption(cmd, "tt", null);
		notdryrun = cmd.hasOption("n");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.removeOption("w");
		options.removeOption("o");
		options.removeOption("filetype");
		options.getOption("i").setDescription("Filepath of fasta file");
		options.addOption(new Option("tf","trimfrom", true, "Trim accession from, default : \"|ref|\""));
		options.addOption(new Option("tt","trimto", true, "Trim accession to default : \"|\""));
		options.addOption(new Option("n","notdryrun", false, "Actually upload rather than testing"));
	}
	
	public void run(){
		setCompleteState(TaskState.STARTED);
		logger.debug("Started running Blast Upload Task @ "+Tools_System.getDateNow());
		DatabaseManager manager = ui.getDatabaseManager(password);
		try{
			if(manager.open()){
				SequenceList l = SequenceListFactory.getSequenceList(input);
				int c=0;
				int s = l.getNoOfSequences();
				for(String seqname : l.keySet()){

						String description = seqname.substring(seqname.indexOf(" ")+1, seqname.length());
						description = description.replaceAll("\\| Symbols: ", "");
						String acc = null;
						if(start == null && stop ==null){
							acc = seqname.substring(0, seqname.indexOf(" ")); 
						}
						else{
							acc = Tools_String.cutLineBetween(start, stop, seqname);
						}
						
						if(notdryrun){
							manager.getBioSQL().genericUpdate(manager.getCon(), new String[]{"description","version"},
								new String[]{description, "1"}, "dbxref", new String[]{"accession", "version"}, 
								new String[]{acc, "0"});
							System.out.print("\r"+(c++) + " of " + s + "             ");
						}
						else{
							System.out.println(acc + ":" + description);
						}
				}
				if(notdryrun)System.out.println("\r"+(c++) + " of " + s + "             ");
			}
			setCompleteState(TaskState.ERROR);
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
		} catch (FileNotFoundException e) {
			logger.error("Failed to parse fasta", e);
		} catch (UnsupportedTypeException e) {
			logger.error("Failed to parse fasta", e);
		} catch (IOException e) {
			logger.error("Failed to parse fasta", e);
		} catch (EddieGenericException e) {
			logger.error("Failed to parse string properly", e);
		}
		
		
		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setCompleteState(TaskState.FINISHED);
	}
}
