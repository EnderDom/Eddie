package enderdom.eddie.tasks.database;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import enderdom.eddie.bio.fasta.Fasta;
import enderdom.eddie.bio.sequence.BioFileType;
import enderdom.eddie.bio.sequence.SequenceList;
import enderdom.eddie.bio.sequence.UnsupportedTypeException;

import enderdom.eddie.databases.manager.DatabaseManager;

import enderdom.eddie.tasks.TaskXT;
import enderdom.eddie.tools.Tools_CLI;
import enderdom.eddie.tools.Tools_String;
import enderdom.eddie.tools.Tools_System;
import enderdom.eddie.ui.UI;

public class Task_dbTools extends TaskXT{

	private String contig;
	private boolean readsasfasta;
	private String output;
	private boolean all;
	private int run_id = -1;
	
	public Task_dbTools(){
		setHelpHeader("--Database Tools--");
	}
	
	public void printHelpMessage(){
		Tools_CLI.printHelpMessage(getHelpHeader(), "-- Share And Enjoy! --", this.options);
		System.out.println("Examples Uses:");
		System.out.println("-task -c Seqman_Digest_2121 -readsasfasta -o /tmp/Out.fasta");
		System.out.println("[Will download all reads which were used for contig with ID Seqman_Digest_2121]");
		System.out.println();
		System.out.println("-task -run_id 2 -o /tmp/Out.fasta");
		System.out.println("[Will download all contigs which were assembled during the assembly linked by run_id 2]");
		System.out.println();
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("c"))contig = cmd.getOptionValue("c");
		if(cmd.hasOption("readsasfasta"))readsasfasta = true;
		if(cmd.hasOption("o"))output = cmd.getOptionValue("output");
		if(cmd.hasOption("all"))all=true;
		if(cmd.hasOption("run_id")){
			all=true;
			Integer run = Tools_String.parseString2Int(cmd.getOptionValue("run_id"));
			if(run != null){
				this.run_id = run; 
			}
		}
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("c","contig", true, "Contig name, used with readsasfasta"));
		options.addOption(new Option("readsasfasta", false, "Pulls reads which make this contig as fasta"));
		options.addOption(new Option("o","output", true, "Output file"));
		options.addOption(new Option("all",false, "Batch download all contigs available"));
		options.addOption(new Option("run_id",true, "Batch download all contigs attached to run_id"));
	}
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		if(readsasfasta){
			DBReadsAsFasta(contig, output, password, ui);
		}
		else if(all){
			if(this.output == null){
				logger.error("Please set output");
				return;
			}
			DBContigsAsFasta(output, run_id, password, ui);
		}

		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	
	public static void DBReadsAsFasta(String contig, String output, String password, UI ui){
		DatabaseManager manager = ui.getDatabaseManager(password);
		if(manager.open()){
			if(contig != null && output != null){
				int m = manager.getBioSQLXT().getBioEntryId(manager, contig, true, manager.getEddieDBID());
				if(m == -1){
					Logger.getRootLogger().error("Failed to retrieve the ID for contig " + contig);
				}
				else{
					int[] reads = manager.getBioSQLXT().getReads(manager, m);
					if(reads.length == 0){
						Logger.getRootLogger().error("Failed to get any reads for contig " + contig + "(ID:"+m+")");
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
							 success = fasta.save2Fasta(new File(output)) != null;
						} 
						catch (IOException e) {
							success = false;
							Logger.getRootLogger().error("Failed to save fasta" , e);
						}
						if(success){
							Logger.getRootLogger().info("Successfully saved fasta at " + output);
						}
					}
				}
			}
			else{
				Logger.getRootLogger().error("No output/contig set");
			}
		}
		else{
			Logger.getRootLogger().error("Failed to connect to Database");
		}
	}
	
	public static void DBContigsAsFasta(String output, int run_id, String password, UI ui){
		DatabaseManager manager = ui.getDatabaseManager(password);
		if(manager.open()){
			SequenceList list = new Fasta();
			list = manager.getBioSQLXT().getContigsAsFasta(manager, list, run_id);
			Logger.getRootLogger().info("Saving "+list.getNoOfSequences()+" Contigs to Fasta file " + output);
			try {
				list.saveFile(new File(output), BioFileType.FASTA);
			} catch (UnsupportedTypeException e) {
				Logger.getRootLogger().error(e);
			} catch (Exception e) {
				Logger.getRootLogger().error(e);
			}
		}
		else{
			Logger.getRootLogger().error("Failed to connect to Database");
		}
	}
}

