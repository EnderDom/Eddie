package tasks.database;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import bio.fasta.Fasta;
import bio.fasta.FastaParser;

import databases.bioSQL.interfaces.BioSQL;
import databases.manager.DatabaseManager;

import tasks.TaskXT;
import tools.Tools_System;
import ui.UI;

/**
 * The idea here is to upload an entire ACE fileset of data  
 * into a biological database ie bioSQL. This is probably beyond
 * what this database is for as is refered to in the  schema regarding location
 * It also may well cause issues if you try and use gbrowse with this
 * ... But I think I'd prefer to use bioSQL than use a custom schema
 * it at least gives others a fighting chance of being able to 
 * interact with this data easily. 
 * ...Perhaps...
 * ...Maybe.
 */

public class Task_Assembly2DB extends TaskXT{

	UI ui;
	private boolean uploadreads;
	private String identifier;
	
	public Task_Assembly2DB(){
		setHelpHeader("--This is the Help Message for the Assemby2DB Task--");
	}
	
	public void parseArgsSub(CommandLine cmd){
		super.parseArgsSub(cmd);
		if(cmd.hasOption("uploadreads"))uploadreads=true;
		if(cmd.hasOption("identifier"))this.identifier=cmd.getOptionValue("identifier");
	}
	
	public void buildOptions(){
		super.buildOptions();
		options.addOption(new Option("u","uploadreads", false, "Uploads a read Fasta or Fastq file"));
		options.addOption(new Option("id","identifier", true, "Uses this as the base identifier"));
		//options.addOption(new Option("species", false, "Drags out the default Species")); //TODO
		//options.addOption(new Option("taxon_id", false, "Set the taxon_id"));
	}
	
	public Options getOptions(){
		return this.options;
	}
	
	public void run(){
		setComplete(started);
		Logger.getRootLogger().debug("Started running Assembly Task @ "+Tools_System.getDateNow());
		
		DatabaseManager manager = this.ui.getDatabaseManager();
		if(manager.open()){
			if(uploadreads){
				File file = new File(this.input);
				if(file.exists()){
					if(filetype == null)this.filetype =detectFileType(input);
					boolean fastq = false;
					if(filetype.equals("FASTQ")){
						fastq=true;
						logger.debug("File is detected as Fastq");
					}
					Fasta fasta = new Fasta();
					fasta = new Fasta();
					fasta.setFastq(true);
					FastaParser parser = new FastaParser(fasta);
					try{
						logger.debug("Parsing....");
						if(fastq)parser.parseFastq(file);
						else parser.parseFasta(file);
						
						LinkedHashMap<String, String> sequences = fasta.getSequences();
						BioSQL bs = manager.getBioSQL();
						int biodatabase_id = manager.getEddieDBID();
						logger.debug("Bio Database id: "+biodatabase_id);
						if(biodatabase_id < 0){
							logger.error("No Biodatase entry for Eddie");
							return;
						}
						int count =0;
						int size = sequences.size();
						logger.debug("Uploading....");
						for(String s : sequences.keySet()){
							if(!bs.addSequence(manager.getCon(), biodatabase_id, null, s, s, this.identifier+count, "READ", null, 1, sequences.get(s), BioSQL.alphabet_DNA))break;
							count++;
							System.out.print("\r"+(count) + " of " +size + "       ");
						}
						System.out.println();
						if(count != size-1){
							logger.error("Failed to upload all the sequences");
						}
					}
					catch(IOException io){
						logger.error("Failed to parse fasta/q file "  +input, io);
					}
				}
				else{
					logger.error("File "+ input + " does not exist");
				}
			}
			else{
				logger.error("No option selected");
			}
			manager.close();
		}
		else{
			logger.error("Failed to open Connection");
		}
		
		Logger.getRootLogger().debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
	    setComplete(finished);
	}
	
	public boolean wantsUI(){
		return true;
	}
	
	public void addUI(UI ui){
		this.ui = ui;
	}
	
}
